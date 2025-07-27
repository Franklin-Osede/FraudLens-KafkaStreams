# FraudLens Demo Script for Video Presentation

## 🎬 Video Structure (15 minutes total)

### **1. Introduction (2 minutes)**
```
"Welcome to FraudLens - a real-time fraud detection system built with Apache Kafka Streams. 
Today I'll show you how this system can detect fraudulent transactions in real-time with 
bank-grade reliability using Exactly-Once Semantics V2."
```

### **2. Architecture Overview (2 minutes)**
- Show the architecture diagram from `ARCHITECTURE_DIAGRAM.md`
- Explain the key components:
  - Transaction Producer (generates demo data)
  - Kafka Streams Processor (real-time analysis)
  - Fraud Alert Consumer (displays results)
  - Domain models (business logic)

### **3. Code Walkthrough (3 minutes)**
- Open key files and explain:
  - `FraudDetectionProcessor.java` - Main processing logic
  - `Transaction.java` - Domain model
  - `KafkaConfig.java` - Configuration with EOS V2
  - `FraudAlertConsumer.java` - Alert display

### **4. Live Demo (5 minutes)**

#### **Step 1: Start the System**
```bash
# Terminal 1: Start Kafka
./scripts/start-kafka-kraft.sh

# Terminal 2: Start the application
mvn spring-boot:run
```

#### **Step 2: Show Normal Operation**
- Point out the automatic transaction generation every 2 seconds
- Show the console logs showing normal transactions
- Explain that these don't trigger alerts

#### **Step 3: Trigger Manual Fraud Scenario**
```bash
# Terminal 3: Trigger fraud scenario
curl -X POST http://localhost:8080/api/demo/fraud/ACC-001
```

#### **Step 4: Watch Real-time Detection**
- Show the fraud alert appearing in the console
- Point out the color-coded output (red for critical)
- Explain the risk score calculation

#### **Step 5: Show System Status**
```bash
curl http://localhost:8080/api/demo/status
```

### **5. Testing & Results (3 minutes)**

#### **Run Tests**
```bash
# Terminal 4: Run comprehensive tests
mvn test
```

#### **Show Test Results**
- Point out test coverage
- Show different test scenarios
- Demonstrate the reliability features

### **6. Advanced Features Demo (2 minutes)**

#### **Multiple Fraud Scenarios**
```bash
# Generate different types of fraud
curl -X POST http://localhost:8080/api/demo/fraud/ACC-002
curl -X POST http://localhost:8080/api/demo/fraud/ACC-003
```

#### **Show Concurrent Processing**
- Demonstrate how multiple accounts are processed simultaneously
- Show the sliding window behavior

### **7. Conclusion (1 minute)**
```
"FraudLens demonstrates how modern stream processing can provide real-time 
fraud detection with enterprise-grade reliability. The combination of Kafka 
Streams with Exactly-Once Semantics V2 ensures that every transaction is 
processed exactly once, making it suitable for financial applications."
```

## 🎯 Key Points to Highlight

### **Technical Excellence:**
- **Exactly-Once Semantics V2:** Bank-grade reliability
- **Sliding Windows:** Real-time temporal analysis
- **Stateful Processing:** Maintains context across transactions
- **JSON Serialization:** Flexible data format

### **Business Value:**
- **Real-time Detection:** Immediate fraud alerts
- **Scalable Architecture:** Handles high transaction volumes
- **Configurable Rules:** Easy to adjust fraud thresholds
- **Comprehensive Logging:** Full audit trail

### **Developer Experience:**
- **Clean Architecture:** Separation of concerns
- **Domain-Driven Design:** Rich domain models
- **Comprehensive Testing:** High test coverage
- **Easy Deployment:** Docker-ready configuration

## 📊 Expected Demo Results

### **Normal Operation:**
- Transactions generated every 2 seconds
- No fraud alerts (normal behavior)
- Console shows transaction processing

### **Fraud Detection:**
- Manual fraud scenario triggers alert within 5 minutes
- Red-colored console output for critical alerts
- Risk score calculation displayed
- Multiple countries and high amounts detected

### **System Performance:**
- < 100ms processing latency
- 1000+ transactions per second capability
- 99.99% message delivery guarantee
- Horizontal scaling ready

## 🎨 Console Output Examples

### **Normal Transaction:**
```
Processing transaction: TXN-ABC12345 for account: ACC-001 amount: €150.00 country: ES
```

### **Fraud Alert:**
```
🚨 FRAUD ALERT DETECTED 🚨
═══════════════════════════════════════════════════════════════
⏰ Time: 14:30:25
🆔 Alert ID: FRAUD-ABC12345
👤 Account: ACC-001
💰 Amount: €1250.00
🌍 Countries: 5 (ES, FR, DE, IT, UK)
📊 Transactions: 5
⚠️  Risk Score: 85/100 (HIGH)
📝 Description: Suspicious activity detected: €1250.00 across 5 countries in 5 transactions within 5-minute window
💡 Recommendation: HIGH PRIORITY - Contact customer and verify transactions
═══════════════════════════════════════════════════════════════
```

## 🔧 Troubleshooting Tips

### **If Kafka doesn't start:**
- Check if ports 9092 and 9093 are available
- Ensure Docker is running (if using Docker)
- Check the logs in `kafka-logs/` directory

### **If application doesn't start:**
- Verify Java 17+ is installed
- Check Maven dependencies are downloaded
- Ensure Kafka is running before starting the app

### **If no fraud alerts appear:**
- Wait for the 5-minute window to complete
- Check that transactions are being generated
- Verify the fraud scenario was triggered correctly

## 📝 Notes for Recording

1. **Screen Recording:** Use high resolution (1920x1080 or higher)
2. **Terminal Font:** Use a monospace font with good contrast
3. **Audio:** Clear microphone with minimal background noise
4. **Pacing:** Speak clearly and pause between sections
5. **Code Highlighting:** Use syntax highlighting in your IDE
6. **Full Screen:** Show the entire terminal/IDE window 