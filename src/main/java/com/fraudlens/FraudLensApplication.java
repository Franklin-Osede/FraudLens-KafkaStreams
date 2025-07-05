package com.fraudlens;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableKafkaStreams
@EnableAsync
@EnableScheduling
public class FraudLensApplication {

    public static void main(String[] args) {
        System.out.println("🔍 FraudLens - Real-time Fraud Detection System");
        System.out.println("🚀 Starting Kafka Streams processing...");
        SpringApplication.run(FraudLensApplication.class, args);
    }
} 