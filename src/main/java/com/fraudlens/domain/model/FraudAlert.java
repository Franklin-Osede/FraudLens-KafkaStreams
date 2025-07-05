package com.fraudlens.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public class FraudAlert {
    
    @JsonProperty("alertId")
    private final String alertId;
    
    @JsonProperty("accountId")
    private final String accountId;
    
    @JsonProperty("alertType")
    private final String alertType;
    
    @JsonProperty("totalAmount")
    private final BigDecimal totalAmount;
    
    @JsonProperty("countriesInvolved")
    private final Set<String> countriesInvolved;
    
    @JsonProperty("transactionCount")
    private final int transactionCount;
    
    @JsonProperty("windowStart")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private final Instant windowStart;
    
    @JsonProperty("windowEnd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private final Instant windowEnd;
    
    @JsonProperty("alertTimestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private final Instant alertTimestamp;
    
    @JsonProperty("riskScore")
    private final int riskScore;
    
    @JsonProperty("description")
    private final String description;

    public FraudAlert(String alertId, String accountId, String alertType, 
                     BigDecimal totalAmount, Set<String> countriesInvolved, 
                     int transactionCount, Instant windowStart, Instant windowEnd,
                     Instant alertTimestamp, int riskScore, String description) {
        this.alertId = Objects.requireNonNull(alertId, "Alert ID cannot be null");
        this.accountId = Objects.requireNonNull(accountId, "Account ID cannot be null");
        this.alertType = Objects.requireNonNull(alertType, "Alert type cannot be null");
        this.totalAmount = Objects.requireNonNull(totalAmount, "Total amount cannot be null");
        this.countriesInvolved = Objects.requireNonNull(countriesInvolved, "Countries involved cannot be null");
        this.transactionCount = transactionCount;
        this.windowStart = Objects.requireNonNull(windowStart, "Window start cannot be null");
        this.windowEnd = Objects.requireNonNull(windowEnd, "Window end cannot be null");
        this.alertTimestamp = Objects.requireNonNull(alertTimestamp, "Alert timestamp cannot be null");
        this.riskScore = riskScore;
        this.description = description;
    }

    // Constructor sin argumentos para Jackson
    public FraudAlert() {
        this.alertId = null;
        this.accountId = null;
        this.alertType = null;
        this.totalAmount = null;
        this.countriesInvolved = null;
        this.transactionCount = 0;
        this.windowStart = null;
        this.windowEnd = null;
        this.alertTimestamp = null;
        this.riskScore = 0;
        this.description = null;
    }

    // Métodos de dominio
    public boolean isHighRisk() {
        return riskScore >= 80;
    }

    public boolean isCritical() {
        return riskScore >= 95;
    }

    public int getCountryCount() {
        return countriesInvolved != null ? countriesInvolved.size() : 0;
    }

    public boolean exceedsAmountThreshold(BigDecimal threshold) {
        return totalAmount != null && totalAmount.compareTo(threshold) >= 0;
    }

    // Getters
    public String getAlertId() { return alertId; }
    public String getAccountId() { return accountId; }
    public String getAlertType() { return alertType; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public Set<String> getCountriesInvolved() { return countriesInvolved; }
    public int getTransactionCount() { return transactionCount; }
    public Instant getWindowStart() { return windowStart; }
    public Instant getWindowEnd() { return windowEnd; }
    public Instant getAlertTimestamp() { return alertTimestamp; }
    public int getRiskScore() { return riskScore; }
    public String getDescription() { return description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FraudAlert that = (FraudAlert) o;
        return Objects.equals(alertId, that.alertId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alertId);
    }

    @Override
    public String toString() {
        return "FraudAlert{" +
                "alertId='" + alertId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", alertType='" + alertType + '\'' +
                ", totalAmount=" + totalAmount +
                ", countriesInvolved=" + countriesInvolved +
                ", transactionCount=" + transactionCount +
                ", windowStart=" + windowStart +
                ", windowEnd=" + windowEnd +
                ", alertTimestamp=" + alertTimestamp +
                ", riskScore=" + riskScore +
                ", description='" + description + '\'' +
                '}';
    }
} 