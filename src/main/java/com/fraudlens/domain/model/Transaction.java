package com.fraudlens.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class Transaction {
    
    @NotNull
    @JsonProperty("transactionId")
    private final String transactionId;
    
    @NotNull
    @JsonProperty("accountId")
    private final String accountId;
    
    @NotNull
    @Positive
    @JsonProperty("amount")
    private final BigDecimal amount;
    
    @NotNull
    @JsonProperty("country")
    private final String country;
    
    @NotNull
    @JsonProperty("currency")
    private final String currency;
    
    @NotNull
    @JsonProperty("transactionType")
    private final String transactionType;
    
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @JsonProperty("timestamp")
    private final Instant timestamp;
    
    @JsonProperty("merchantName")
    private final String merchantName;
    
    @JsonProperty("description")
    private final String description;

    // Constructor principal
    public Transaction(String transactionId, String accountId, BigDecimal amount, 
                      String country, String currency, String transactionType, 
                      Instant timestamp, String merchantName, String description) {
        this.transactionId = Objects.requireNonNull(transactionId, "Transaction ID cannot be null");
        this.accountId = Objects.requireNonNull(accountId, "Account ID cannot be null");
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null");
        this.country = Objects.requireNonNull(country, "Country cannot be null");
        this.currency = Objects.requireNonNull(currency, "Currency cannot be null");
        this.transactionType = Objects.requireNonNull(transactionType, "Transaction type cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        this.merchantName = merchantName;
        this.description = description;
        
        validateAmount();
    }

    // Constructor sin argumentos para Jackson
    public Transaction() {
        this.transactionId = null;
        this.accountId = null;
        this.amount = null;
        this.country = null;
        this.currency = null;
        this.transactionType = null;
        this.timestamp = null;
        this.merchantName = null;
        this.description = null;
    }

    private void validateAmount() {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    // Métodos de dominio
    @JsonIgnore
    public boolean isHighValue() {
        return amount != null && amount.compareTo(new BigDecimal("1000.00")) >= 0;
    }

    @JsonIgnore
    public boolean isFromCountry(String targetCountry) {
        return country != null && country.equalsIgnoreCase(targetCountry);
    }

    @JsonIgnore
    public boolean isWithinTimeWindow(Instant windowStart, Instant windowEnd) {
        return timestamp != null && 
               !timestamp.isBefore(windowStart) && 
               !timestamp.isAfter(windowEnd);
    }

    // Getters
    public String getTransactionId() { return transactionId; }
    public String getAccountId() { return accountId; }
    public BigDecimal getAmount() { return amount; }
    public String getCountry() { return country; }
    public String getCurrency() { return currency; }
    public String getTransactionType() { return transactionType; }
    public Instant getTimestamp() { return timestamp; }
    public String getMerchantName() { return merchantName; }
    public String getDescription() { return description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", amount=" + amount +
                ", country='" + country + '\'' +
                ", currency='" + currency + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", timestamp=" + timestamp +
                ", merchantName='" + merchantName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
} 