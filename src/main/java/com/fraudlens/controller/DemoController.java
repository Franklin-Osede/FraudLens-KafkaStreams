package com.fraudlens.controller;

import com.fraudlens.infrastructure.kafka.TransactionProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);

    @Autowired
    private TransactionProducer transactionProducer;

    @PostMapping("/fraud/{accountId}")
    public ResponseEntity<Map<String, Object>> generateFraudScenario(@PathVariable String accountId) {
        logger.info("🎯 Manual fraud scenario requested for account: {}", accountId);
        
        try {
            transactionProducer.generateFraudScenario(accountId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Fraud scenario generated successfully",
                "accountId", accountId,
                "description", "5 transactions generated across different countries totaling €1,250",
                "expectedAlert", "Should trigger fraud alert within 5 minutes"
            ));
        } catch (Exception e) {
            logger.error("Error generating fraud scenario for account {}: {}", accountId, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to generate fraud scenario",
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/normal/{count}")
    public ResponseEntity<Map<String, Object>> generateNormalTransactions(@PathVariable int count) {
        logger.info("🔄 Generating {} normal transactions", count);
        
        try {
            for (int i = 0; i < count; i++) {
                transactionProducer.generateNormalTransaction();
                Thread.sleep(100); // Small delay between transactions
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Normal transactions generated successfully",
                "count", count,
                "description", "Regular transactions for demonstration"
            ));
        } catch (Exception e) {
            logger.error("Error generating normal transactions: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to generate normal transactions",
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        return ResponseEntity.ok(Map.of(
            "system", "FraudLens",
            "status", "running",
            "description", "Real-time fraud detection system",
            "features", Map.of(
                "kafka", "KRaft enabled",
                "streams", "Exactly-Once Semantics V2",
                "windowSize", "5 minutes",
                "fraudThreshold", "€1,000 + 3 countries"
            ),
            "endpoints", Map.of(
                "generateFraud", "POST /api/demo/fraud/{accountId}",
                "generateNormal", "POST /api/demo/normal/{count}",
                "systemStatus", "GET /api/demo/status"
            )
        ));
    }
} 