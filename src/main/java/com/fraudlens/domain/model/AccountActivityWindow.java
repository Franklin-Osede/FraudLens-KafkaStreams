package com.fraudlens.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class AccountActivityWindow {
    
    @JsonProperty("accountId")
    private final String accountId;
    
    @JsonProperty("windowStart")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private final Instant windowStart;
    
    @JsonProperty("windowEnd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private final Instant windowEnd;
    
    @JsonProperty("totalAmount")
    private final BigDecimal totalAmount;
    
    @JsonProperty("transactionCount")
    private final int transactionCount;
    
    @JsonProperty("countries")
    private final Set<String> countries;
    
    @JsonProperty("lastUpdated")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private final Instant lastUpdated;

    public AccountActivityWindow(String accountId, Instant windowStart, Instant windowEnd,
                               BigDecimal totalAmount, int transactionCount, 
                               Set<String> countries, Instant lastUpdated) {
        this.accountId = Objects.requireNonNull(accountId, "Account ID cannot be null");
        this.windowStart = Objects.requireNonNull(windowStart, "Window start cannot be null");
        this.windowEnd = Objects.requireNonNull(windowEnd, "Window end cannot be null");
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.transactionCount = transactionCount;
        this.countries = countries != null ? new HashSet<>(countries) : new HashSet<>();
        this.lastUpdated = Objects.requireNonNull(lastUpdated, "Last updated cannot be null");
    }

    // Constructor sin argumentos para Jackson
    public AccountActivityWindow() {
        this.accountId = null;
        this.windowStart = null;
        this.windowEnd = null;
        this.totalAmount = BigDecimal.ZERO;
        this.transactionCount = 0;
        this.countries = new HashSet<>();
        this.lastUpdated = null;
    }

    // Factory method para crear una ventana nueva
    public static AccountActivityWindow createNew(String accountId, Instant windowStart, Instant windowEnd) {
        return new AccountActivityWindow(accountId, windowStart, windowEnd, 
                                       BigDecimal.ZERO, 0, new HashSet<>(), Instant.now());
    }

    // Métodos de dominio
    public AccountActivityWindow addTransaction(Transaction transaction) {
        if (!isTransactionInWindow(transaction)) {
            throw new IllegalArgumentException("Transaction is not within the window period");
        }
        
        BigDecimal newTotal = totalAmount.add(transaction.getAmount());
        int newCount = transactionCount + 1;
        Set<String> newCountries = new HashSet<>(countries);
        newCountries.add(transaction.getCountry());
        
        return new AccountActivityWindow(accountId, windowStart, windowEnd, 
                                       newTotal, newCount, newCountries, Instant.now());
    }

    public boolean isTransactionInWindow(Transaction transaction) {
        if (transaction.getTimestamp() == null) {
            return false;
        }
        return !transaction.getTimestamp().isBefore(windowStart) && 
               !transaction.getTimestamp().isAfter(windowEnd);
    }

    public boolean isSuspiciousActivity() {
        return exceedsAmountThreshold() && hasMultipleCountries();
    }

    public boolean exceedsAmountThreshold() {
        return totalAmount.compareTo(new BigDecimal("1000.00")) >= 0;
    }

    public boolean hasMultipleCountries() {
        return countries.size() >= 3;
    }

    public int getCountryCount() {
        return countries.size();
    }

    public boolean isWindowExpired(Instant currentTime) {
        return currentTime.isAfter(windowEnd);
    }

    // Getters
    public String getAccountId() { return accountId; }
    public Instant getWindowStart() { return windowStart; }
    public Instant getWindowEnd() { return windowEnd; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public int getTransactionCount() { return transactionCount; }
    public Set<String> getCountries() { return new HashSet<>(countries); }
    public Instant getLastUpdated() { return lastUpdated; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountActivityWindow that = (AccountActivityWindow) o;
        return Objects.equals(accountId, that.accountId) &&
               Objects.equals(windowStart, that.windowStart) &&
               Objects.equals(windowEnd, that.windowEnd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, windowStart, windowEnd);
    }

    @Override
    public String toString() {
        return "AccountActivityWindow{" +
                "accountId='" + accountId + '\'' +
                ", windowStart=" + windowStart +
                ", windowEnd=" + windowEnd +
                ", totalAmount=" + totalAmount +
                ", transactionCount=" + transactionCount +
                ", countries=" + countries +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
} 