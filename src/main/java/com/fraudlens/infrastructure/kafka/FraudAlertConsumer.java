package com.fraudlens.infrastructure.kafka;

import com.fraudlens.domain.model.FraudAlert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Set;

@Service
public class FraudAlertConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(FraudAlertConsumer.class);
    
    // Colores ANSI para terminal
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_UNDERLINE = "\u001B[4m";
    
    // Caracteres especiales para hacer más visual
    private static final String ALERT_ICON = "🚨";
    private static final String MONEY_ICON = "💰";
    private static final String WORLD_ICON = "🌍";
    private static final String TIME_ICON = "⏰";
    private static final String SHIELD_ICON = "🛡️";
    private static final String DANGER_ICON = "⚠️";
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @KafkaListener(topics = "fraud-alerts", groupId = "fraudlens-consumer-group")
    public void handleFraudAlert(@Payload FraudAlert alert, 
                                @Header(KafkaHeaders.RECEIVED_TIMESTAMP) Long timestamp,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        logger.info("Received fraud alert: {} for account: {}", alert.getAlertId(), alert.getAccountId());
        
        // Determinar el color y nivel de alerta basado en el risk score
        String colorAndLevel = getAlertColorAndLevel(alert.getRiskScore());
        
        // Imprimir alerta con formato colorido
        printColorfulAlert(alert, colorAndLevel);
        
        // Log adicional para debugging
        logger.debug("Alert details: {}", alert);
    }
    
    private String getAlertColorAndLevel(int riskScore) {
        if (riskScore >= 95) {
            return ANSI_RED + ANSI_BOLD + "CRITICAL" + ANSI_RESET;
        } else if (riskScore >= 80) {
            return ANSI_YELLOW + ANSI_BOLD + "HIGH" + ANSI_RESET;
        } else if (riskScore >= 60) {
            return ANSI_CYAN + ANSI_BOLD + "MEDIUM" + ANSI_RESET;
        } else {
            return ANSI_GREEN + ANSI_BOLD + "LOW" + ANSI_RESET;
        }
    }
    
    private void printColorfulAlert(FraudAlert alert, String colorAndLevel) {
        System.out.println();
        System.out.println(ANSI_RED + ANSI_BOLD + "═══════════════════════════════════════════════════════════════════════════════" + ANSI_RESET);
        System.out.println(ANSI_RED + ANSI_BOLD + "                              " + ALERT_ICON + " FRAUD ALERT DETECTED " + ALERT_ICON + ANSI_RESET);
        System.out.println(ANSI_RED + ANSI_BOLD + "═══════════════════════════════════════════════════════════════════════════════" + ANSI_RESET);
        
        System.out.printf("%s%s Alert ID:%s %s%s%s%n", 
                         ANSI_PURPLE, ANSI_BOLD, ANSI_RESET, 
                         ANSI_WHITE, alert.getAlertId(), ANSI_RESET);
        
        System.out.printf("%s%s Account:%s %s%s%s%n", 
                         ANSI_BLUE, ANSI_BOLD, ANSI_RESET, 
                         ANSI_WHITE, alert.getAccountId(), ANSI_RESET);
        
        System.out.printf("%s%s Risk Level:%s %s%n", 
                         ANSI_YELLOW, ANSI_BOLD, ANSI_RESET, 
                         colorAndLevel);
        
        System.out.printf("%s%s Risk Score:%s %s%d/100%s%n", 
                         ANSI_CYAN, ANSI_BOLD, ANSI_RESET, 
                         ANSI_WHITE, alert.getRiskScore(), ANSI_RESET);
        
        System.out.printf("%s%s %s Total Amount:%s %s€%.2f%s%n", 
                         ANSI_GREEN, ANSI_BOLD, MONEY_ICON, ANSI_RESET, 
                         ANSI_WHITE, alert.getTotalAmount(), ANSI_RESET);
        
        System.out.printf("%s%s %s Countries:%s %s%d countries (%s)%s%n", 
                         ANSI_PURPLE, ANSI_BOLD, WORLD_ICON, ANSI_RESET, 
                         ANSI_WHITE, alert.getCountryCount(), 
                         formatCountries(alert.getCountriesInvolved()), ANSI_RESET);
        
        System.out.printf("%s%s Transactions:%s %s%d transactions%s%n", 
                         ANSI_BLUE, ANSI_BOLD, ANSI_RESET, 
                         ANSI_WHITE, alert.getTransactionCount(), ANSI_RESET);
        
        System.out.printf("%s%s %s Time Window:%s %s%s - %s%s%n", 
                         ANSI_YELLOW, ANSI_BOLD, TIME_ICON, ANSI_RESET, 
                         ANSI_WHITE, alert.getWindowStart().atZone(java.time.ZoneId.systemDefault()).format(FORMATTER),
                         alert.getWindowEnd().atZone(java.time.ZoneId.systemDefault()).format(FORMATTER), ANSI_RESET);
        
        System.out.printf("%s%s %s Alert Time:%s %s%s%s%n", 
                         ANSI_CYAN, ANSI_BOLD, TIME_ICON, ANSI_RESET, 
                         ANSI_WHITE, alert.getAlertTimestamp().atZone(java.time.ZoneId.systemDefault()).format(FORMATTER), ANSI_RESET);
        
        System.out.printf("%s%s %s Description:%s %s%s%s%n", 
                         ANSI_GREEN, ANSI_BOLD, SHIELD_ICON, ANSI_RESET, 
                         ANSI_WHITE, alert.getDescription(), ANSI_RESET);
        
        // Mostrar recomendaciones basadas en el risk score
        System.out.println();
        System.out.printf("%s%s %s RECOMMENDED ACTIONS:%s%n", 
                         ANSI_RED, ANSI_BOLD, DANGER_ICON, ANSI_RESET);
        
        if (alert.getRiskScore() >= 95) {
            System.out.printf("%s%s   • IMMEDIATE: Freeze account and contact customer%s%n", 
                             ANSI_RED, ANSI_BOLD, ANSI_RESET);
            System.out.printf("%s%s   • URGENT: Investigate all recent transactions%s%n", 
                             ANSI_RED, ANSI_BOLD, ANSI_RESET);
            System.out.printf("%s%s   • CRITICAL: Notify fraud department%s%n", 
                             ANSI_RED, ANSI_BOLD, ANSI_RESET);
        } else if (alert.getRiskScore() >= 80) {
            System.out.printf("%s%s   • Review account activity manually%s%n", 
                             ANSI_YELLOW, ANSI_BOLD, ANSI_RESET);
            System.out.printf("%s%s   • Consider temporary limits%s%n", 
                             ANSI_YELLOW, ANSI_BOLD, ANSI_RESET);
        } else {
            System.out.printf("%s%s   • Monitor account for further activity%s%n", 
                             ANSI_GREEN, ANSI_BOLD, ANSI_RESET);
        }
        
        System.out.println(ANSI_RED + ANSI_BOLD + "═══════════════════════════════════════════════════════════════════════════════" + ANSI_RESET);
        System.out.println();
    }
    
    private String formatCountries(Set<String> countries) {
        if (countries == null || countries.isEmpty()) {
            return "N/A";
        }
        return String.join(", ", countries);
    }
} 