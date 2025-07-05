package com.fraudlens.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for FraudLens application
 * This class defines all custom properties to avoid IDE warnings
 */
@Configuration
@ConfigurationProperties(prefix = "fraudlens")
public class ApplicationProperties {

    private Demo demo = new Demo();
    private Fraud fraud = new Fraud();
    private Kafka kafka = new Kafka();

    public static class Demo {
        private boolean enabled = true;
        private int transactionInterval = 2000;
        private int fraudInterval = 15000;

        // Getters and setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getTransactionInterval() { return transactionInterval; }
        public void setTransactionInterval(int transactionInterval) { this.transactionInterval = transactionInterval; }
        public int getFraudInterval() { return fraudInterval; }
        public void setFraudInterval(int fraudInterval) { this.fraudInterval = fraudInterval; }
    }

    public static class Fraud {
        private double amountThreshold = 1000.00;
        private int countryThreshold = 3;
        private int windowSizeMinutes = 5;

        // Getters and setters
        public double getAmountThreshold() { return amountThreshold; }
        public void setAmountThreshold(double amountThreshold) { this.amountThreshold = amountThreshold; }
        public int getCountryThreshold() { return countryThreshold; }
        public void setCountryThreshold(int countryThreshold) { this.countryThreshold = countryThreshold; }
        public int getWindowSizeMinutes() { return windowSizeMinutes; }
        public void setWindowSizeMinutes(int windowSizeMinutes) { this.windowSizeMinutes = windowSizeMinutes; }
    }

    public static class Kafka {
        private String applicationId = "fraudlens-app";

        // Getters and setters
        public String getApplicationId() { return applicationId; }
        public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    }

    // Main getters
    public Demo getDemo() { return demo; }
    public void setDemo(Demo demo) { this.demo = demo; }
    public Fraud getFraud() { return fraud; }
    public void setFraud(Fraud fraud) { this.fraud = fraud; }
    public Kafka getKafka() { return kafka; }
    public void setKafka(Kafka kafka) { this.kafka = kafka; }
} 