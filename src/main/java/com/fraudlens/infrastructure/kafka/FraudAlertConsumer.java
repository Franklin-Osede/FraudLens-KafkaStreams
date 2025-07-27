package com.fraudlens.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fraudlens.domain.model.FraudAlert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class FraudAlertConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(FraudAlertConsumer.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final ObjectMapper objectMapper;

    public FraudAlertConsumer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @KafkaListener(topics = "fraud-alerts", groupId = "fraudlens-consumer-group")
    public void handleFraudAlert(@Payload String alertJson) {
        
        try {
            FraudAlert alert = objectMapper.readValue(alertJson, FraudAlert.class);
            
            logger.info("Received fraud alert: {} for account: {}", alert.getAlertId(), alert.getAccountId());
            
            // Get risk level based on score
            String riskLevel = getRiskLevel(alert.getRiskScore());
            String colorCode = getColorCode(riskLevel);
            
            String timestamp = LocalTime.now().format(FORMATTER);
            
            // Show colored alert in console
            System.out.println("\n" + colorCode + "🚨 FRAUD ALERT DETECTED 🚨" + "\u001B[0m");
            System.out.println(colorCode + "═══════════════════════════════════════════════════════════════" + "\u001B[0m");
            System.out.println(colorCode + "⏰ Time: " + timestamp + "\u001B[0m");
            System.out.println(colorCode + "🆔 Alert ID: " + alert.getAlertId() + "\u001B[0m");
            System.out.println(colorCode + "👤 Account: " + alert.getAccountId() + "\u001B[0m");
            System.out.println(colorCode + "💰 Amount: €" + alert.getTotalAmount() + "\u001B[0m");
            System.out.println(colorCode + "🌍 Countries: " + alert.getCountriesInvolved().size() + " (" + String.join(", ", alert.getCountriesInvolved()) + ")" + "\u001B[0m");
            System.out.println(colorCode + "📊 Transactions: " + alert.getTransactionCount() + "\u001B[0m");
            System.out.println(colorCode + "⚠️  Risk Score: " + alert.getRiskScore() + "/100 (" + riskLevel + ")" + "\u001B[0m");
            System.out.println(colorCode + "📝 Description: " + alert.getDescription() + "\u001B[0m");
            System.out.println(colorCode + "💡 Recommendation: " + getRecommendation(alert.getRiskScore()) + "\u001B[0m");
            System.out.println(colorCode + "═══════════════════════════════════════════════════════════════" + "\u001B[0m");
            
        } catch (Exception e) {
            logger.error("Error processing fraud alert: {}", e.getMessage());
            logger.error("Raw JSON: {}", alertJson);
        }
    }
    
    private String getRiskLevel(int riskScore) {
        if (riskScore >= 90) return "CRITICAL";
        if (riskScore >= 70) return "HIGH";
        if (riskScore >= 50) return "MEDIUM";
        return "LOW";
    }
    
    private String getColorCode(String riskLevel) {
        return switch (riskLevel) {
            case "CRITICAL" -> "\u001B[41m\u001B[37m"; // Red background, white text
            case "HIGH" -> "\u001B[43m\u001B[30m";     // Yellow background, black text
            case "MEDIUM" -> "\u001B[46m\u001B[30m";   // Cyan background, black text
            case "LOW" -> "\u001B[42m\u001B[30m";      // Green background, black text
            default -> "\u001B[0m";                    // Normal
        };
    }
    
    private String getRecommendation(int riskScore) {
        if (riskScore >= 90) return "IMMEDIATE ACTION REQUIRED - Block account and investigate";
        if (riskScore >= 70) return "HIGH PRIORITY - Contact customer and verify transactions";
        if (riskScore >= 50) return "REVIEW - Monitor account for additional suspicious activity";
        return "MONITOR - Log for future reference";
    }
} 