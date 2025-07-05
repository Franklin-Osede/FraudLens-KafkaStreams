package com.fraudlens.domain.service;

import com.fraudlens.domain.model.AccountActivityWindow;
import com.fraudlens.domain.model.FraudAlert;
import com.fraudlens.domain.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class FraudDetectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionService.class);
    
    private static final BigDecimal FRAUD_THRESHOLD = new BigDecimal("1000.00");
    private static final int MIN_COUNTRIES_FOR_FRAUD = 3;
    private static final String MULTI_COUNTRY_FRAUD_TYPE = "MULTI_COUNTRY_HIGH_VALUE";

    public boolean isFraudulent(AccountActivityWindow activityWindow) {
        if (activityWindow == null) {
            return false;
        }
        
        boolean exceedsAmount = activityWindow.exceedsAmountThreshold();
        boolean hasMultipleCountries = activityWindow.hasMultipleCountries();
        
        logger.debug("Fraud detection for account {}: Amount exceeds threshold={}, Country count={}", 
                    activityWindow.getAccountId(), exceedsAmount, activityWindow.getCountryCount());
        
        return exceedsAmount && hasMultipleCountries;
    }

    public FraudAlert generateFraudAlert(AccountActivityWindow activityWindow) {
        if (!isFraudulent(activityWindow)) {
            throw new IllegalArgumentException("Activity window does not meet fraud criteria");
        }
        
        int riskScore = calculateRiskScore(activityWindow);
        String description = generateDescription(activityWindow);
        
        return new FraudAlert(
            generateAlertId(),
            activityWindow.getAccountId(),
            MULTI_COUNTRY_FRAUD_TYPE,
            activityWindow.getTotalAmount(),
            activityWindow.getCountries(),
            activityWindow.getTransactionCount(),
            activityWindow.getWindowStart(),
            activityWindow.getWindowEnd(),
            Instant.now(),
            riskScore,
            description
        );
    }

    private int calculateRiskScore(AccountActivityWindow activityWindow) {
        int baseScore = 50;
        
        // Puntuación por importe
        BigDecimal amountMultiplier = activityWindow.getTotalAmount().divide(FRAUD_THRESHOLD, 2, BigDecimal.ROUND_HALF_UP);
        int amountScore = Math.min(30, amountMultiplier.intValue() * 10);
        
        // Puntuación por número de países
        int countryScore = Math.min(20, (activityWindow.getCountryCount() - 2) * 5);
        
        // Puntuación por número de transacciones
        int transactionScore = Math.min(10, activityWindow.getTransactionCount() * 2);
        
        int totalScore = baseScore + amountScore + countryScore + transactionScore;
        
        logger.debug("Risk score calculation for account {}: base={}, amount={}, countries={}, transactions={}, total={}", 
                    activityWindow.getAccountId(), baseScore, amountScore, countryScore, transactionScore, totalScore);
        
        return Math.min(100, totalScore);
    }

    private String generateDescription(AccountActivityWindow activityWindow) {
        return String.format(
            "Suspicious activity detected: €%.2f across %d countries in %d transactions within 5-minute window",
            activityWindow.getTotalAmount(),
            activityWindow.getCountryCount(),
            activityWindow.getTransactionCount()
        );
    }

    private String generateAlertId() {
        return "FRAUD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public boolean shouldTriggerAlert(Transaction transaction, AccountActivityWindow currentWindow) {
        if (transaction == null || currentWindow == null) {
            return false;
        }
        
        // Simulamos agregar la transacción a la ventana actual
        AccountActivityWindow updatedWindow = currentWindow.addTransaction(transaction);
        
        return isFraudulent(updatedWindow);
    }
} 