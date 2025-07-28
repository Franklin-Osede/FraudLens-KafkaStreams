package com.fraudlens.infrastructure.kafka;

import com.fraudlens.domain.model.AccountActivityWindow;
import com.fraudlens.domain.model.FraudAlert;
import com.fraudlens.domain.model.Transaction;
import com.fraudlens.domain.service.FraudDetectionService;
import com.fraudlens.infrastructure.serde.JsonSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.WindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Component
public class FraudDetectionProcessor {

    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionProcessor.class);
    private static final String TRANSACTIONS_TOPIC = "transactions";
    private static final String FRAUD_ALERTS_TOPIC = "fraud-alerts";
    private static final Duration WINDOW_SIZE = Duration.ofMinutes(5);
    private static final Duration GRACE_PERIOD = Duration.ofMinutes(1);

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @Autowired
    @SuppressWarnings("unchecked")
    public void buildPipeline(StreamsBuilder streamsBuilder) {
        logger.info("🔧 Building Kafka Streams topology for fraud detection");

        // Input transaction stream
        KStream<String, Transaction> transactionStream = streamsBuilder
                .stream(TRANSACTIONS_TOPIC, Consumed.with(Serdes.String(), new JsonSerde<>(Transaction.class)))
                .peek((key, transaction) -> 
                    logger.debug("Processing transaction: {} for account: {} amount: €{} country: {}", 
                               transaction.getTransactionId(), 
                               transaction.getAccountId(), 
                               transaction.getAmount(), 
                               transaction.getCountry()));

        // Group by accountId and process in sliding windows
        KTable<Windowed<String>, AccountActivityWindow> accountActivityTable = transactionStream
                .filter((key, transaction) -> transaction.getAccountId() != null)
                .groupByKey(Grouped.with(Serdes.String(), new JsonSerde<>(Transaction.class)))
                .windowedBy(TimeWindows.ofSizeAndGrace(WINDOW_SIZE, GRACE_PERIOD))
                .aggregate(
                    // Initializer: create empty window
                    () -> {
                        Instant now = Instant.now();
                        return AccountActivityWindow.createNew("", now.minus(WINDOW_SIZE), now);
                    },
                    // Aggregator: add transaction to window
                    (accountId, transaction, window) -> {
                        logger.debug("Aggregating transaction {} for account {} in window", 
                                   transaction.getTransactionId(), accountId);
                        return aggregateTransaction(accountId, transaction, window);
                    },
                    // Materializer: configure store with serdes
                    Materialized.<String, AccountActivityWindow, WindowStore<org.apache.kafka.common.utils.Bytes, byte[]>>as("account-activity-store")
                            .withKeySerde(Serdes.String())
                            .withValueSerde(new JsonSerde<>(AccountActivityWindow.class))
                            .withRetention(WINDOW_SIZE.plus(GRACE_PERIOD))
                );

        // Activity window stream
        KStream<Windowed<String>, AccountActivityWindow> activityStream = accountActivityTable
                .toStream()
                .peek((windowedKey, window) -> 
                    logger.debug("Activity window for account {}: €{} across {} countries with {} transactions",
                               windowedKey.key(),
                               window.getTotalAmount(),
                               window.getCountryCount(),
                               window.getTransactionCount()));

        // Detect fraud and generate alerts
        KStream<String, FraudAlert> fraudAlertStream = activityStream
                .filter((windowedKey, window) -> {
                    boolean isFraud = fraudDetectionService.isFraudulent(window);
                    if (isFraud) {
                        logger.warn("🚨 FRAUD DETECTED for account {}: €{} across {} countries", 
                                  windowedKey.key(), 
                                  window.getTotalAmount(), 
                                  window.getCountryCount());
                    }
                    return isFraud;
                })
                .map((windowedKey, window) -> {
                    FraudAlert alert = fraudDetectionService.generateFraudAlert(window);
                    logger.info("🚨 Generated fraud alert: {} for account: {} with risk score: {}", 
                              alert.getAlertId(), 
                              alert.getAccountId(), 
                              alert.getRiskScore());
                    return KeyValue.pair(alert.getAccountId(), alert);
                });

        // Send alerts to output topic
        fraudAlertStream.to(FRAUD_ALERTS_TOPIC, 
                          Produced.with(Serdes.String(), new JsonSerde<>(FraudAlert.class)));

        // Simplified additional statistics log
        activityStream
                .foreach((windowedKey, window) -> 
                    logger.debug("Processing activity window for account {}: {} transactions", 
                               windowedKey.key(), window.getTransactionCount()));

        logger.info("✅ Fraud detection pipeline built successfully");
    }

    private AccountActivityWindow aggregateTransaction(String accountId, Transaction transaction, AccountActivityWindow currentWindow) {
        try {
            // If window is empty, initialize it with transaction data
            if (currentWindow.getAccountId() == null || currentWindow.getAccountId().isEmpty()) {
                Instant windowStart = transaction.getTimestamp().minus(WINDOW_SIZE);
                Instant windowEnd = transaction.getTimestamp();
                
                Set<String> countries = new HashSet<>();
                countries.add(transaction.getCountry());
                
                return new AccountActivityWindow(
                    accountId,
                    windowStart,
                    windowEnd,
                    transaction.getAmount(),
                    1,
                    countries,
                    Instant.now()
                );
            }
            
            // Add transaction to existing window
            BigDecimal newTotal = currentWindow.getTotalAmount().add(transaction.getAmount());
            int newCount = currentWindow.getTransactionCount() + 1;
            Set<String> newCountries = new HashSet<>(currentWindow.getCountries());
            newCountries.add(transaction.getCountry());
            
            return new AccountActivityWindow(
                accountId,
                currentWindow.getWindowStart(),
                currentWindow.getWindowEnd(),
                newTotal,
                newCount,
                newCountries,
                Instant.now()
            );
            
        } catch (Exception e) {
            logger.error("Error aggregating transaction {} for account {}: {}", 
                        transaction.getTransactionId(), accountId, e.getMessage());
            return currentWindow;
        }
    }
} 