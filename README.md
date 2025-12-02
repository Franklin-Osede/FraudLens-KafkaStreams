# FraudLens – Enterprise Real-Time Fraud Detection Platform

> **Enterprise-grade real-time fraud detection system** featuring Apache Kafka Streams, Spring Boot microservices, and Kubernetes-ready cloud infrastructure with exactly-once processing semantics.

[![Java](https://img.shields.io/badge/Java-17-orange?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen?style=flat&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.6.1-black?style=flat&logo=apache-kafka&logoColor=white)](https://kafka.apache.org/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.28-blue?style=flat&logo=kubernetes&logoColor=white)](https://kubernetes.io/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-red?style=flat&logo=apache-maven&logoColor=white)](https://maven.apache.org/)

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Key Features](#key-features)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [Development](#development)
- [Deployment](#deployment)
- [Monitoring & Observability](#monitoring--observability)
- [Configuration](#configuration)
- [Testing](#testing)
- [Documentation](#documentation)
- [License](#license)

---

## Overview

FraudLens is a production-ready real-time fraud detection platform designed for financial institutions and payment processors. The system processes transaction streams using Apache Kafka Streams with exactly-once semantics, providing bank-grade reliability and sub-100ms fraud detection latency.

### System Components

- **Transaction Producer**: Generates demo transactions and fraud scenarios for testing
- **Kafka Streams Processor**: Real-time fraud detection using sliding time windows
- **Fraud Alert Consumer**: Processes and displays color-coded fraud alerts
- **Domain Models**: Rich business logic with validation and immutability
- **REST API**: Demo endpoints for testing, monitoring, and manual scenario generation

### Key Features

- **Real-Time Processing**: Sub-100ms fraud detection latency with sliding window aggregation
- **Exactly-Once Semantics V2**: Guarantees no duplicate processing or message loss
- **Stateful Aggregation**: Maintains account activity across transactions in 5-minute windows
- **Configurable Thresholds**: Amount and country-based fraud detection rules
- **Risk Scoring**: 0-100 risk score calculation based on amount, countries, and transaction count
- **Horizontal Scaling**: Kafka Streams enables distributed processing across multiple instances
- **KRaft Mode**: Modern Kafka deployment without Zookeeper dependency
- **Production Ready**: Kubernetes deployment with auto-scaling and monitoring

---

## Architecture

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│              Transaction Sources                             │
│    (Payment Gateways, Banking Systems, APIs)                │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│              Apache Kafka (KRaft Mode)                      │
│         Topic: transactions (Partitioned)                   │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│         Kafka Streams Processing Pipeline                   │
│  ┌────────────────────────────────────────────────────┐    │
│  │ 1. Group by Account ID                             │    │
│  │ 2. Sliding Window (5 min + 1 min grace)            │    │
│  │ 3. Aggregate: Amount, Countries, Transaction Count│    │
│  │ 4. Fraud Detection Logic                           │    │
│  │ 5. Risk Score Calculation                           │    │
│  └───────────────────────┬────────────────────────────┘    │
└───────────────────────────┼─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              Apache Kafka                                   │
│         Topic: fraud-alerts                                 │
└───────────────────────┬─────────────────────────────────────┘
                        │
        ┌───────────────┴───────────────┐
        │                               │
        ▼                               ▼
┌──────────────────┐        ┌──────────────────────┐
│ Fraud Alert      │        │ REST API             │
│ Consumer         │        │ /api/demo/*          │
│ (Console Output) │        │ (Monitoring)         │
└──────────────────┘        └──────────────────────┘
```

### Technology Stack

**Core Framework:**
- Spring Boot 3.2.1
- Java 17+
- Maven 3.6+

**Stream Processing:**
- Apache Kafka 3.6.1 (KRaft mode)
- Kafka Streams (Exactly-Once Semantics V2)
- Custom JSON Serdes

**Infrastructure:**
- Docker + Docker Compose
- Kubernetes (for production)
- Prometheus + Grafana (monitoring)

**Development:**
- Spring Boot Actuator (health checks, metrics)
- SLF4J + Logback (structured logging)
- JUnit 5 (testing)

---

## Project Structure

```
fraudlens-kafka-streams/
├── src/
│   ├── main/java/com/fraudlens/
│   │   ├── config/                    # Configuration classes
│   │   │   ├── ApplicationProperties.java
│   │   │   └── KafkaConfig.java
│   │   ├── controller/                # REST API controllers
│   │   │   └── DemoController.java
│   │   ├── domain/                     # Domain layer (DDD)
│   │   │   ├── model/                 # Business entities
│   │   │   │   ├── Transaction.java
│   │   │   │   ├── AccountActivityWindow.java
│   │   │   │   └── FraudAlert.java
│   │   │   └── service/               # Business logic
│   │   │       └── FraudDetectionService.java
│   │   ├── infrastructure/            # External integrations
│   │   │   ├── kafka/                 # Kafka components
│   │   │   │   ├── TransactionProducer.java
│   │   │   │   ├── FraudDetectionProcessor.java
│   │   │   │   └── FraudAlertConsumer.java
│   │   │   └── serde/                 # Serialization
│   │   │       └── JsonSerde.java
│   │   └── FraudLensApplication.java  # Main application
│   └── resources/
│       └── application.properties     # Configuration
│
├── k8s/production/                     # Kubernetes manifests
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── deployment.yaml
│   └── ingress.yaml
│
├── grafana/dashboards/                 # Grafana dashboards
│   ├── fraud-detection-overview.json
│   ├── system-performance.json
│   └── business-intelligence.json
│
├── monitoring/                         # Prometheus configuration
│   └── prometheus-config.yaml
│
├── scripts/                           # Automation scripts
│   ├── start-kafka-kraft.sh
│   └── compile-clean.sh
│
├── kafka-config/                       # Kafka configuration
│   └── server.properties
│
├── Dockerfile.jvm                     # JVM Docker image
├── Dockerfile.native                  # Native image (Quarkus)
├── pom.xml                            # Maven dependencies
├── deploy.sh                          # Deployment script
└── README.md                          # This file
```

---

## Key Features

### Real-Time Processing

- **Sliding Windows**: 5-minute windows with 1-minute grace period for late-arriving events
- **Stateful Aggregation**: Maintains account activity state across transactions
- **Exactly-Once Semantics V2**: Guarantees no duplicate processing or message loss
- **Sub-100ms Latency**: Real-time fraud detection with minimal processing delay

### Fraud Detection Logic

- **Amount Threshold**: Detects transactions totaling €1,000 or more
- **Country Threshold**: Flags activity across 3 or more different countries
- **Risk Scoring**: Calculates 0-100 risk score based on:
  - Base score: 50 points
  - Amount multiplier: up to 30 points
  - Country diversity: up to 20 points
  - Transaction count: up to 10 points
- **Real-Time Alerts**: Immediate notification of suspicious activity

### Bank-Grade Reliability

- **Idempotent Producers**: Prevents duplicate message generation
- **Manual Offset Management**: Full control over message consumption
- **Comprehensive Error Handling**: Graceful failure recovery with logging
- **High Availability**: Horizontal scaling ready with Kafka Streams

---

## Getting Started

### Prerequisites

- **Java**: JDK 17 or higher
- **Maven**: 3.6 or higher
- **Docker**: Optional, for containerized Kafka deployment
- **Kubernetes**: Optional, for production deployment

### Installation

```bash
# Clone the repository
git clone <repository-url>
cd FraudLens-KafkaStreams

# Build the project
mvn clean install
```

### Local Development Setup

**1. Start Kafka (KRaft Mode)**

```bash
# Using the provided script
./scripts/start-kafka-kraft.sh

# Or using Docker Compose
docker-compose up -d
```

**2. Start the Application**

```bash
# Run with Spring Boot
mvn spring-boot:run

# Or run the compiled JAR
java -jar target/fraudlens-kafka-streams-1.0.0.jar
```

**3. Verify System Status**

```bash
# Check system status
curl http://localhost:8081/api/demo/status
```

### Demo Scenarios

The application automatically generates:
- **Normal transactions**: Every 2 seconds
- **Suspicious activity**: Every 15 seconds (4 transactions across different countries)

**Manual Testing:**

```bash
# Generate fraud scenario for specific account
curl -X POST http://localhost:8081/api/demo/fraud/ACC-001

# Generate normal transactions
curl -X POST http://localhost:8081/api/demo/normal/10

# Check system status
curl http://localhost:8081/api/demo/status
```

---

## Development

### Project Architecture

The system follows **Domain-Driven Design (DDD)** principles:

- **Domain Layer**: Business entities (`Transaction`, `AccountActivityWindow`, `FraudAlert`) with rich business logic
- **Infrastructure Layer**: External integrations (Kafka producers, consumers, serialization)
- **Application Layer**: Use cases and services (`FraudDetectionService`)
- **Presentation Layer**: REST controllers (`DemoController`)

### Kafka Streams Pipeline

The fraud detection pipeline processes transactions in real-time:

1. **Input Stream**: Consumes from `transactions` topic
2. **Grouping**: Groups transactions by `accountId`
3. **Windowing**: Applies 5-minute sliding windows with 1-minute grace period
4. **Aggregation**: Calculates total amount, unique countries, and transaction count
5. **Fraud Detection**: Applies business rules (amount ≥ €1,000 AND countries ≥ 3)
6. **Alert Generation**: Creates `FraudAlert` with risk score
7. **Output Stream**: Publishes to `fraud-alerts` topic

### Configuration

**Application Properties** (`src/main/resources/application.properties`):

```properties
# Server
server.port=8081

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
fraudlens.kafka.application-id=fraudlens-app

# Fraud Detection Thresholds
fraudlens.fraud.amount-threshold=1000.00
fraudlens.fraud.country-threshold=3
fraudlens.fraud.window-size-minutes=5

# Demo Configuration
fraudlens.demo.transaction-interval=2000
fraudlens.demo.fraud-interval=15000
```

**Kafka Streams Configuration** (Exactly-Once Semantics V2):

```java
props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10000);
props.put(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, 10 * 1024 * 1024);
```

---

## Deployment

### Docker Deployment

**Build Docker Image:**

```bash
# JVM image
docker build -f Dockerfile.jvm -t fraudlens:latest .

# Native image (Quarkus)
docker build -f Dockerfile.native -t fraudlens:native .
```

**Run Container:**

```bash
docker run -p 8081:8081 \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  fraudlens:latest
```

### Kubernetes Deployment

**Quick Deploy:**

```bash
# Run deployment script
./deploy.sh
```

**Manual Deployment:**

```bash
# Create namespace
kubectl apply -f k8s/production/namespace.yaml

# Apply configuration
kubectl apply -f k8s/production/configmap.yaml

# Deploy application
kubectl apply -f k8s/production/deployment.yaml

# Configure ingress
kubectl apply -f k8s/production/ingress.yaml
```

**Auto-Scaling Configuration:**

The deployment includes a HorizontalPodAutoscaler (HPA):
- **Min replicas**: 3
- **Max replicas**: 10
- **CPU threshold**: 70% utilization
- **Memory threshold**: 80% utilization

### Production Considerations

- **Resource Limits**: Configured per pod (CPU: 250m-500m, Memory: 256Mi-512Mi)
- **Health Checks**: Liveness, readiness, and startup probes configured
- **Graceful Shutdown**: 30-second termination grace period
- **Monitoring**: Prometheus metrics exposed at `/actuator/metrics`

---

## Monitoring & Observability

### Grafana Dashboards

Three specialized dashboards are included:

1. **Fraud Detection Overview**
   - Transactions processed in real-time
   - Frauds detected per hour/day
   - Average detection latency
   - System throughput
   - Most active countries in frauds

2. **System Performance**
   - CPU and memory per instance
   - Kafka lag (processing delay)
   - Error rate
   - API response times
   - JVM metrics (GC, heap, threads)

3. **Business Intelligence**
   - Total amounts processed
   - Geographic distribution of transactions
   - Fraud patterns by time of day
   - Detection effectiveness (precision/recall)
   - Infrastructure costs

### Prometheus Metrics

Key metrics exposed:
- `fraudlens_transactions_processed_total`
- `fraudlens_frauds_detected_total`
- `fraudlens_detection_latency_seconds`
- `fraudlens_risk_score_distribution`

### Logging

Structured logging with SLF4J:
- **DEBUG**: Detailed transaction processing
- **INFO**: Fraud alerts and system events
- **WARN**: Suspicious activity detection
- **ERROR**: System errors and exceptions

Log files: `logs/fraudlens.log` (with rotation)

---

## Configuration

### Fraud Detection Thresholds

Configure fraud detection rules in `application.properties`:

```properties
# Amount threshold (default: €1,000)
fraudlens.fraud.amount-threshold=1000.00

# Country threshold (default: 3 countries)
fraudlens.fraud.country-threshold=3

# Window size in minutes (default: 5 minutes)
fraudlens.fraud.window-size-minutes=5
```

### Kafka Configuration

```properties
# Bootstrap servers
spring.kafka.bootstrap-servers=localhost:9092

# Application ID (must be unique per Kafka Streams application)
fraudlens.kafka.application-id=fraudlens-app

# Consumer group
fraudlens.kafka.consumer.group-id=fraudlens-consumer-group
```

### Demo Mode

```properties
# Enable/disable demo mode
fraudlens.demo.enabled=true

# Normal transaction interval (milliseconds)
fraudlens.demo.transaction-interval=2000

# Fraud scenario interval (milliseconds)
fraudlens.demo.fraud-interval=15000
```

---

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=FraudLensApplicationTests
```

### Test Strategy

- **Unit Tests**: Domain entities and business logic
- **Integration Tests**: Kafka Streams topology and processors
- **Component Tests**: REST API endpoints

### Test Coverage

Key areas covered:
- Fraud detection logic
- Risk score calculation
- Transaction aggregation
- Window processing
- Error handling

---

## Documentation

### Technical Documentation

- **[Architecture Explanation](./ARCHITECTURE_EXPLANATION.md)**: Detailed architecture and design decisions
- **[Deployment Guide](./DEPLOYMENT_GUIDE.md)**: Complete deployment instructions
- **[Simple Workflow](./SIMPLE_WORKFLOW.md)**: Quick start guide

### API Documentation

**REST Endpoints:**

- `GET /api/demo/status` - System status and configuration
- `POST /api/demo/fraud/{accountId}` - Generate fraud scenario for account
- `POST /api/demo/normal/{count}` - Generate normal transactions

**Actuator Endpoints:**

- `GET /actuator/health` - Health check
- `GET /actuator/info` - Application information
- `GET /actuator/metrics` - Application metrics

---

## License

**Private** – FraudLens

All rights reserved. This software is proprietary and confidential.

---

## Support

For technical support or questions:

- **Documentation**: See project documentation files
- **Logs**: Check `logs/fraudlens.log` for application logs
- **Monitoring**: Access Grafana dashboards for system metrics
- **Kafka Logs**: Check `kafka-logs/kafka.log` for Kafka broker logs

---

**Built for enterprise fraud detection with real-time stream processing**
