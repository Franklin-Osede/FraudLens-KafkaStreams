package com.fraudlens.infrastructure.kafka;

import com.fraudlens.domain.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class TransactionProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionProducer.class);
    private static final String TOPIC = "transactions";
    
    @Autowired
    private KafkaTemplate<String, Transaction> kafkaTemplate;
    
    private final Random random = new Random();
    private final List<String> countries = Arrays.asList(
        "ES", "FR", "DE", "IT", "UK", "US", "CA", "JP", "AU", "BR", "MX", "AR", "CN", "IN", "RU"
    );
    
    private final List<String> transactionTypes = Arrays.asList(
        "PURCHASE", "WITHDRAWAL", "TRANSFER", "PAYMENT", "DEPOSIT"
    );
    
    private final List<String> merchants = Arrays.asList(
        "Amazon", "Apple Store", "Google Play", "Netflix", "Spotify", "Uber", "Airbnb", 
        "McDonald's", "Starbucks", "Shell", "Repsol", "El Corte Inglés", "Zara", "H&M"
    );
    
    private final List<String> accountIds = Arrays.asList(
        "ACC-001", "ACC-002", "ACC-003", "ACC-004", "ACC-005", 
        "ACC-006", "ACC-007", "ACC-008", "ACC-009", "ACC-010"
    );

    @Scheduled(fixedDelay = 2000) // Every 2 seconds
    @SuppressWarnings("unchecked")
    public void generateNormalTransaction() {
        Transaction transaction = createNormalTransaction();
        sendTransaction(transaction);
    }

    @Scheduled(fixedDelay = 15000) // Every 15 seconds
    public void generateSuspiciousActivity() {
        String accountId = accountIds.get(random.nextInt(accountIds.size()));
        
        logger.info("🚨 Generating suspicious activity for account: {}", accountId);
        
        // Generate multiple transactions that will trigger the alert
        for (int i = 0; i < 4; i++) {
            Transaction transaction = createSuspiciousTransaction(accountId);
            sendTransaction(transaction);
            
            // Small pause between transactions
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private Transaction createNormalTransaction() {
        return new Transaction(
            "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            accountIds.get(random.nextInt(accountIds.size())),
            generateRandomAmount(10.0, 500.0),
            countries.get(random.nextInt(countries.size())),
            "EUR",
            transactionTypes.get(random.nextInt(transactionTypes.size())),
            Instant.now(),
            merchants.get(random.nextInt(merchants.size())),
            "Normal transaction"
        );
    }

    private Transaction createSuspiciousTransaction(String accountId) {
        return new Transaction(
            "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            accountId,
            generateRandomAmount(300.0, 800.0), // Higher amounts
            countries.get(random.nextInt(countries.size())), // Different countries
            "EUR",
            "PURCHASE",
            Instant.now(),
            merchants.get(random.nextInt(merchants.size())),
            "Suspicious high-value transaction"
        );
    }

    private BigDecimal generateRandomAmount(double min, double max) {
        double amount = min + (max - min) * random.nextDouble();
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }

    @Async
    public CompletableFuture<SendResult<String, Transaction>> sendTransaction(Transaction transaction) {
        logger.debug("Sending transaction: {} for account: {} amount: €{} country: {}", 
                    transaction.getTransactionId(), 
                    transaction.getAccountId(), 
                    transaction.getAmount(), 
                    transaction.getCountry());
        
        return kafkaTemplate.send(TOPIC, transaction.getAccountId(), transaction)
                .whenComplete((result, exception) -> {
                    if (exception == null) {
                        logger.debug("Transaction sent successfully: {}", transaction.getTransactionId());
                    } else {
                        logger.error("Failed to send transaction: {}", transaction.getTransactionId(), exception);
                    }
                });
    }

    // Method to generate specific transactions for demonstration
    public void generateFraudScenario(String accountId) {
        logger.info("🎯 Generating fraud scenario for account: {}", accountId);
        
        // Generate 5 transactions in different countries that will total more than €1000
        String[] fraudCountries = {"ES", "FR", "DE", "IT", "UK"};
        
        for (int i = 0; i < fraudCountries.length; i++) {
            Transaction transaction = new Transaction(
                "FRAUD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                accountId,
                new BigDecimal("250.00"), // €250 cada una = €1250 total
                fraudCountries[i],
                "EUR",
                "PURCHASE",
                Instant.now(),
                "Suspicious Merchant " + (i + 1),
                "Fraud demo transaction"
            );
            
            sendTransaction(transaction);
            
            // Short pause between transactions
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
} 