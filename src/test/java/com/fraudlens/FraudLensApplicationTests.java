package com.fraudlens;

import com.fraudlens.domain.model.AccountActivityWindow;
import com.fraudlens.domain.model.FraudAlert;
import com.fraudlens.domain.model.Transaction;
import com.fraudlens.domain.service.FraudDetectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class FraudLensApplicationTests {

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @Test
    void contextLoads() {
        // Test that the Spring context loads successfully
        assertNotNull(fraudDetectionService);
    }

    @Test
    void testNormalTransactionCreation() {
        // Test creating a normal transaction
        Transaction transaction = new Transaction(
            "TXN-TEST-001",
            "ACC-001",
            new BigDecimal("150.00"),
            "ES",
            "EUR",
            "PURCHASE",
            Instant.now(),
            "Test Merchant",
            "Normal transaction test"
        );

        assertNotNull(transaction);
        assertEquals("TXN-TEST-001", transaction.getTransactionId());
        assertEquals("ACC-001", transaction.getAccountId());
        assertEquals(new BigDecimal("150.00"), transaction.getAmount());
        assertEquals("ES", transaction.getCountry());
        assertFalse(transaction.isHighValue()); // Should be false for €150
    }

    @Test
    void testHighValueTransaction() {
        // Test creating a high-value transaction
        Transaction transaction = new Transaction(
            "TXN-TEST-002",
            "ACC-002",
            new BigDecimal("1500.00"),
            "FR",
            "EUR",
            "PURCHASE",
            Instant.now(),
            "High Value Merchant",
            "High value transaction test"
        );

        assertTrue(transaction.isHighValue()); // Should be true for €1500
        assertTrue(transaction.isFromCountry("FR"));
    }

    @Test
    void testAccountActivityWindowCreation() {
        // Test creating an account activity window
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(300); // 5 minutes ago
        Instant windowEnd = now;

        AccountActivityWindow window = AccountActivityWindow.createNew("ACC-001", windowStart, windowEnd);

        assertNotNull(window);
        assertEquals("ACC-001", window.getAccountId());
        assertEquals(windowStart, window.getWindowStart());
        assertEquals(windowEnd, window.getWindowEnd());
        assertEquals(BigDecimal.ZERO, window.getTotalAmount());
        assertEquals(0, window.getTransactionCount());
        assertTrue(window.getCountries().isEmpty());
    }

    @Test
    void testAccountActivityWindowWithTransactions() {
        // Test adding transactions to a window
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(300);
        Instant windowEnd = now;

        AccountActivityWindow window = AccountActivityWindow.createNew("ACC-001", windowStart, windowEnd);

        // Add first transaction
        Transaction tx1 = new Transaction(
            "TXN-TEST-003",
            "ACC-001",
            new BigDecimal("300.00"),
            "ES",
            "EUR",
            "PURCHASE",
            now.minusSeconds(200),
            "Merchant 1",
            "First transaction"
        );

        AccountActivityWindow updatedWindow1 = window.addTransaction(tx1);
        assertEquals(new BigDecimal("300.00"), updatedWindow1.getTotalAmount());
        assertEquals(1, updatedWindow1.getTransactionCount());
        assertEquals(1, updatedWindow1.getCountryCount());
        assertTrue(updatedWindow1.getCountries().contains("ES"));

        // Add second transaction from different country
        Transaction tx2 = new Transaction(
            "TXN-TEST-004",
            "ACC-001",
            new BigDecimal("400.00"),
            "FR",
            "EUR",
            "PURCHASE",
            now.minusSeconds(100),
            "Merchant 2",
            "Second transaction"
        );

        AccountActivityWindow updatedWindow2 = updatedWindow1.addTransaction(tx2);
        assertEquals(new BigDecimal("700.00"), updatedWindow2.getTotalAmount());
        assertEquals(2, updatedWindow2.getTransactionCount());
        assertEquals(2, updatedWindow2.getCountryCount());
        assertTrue(updatedWindow2.getCountries().contains("ES"));
        assertTrue(updatedWindow2.getCountries().contains("FR"));
    }

    @Test
    void testFraudDetectionLogic() {
        // Test fraud detection with suspicious activity
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(300);
        Instant windowEnd = now;

        Set<String> countries = new HashSet<>();
        countries.add("ES");
        countries.add("FR");
        countries.add("DE");
        countries.add("IT");

        AccountActivityWindow suspiciousWindow = new AccountActivityWindow(
            "ACC-001",
            windowStart,
            windowEnd,
            new BigDecimal("1200.00"), // Over €1000 threshold
            4, // 4 transactions
            countries, // 4 different countries
            now
        );

        // Should be detected as fraud
        assertTrue(suspiciousWindow.isSuspiciousActivity());
        assertTrue(suspiciousWindow.exceedsAmountThreshold());
        assertTrue(suspiciousWindow.hasMultipleCountries());
        assertEquals(4, suspiciousWindow.getCountryCount());

        // Test normal activity (should not be fraud)
        Set<String> normalCountries = new HashSet<>();
        normalCountries.add("ES");

        AccountActivityWindow normalWindow = new AccountActivityWindow(
            "ACC-002",
            windowStart,
            windowEnd,
            new BigDecimal("500.00"), // Under €1000 threshold
            2, // 2 transactions
            normalCountries, // Only 1 country
            now
        );

        assertFalse(normalWindow.isSuspiciousActivity());
        assertFalse(normalWindow.exceedsAmountThreshold());
        assertFalse(normalWindow.hasMultipleCountries());
    }

    @Test
    void testFraudAlertGeneration() {
        // Test generating a fraud alert
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(300);
        Instant windowEnd = now;

        Set<String> countries = new HashSet<>();
        countries.add("ES");
        countries.add("FR");
        countries.add("DE");

        AccountActivityWindow suspiciousWindow = new AccountActivityWindow(
            "ACC-001",
            windowStart,
            windowEnd,
            new BigDecimal("1500.00"),
            3,
            countries,
            now
        );

        FraudAlert alert = fraudDetectionService.generateFraudAlert(suspiciousWindow);

        assertNotNull(alert);
        assertEquals("ACC-001", alert.getAccountId());
        assertEquals("MULTI_COUNTRY_HIGH_VALUE", alert.getAlertType());
        assertEquals(new BigDecimal("1500.00"), alert.getTotalAmount());
        assertEquals(3, alert.getTransactionCount());
        assertEquals(3, alert.getCountriesInvolved().size());
        assertTrue(alert.getRiskScore() > 50); // Should have significant risk score
        assertTrue(alert.isHighRisk()); // Should be high risk
    }

    @Test
    void testRiskScoreCalculation() {
        // Test different risk score scenarios
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(300);
        Instant windowEnd = now;

        // Low risk scenario
        Set<String> lowRiskCountries = new HashSet<>();
        lowRiskCountries.add("ES");
        lowRiskCountries.add("FR");

        AccountActivityWindow lowRiskWindow = new AccountActivityWindow(
            "ACC-001",
            windowStart,
            windowEnd,
            new BigDecimal("1100.00"), // Just over threshold
            2, // 2 transactions
            lowRiskCountries, // 2 countries
            now
        );

        FraudAlert lowRiskAlert = fraudDetectionService.generateFraudAlert(lowRiskWindow);
        assertTrue(lowRiskAlert.getRiskScore() >= 50 && lowRiskAlert.getRiskScore() < 70);

        // High risk scenario
        Set<String> highRiskCountries = new HashSet<>();
        highRiskCountries.add("ES");
        highRiskCountries.add("FR");
        highRiskCountries.add("DE");
        highRiskCountries.add("IT");
        highRiskCountries.add("UK");

        AccountActivityWindow highRiskWindow = new AccountActivityWindow(
            "ACC-002",
            windowStart,
            windowEnd,
            new BigDecimal("2500.00"), // Much higher amount
            5, // 5 transactions
            highRiskCountries, // 5 countries
            now
        );

        FraudAlert highRiskAlert = fraudDetectionService.generateFraudAlert(highRiskWindow);
        assertTrue(highRiskAlert.getRiskScore() >= 80); // Should be high risk
        assertTrue(highRiskAlert.isHighRisk());
    }

    @Test
    void testTransactionValidation() {
        // Test transaction validation
        assertThrows(IllegalArgumentException.class, () -> {
            new Transaction(
                "TXN-TEST-005",
                "ACC-001",
                new BigDecimal("-100.00"), // Negative amount should fail
                "ES",
                "EUR",
                "PURCHASE",
                Instant.now(),
                "Test Merchant",
                "Invalid transaction"
            );
        });

        assertThrows(NullPointerException.class, () -> {
            new Transaction(
                null, // Null transaction ID should fail
                "ACC-001",
                new BigDecimal("100.00"),
                "ES",
                "EUR",
                "PURCHASE",
                Instant.now(),
                "Test Merchant",
                "Invalid transaction"
            );
        });
    }

    @Test
    void testWindowTimeValidation() {
        // Test window time validation
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(300);
        Instant windowEnd = now;

        AccountActivityWindow window = AccountActivityWindow.createNew("ACC-001", windowStart, windowEnd);

        // Transaction within window should be valid
        Transaction validTransaction = new Transaction(
            "TXN-TEST-006",
            "ACC-001",
            new BigDecimal("100.00"),
            "ES",
            "EUR",
            "PURCHASE",
            now.minusSeconds(150), // Within window
            "Test Merchant",
            "Valid transaction"
        );

        assertTrue(window.isTransactionInWindow(validTransaction));

        // Transaction outside window should be invalid
        Transaction invalidTransaction = new Transaction(
            "TXN-TEST-007",
            "ACC-001",
            new BigDecimal("100.00"),
            "ES",
            "EUR",
            "PURCHASE",
            now.minusSeconds(400), // Outside window
            "Test Merchant",
            "Invalid transaction"
        );

        assertFalse(window.isTransactionInWindow(invalidTransaction));
    }

    @Test
    void testSystemIntegration() {
        // Test that all components work together
        assertNotNull(fraudDetectionService);
        
        // Test that the service can detect fraud
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(300);
        Instant windowEnd = now;

        Set<String> countries = new HashSet<>();
        countries.add("ES");
        countries.add("FR");
        countries.add("DE");

        AccountActivityWindow window = new AccountActivityWindow(
            "ACC-001",
            windowStart,
            windowEnd,
            new BigDecimal("1200.00"),
            3,
            countries,
            now
        );

        boolean isFraud = fraudDetectionService.isFraudulent(window);
        assertTrue(isFraud);

        FraudAlert alert = fraudDetectionService.generateFraudAlert(window);
        assertNotNull(alert);
        assertTrue(alert.getRiskScore() > 0);
    }
} 