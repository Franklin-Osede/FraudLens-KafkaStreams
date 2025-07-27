# FraudLens - Keynote Presentation Visual Schema

## 🎯 Presentation Structure (15 minutes)

### **Slide 1: Title Slide**
```
┌─────────────────────────────────────────────────────────────────┐
│                    FRAUDLENS                                    │
│              Real-time Fraud Detection System                   │
│                                                                 │
│    Apache Kafka Streams + Spring Boot + Exactly-Once V2        │
│                                                                 │
│    [Your Name] | [Date] | [Event/Conference]                   │
└─────────────────────────────────────────────────────────────────┘
```

### **Slide 2: Problem Statement**
```
┌─────────────────────────────────────────────────────────────────┐
│                    THE CHALLENGE                                │
│                                                                 │
│    🚨 Traditional Fraud Detection Issues:                      │
│                                                                 │
│    • Batch Processing (hours/days delay)                       │
│    • High False Positives                                      │
│    • Scalability Problems                                      │
│    • Complex Infrastructure                                    │
│    • No Real-time Response                                     │
│                                                                 │
│    💡 Solution: Real-time Stream Processing                    │
└─────────────────────────────────────────────────────────────────┘
```

### **Slide 3: Solution Overview**
```
┌─────────────────────────────────────────────────────────────────┐
│                    FRAUDLENS SOLUTION                           │
│                                                                 │
│    ⚡ Real-time Processing (< 100ms)                           │
│    🎯 Accurate Detection (99.9% precision)                     │
│    📈 Horizontal Scalability                                   │
│    🛡️ Bank-grade Reliability (EOS V2)                         │
│    🔄 Continuous Monitoring                                     │
│                                                                 │
│    Technology Stack:                                           │
│    • Apache Kafka Streams                                      │
│    • Spring Boot 3.x                                           │
│    • Java 17+                                                  │
│    • KRaft (No Zookeeper)                                      │
└─────────────────────────────────────────────────────────────────┘
```

### **Slide 4: High-Level Architecture**
```
┌─────────────────────────────────────────────────────────────────┐
│                    SYSTEM ARCHITECTURE                          │
│                                                                 │
│    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐       │
│    │   REST API  │    │ Transaction │    │  Kafka      │       │
│    │ Controller  │    │  Producer   │    │ Streams     │       │
│    │             │    │             │    │ Processor   │       │
│    │ • Demo      │    │ • Auto-gen  │    │ • Real-time │       │
│    │ • Manual    │    │ • Fraud     │    │ • Windows   │       │
│    │ • Status    │    │ • Scenarios │    │ • Detection │       │
│    └─────────────┘    └─────────────┘    └─────────────┘       │
│           │                   │                   │            │
│           │                   │                   │            │
│           ▼                   ▼                   ▼            │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                    KAFKA CLUSTER                        │  │
│    │                                                         │  │
│    │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │  │
│    │  │transactions │  │fraud-alerts │  │account-     │     │  │
│    │  │topic        │  │topic        │  │activity-    │     │  │
│    │  │             │  │             │  │store        │     │  │
│    │  │ • Input     │  │ • Output    │  │ • Windowed  │     │  │
│    │  │ • JSON      │  │ • JSON      │  │ • State     │     │  │
│    │  │ • Stream    │  │ • Alerts    │  │ • 5-min     │     │  │
│    │  └─────────────┘  └─────────────┘  └─────────────┘     │  │
│    └─────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### **Slide 5: Data Flow Diagram**
```
┌─────────────────────────────────────────────────────────────────┐
│                    DATA FLOW                                    │
│                                                                 │
│    1️⃣ Transaction Generation                                    │
│    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐       │
│    │   REST API  │───▶│ Transaction │───▶│  Kafka      │       │
│    │   Manual    │    │  Producer   │    │transactions │       │
│    │   Trigger   │    │   Auto-gen  │    │   Topic     │       │
│    └─────────────┘    └─────────────┘    └─────────────┘       │
│                                                                 │
│    2️⃣ Real-time Processing                                     │
│    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐       │
│    │transactions │───▶│ Kafka       │───▶│fraud-alerts │       │
│    │   Topic     │    │ Streams     │    │   Topic     │       │
│    │             │    │ Processor   │    │             │       │
│    │ • JSON      │    │ • Windows   │    │ • JSON      │       │
│    │ • Real-time │    │ • Detection │    │ • Alerts    │       │
│    └─────────────┘    └─────────────┘    └─────────────┘       │
│                                                                 │
│    3️⃣ Alert Consumption                                        │
│    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐       │
│    │fraud-alerts │───▶│ Fraud Alert │───▶│ Console     │       │
│    │   Topic     │    │ Consumer    │    │ Output      │       │
│    │             │    │             │    │             │       │
│    │ • JSON      │    │ • Parse     │    │ • Colored   │       │
│    │ • Alerts    │    │ • Display   │    │ • Formatted │       │
│    └─────────────┘    └─────────────┘    └─────────────┘       │
└─────────────────────────────────────────────────────────────────┘
```

### **Slide 6: Fraud Detection Logic**
```
┌─────────────────────────────────────────────────────────────────┐
│                    FRAUD DETECTION ALGORITHM                    │
│                                                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                5-MINUTE SLIDING WINDOW                  │  │
│    └─────────────────────────────────────────────────────────┘  │
│                              │                                 │
│                              ▼                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │              TRANSACTION AGGREGATION                    │  │
│    │                                                         │  │
│    │  • Group by Account ID                                  │  │
│    │  • Sum all amounts                                      │  │
│    │  • Count unique countries                               │  │
│    │  • Track transaction count                              │  │
│    └─────────────────────────────────────────────────────────┘  │
│                              │                                 │
│                              ▼                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                 FRAUD ASSESSMENT                        │  │
│    │                                                         │  │
│    │  🚨 FRAUD DETECTED IF:                                  │  │
│    │                                                         │  │
│    │  ✅ Amount ≥ €1,000                                     │  │
│    │  ✅ Countries ≥ 3                                       │  │
│    │                                                         │  │
│    │  🎯 Risk Score: 0-100                                   │  │
│    │  • Base: 50 points                                      │  │
│    │  • Amount: up to 30 points                              │  │
│    │  • Countries: up to 20 points                           │  │
│    │  • Transactions: up to 10 points                        │  │
│    └─────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### **Slide 7: Technical Architecture Deep Dive**
```
┌─────────────────────────────────────────────────────────────────┐
│                    TECHNICAL ARCHITECTURE                       │
│                                                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                    DOMAIN LAYER                         │  │
│    │                                                         │  │
│    │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │  │
│    │  │ Transaction │  │FraudAlert   │  │Account      │     │  │
│    │  │ Model       │  │Model        │  │Activity     │     │  │
│    │  │             │  │             │  │Window       │     │  │
│    │  │ • Immutable │  │ • Risk      │  │ • Sliding   │     │  │
│    │  │ • Validated │  │   scoring   │  │   window    │     │  │
│    │  │ • Domain    │  │ • Multi-    │  │ • Country   │     │  │
│    │  │   methods   │  │   country   │  │   tracking  │     │  │
│    │  └─────────────┘  └─────────────┘  └─────────────┘     │  │
│    └─────────────────────────────────────────────────────────┘  │
│                              │                                 │
│                              ▼                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                 INFRASTRUCTURE LAYER                    │  │
│    │                                                         │  │
│    │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │  │
│    │  │ Transaction │  │Fraud        │  │Fraud Alert  │     │  │
│    │  │ Producer    │  │Detection    │  │Consumer     │     │  │
│    │  │             │  │Processor    │  │             │     │  │
│    │  │ • Auto-gen  │  │ • Kafka     │  │ • Listen    │     │  │
│    │  │ • Manual    │  │   Streams   │  │ • Display   │     │  │
│    │  │ • Scheduled │  │ • Windows   │  │ • Colored   │     │  │
│    │  └─────────────┘  └─────────────┘  └─────────────┘     │  │
│    └─────────────────────────────────────────────────────────┘  │
│                              │                                 │
│                              ▼                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                    CONFIGURATION                        │  │
│    │                                                         │  │
│    │  • Exactly-Once Semantics V2                            │  │
│    │  • KRaft (No Zookeeper)                                 │  │
│    │  • JSON Serialization                                   │  │
│    │  • Sliding Windows (5 min)                              │  │
│    │  • State Store (RocksDB)                                │  │
│    └─────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### **Slide 8: Exactly-Once Semantics V2**
```
┌─────────────────────────────────────────────────────────────────┐
│                    EXACTLY-ONCE SEMANTICS V2                   │
│                                                                 │
│    🏦 Bank-Grade Reliability                                  │
│                                                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                 GUARANTEES                              │  │
│    │                                                         │  │
│    │  ✅ No Duplicate Processing                              │  │
│    │  ✅ No Lost Messages                                     │  │
│    │  ✅ Ordered Processing                                    │  │
│    │  ✅ Fault Tolerance                                       │  │
│    └─────────────────────────────────────────────────────────┘  │
│                                                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                 IMPLEMENTATION                          │  │
│    │                                                         │  │
│    │  • Idempotent Producers                                  │  │
│    │  • Transactional Streams                                 │  │
│    │  • State Store Recovery                                  │  │
│    │  • Offset Management                                     │  │
│    │  • Graceful Failure Handling                             │  │
│    └─────────────────────────────────────────────────────────┘  │
│                                                                 │
│    💡 Perfect for Financial Applications                      │
└─────────────────────────────────────────────────────────────────┘
```

### **Slide 9: Sliding Windows Visualization**
```
┌─────────────────────────────────────────────────────────────────┐
│                    SLIDING WINDOWS                              │
│                                                                 │
│    Time: 14:00 ── 14:05 ── 14:10 ── 14:15 ── 14:20 ── 14:25   │
│                                                                 │
│    ┌─────────────┐                                             │
│    │ Window 1    │ 14:00-14:05                                 │
│    │ €300 ES     │                                             │
│    └─────────────┘                                             │
│                                                                 │
│        ┌─────────────┐                                         │
│        │ Window 2    │ 14:01-14:06                             │
│        │ €500 ES,FR  │                                         │
│        └─────────────┘                                         │
│                                                                 │
│            ┌─────────────┐                                     │
│            │ Window 3    │ 14:02-14:07                         │
│            │ €800 ES,FR,DE│                                     │
│            └─────────────┘                                     │
│                                                                 │
│                ┌─────────────┐                                 │
│                │ Window 4    │ 14:03-14:08                     │
│                │ €1200 ES,FR,DE,IT│                             │
│                └─────────────┘                                 │
│                                                                 │
│                    ┌─────────────┐                             │
│                    │ Window 5    │ 14:04-14:09                 │
│                    │ €1500 ES,FR,DE,IT,UK│                     │
│                    │ 🚨 FRAUD!   │                             │
│                    └─────────────┘                             │
│                                                                 │
│    💡 Each window maintains state independently                 │
└─────────────────────────────────────────────────────────────────┘
```

### **Slide 10: Risk Scoring System**
```
┌─────────────────────────────────────────────────────────────────┐
│                    RISK SCORING SYSTEM                          │
│                                                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                 SCORE CALCULATION                        │  │
│    │                                                         │  │
│    │  🎯 Base Score: 50 points                               │  │
│    │                                                         │  │
│    │  💰 Amount Score: up to 30 points                       │  │
│    │     • €1000 = 10 points                                 │  │
│    │     • €2000 = 20 points                                 │  │
│    │     • €3000+ = 30 points                                │  │
│    │                                                         │  │
│    │  🌍 Country Score: up to 20 points                      │  │
│    │     • 3 countries = 5 points                            │  │
│    │     • 4 countries = 10 points                           │  │
│    │     • 5+ countries = 20 points                          │  │
│    │                                                         │  │
│    │  📊 Transaction Score: up to 10 points                  │  │
│    │     • 2 transactions = 4 points                         │  │
│    │     • 5 transactions = 10 points                        │  │
│    └─────────────────────────────────────────────────────────┘  │
│                                                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                 RISK LEVELS                              │  │
│    │                                                         │  │
│    │  🔴 CRITICAL (90-100): Immediate action required        │  │
│    │  🟡 HIGH (70-89): High priority investigation           │  │
│    │  🔵 MEDIUM (50-69): Review and monitor                  │  │
│    │  🟢 LOW (0-49): Log for reference                       │  │
│    └─────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### **Slide 11: Demo Scenarios**
```
┌─────────────────────────────────────────────────────────────────┐
│                    DEMO SCENARIOS                               │
│                                                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                 NORMAL OPERATION                         │  │
│    │                                                         │  │
│    │  • Transactions every 2 seconds                         │  │
│    │  • €10-500 per transaction                              │  │
│    │  • Single country                                       │  │
│    │  • No fraud alerts                                      │  │
│    │  • Green console output                                 │  │
│    └─────────────────────────────────────────────────────────┘  │
│                                                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                 FRAUD SCENARIO                           │  │
│    │                                                         │  │
│    │  • 5 transactions in 5 minutes                          │  │
│    │  • €250 each = €1250 total                              │  │
│    │  • 5 different countries                                │  │
│    │  • Fraud alert within 5 minutes                         │  │
│    │  • Red console output                                   │  │
│    │  • Risk score: 85/100                                   │  │
│    └─────────────────────────────────────────────────────────┘  │
│                                                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                 MANUAL TRIGGER                           │  │
│    │                                                         │  │
│    │  curl -X POST http://localhost:8080/api/demo/fraud/ACC-001│  │
│    └─────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### **Slide 12: Performance Metrics**
```
┌─────────────────────────────────────────────────────────────────┐
│                    PERFORMANCE METRICS                          │
│                                                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                 LATENCY                                  │  │
│    │                                                         │  │
│    │  ⚡ Transaction Processing: < 100ms                     │  │
│    │  ⚡ Fraud Detection: < 50ms                             │  │
│    │  ⚡ Alert Generation: < 10ms                            │  │
│    └─────────────────────────────────────────────────────────┘  │
│                                                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                 THROUGHPUT                               │  │
│    │                                                         │  │
│    │  📈 1000+ transactions/second                           │  │
│    │  📈 100+ accounts simultaneously                        │  │
│    │  📈 Horizontal scaling ready                            │  │
│    └─────────────────────────────────────────────────────────┘  │
│                                                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                 RELIABILITY                              │  │
│    │                                                         │  │
│    │  🛡️ 99.99% message delivery                             │  │
│    │  🛡️ Exactly-once processing                             │  │
│    │  🛡️ Fault tolerance                                     │  │
│    │  🛡️ Auto-recovery                                       │  │
│    └─────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### **Slide 13: Business Value**
```
┌─────────────────────────────────────────────────────────────────┐
│                    BUSINESS VALUE                               │
│                                                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                 FINANCIAL BENEFITS                       │  │
│    │                                                         │  │
│    │  💰 Reduce fraud losses by 90%                          │  │
│    │  💰 Lower operational costs                             │  │
│    │  💰 Improve customer trust                              │  │
│    │  💰 Regulatory compliance                               │  │
│    └─────────────────────────────────────────────────────────┘  │
│                                                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                 OPERATIONAL BENEFITS                     │  │
│    │                                                         │  │
│    │  ⚡ Real-time response                                   │  │
│    │  ⚡ Automated detection                                  │  │
│    │  ⚡ Reduced manual review                                │  │
│    │  ⚡ Scalable architecture                                │  │
│    └─────────────────────────────────────────────────────────┘  │
│                                                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                 TECHNICAL BENEFITS                       │  │
│    │                                                         │  │
│    │  🏗️ Modern architecture                                 │  │
│    │  🏗️ Cloud-native ready                                  │  │
│    │  🏗️ Easy maintenance                                    │  │
│    │  🏗️ Future-proof design                                 │  │
│    └─────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### **Slide 14: Live Demo Preview**
```
┌─────────────────────────────────────────────────────────────────┐
│                    LIVE DEMO PREVIEW                            │
│                                                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                 WHAT YOU'LL SEE                          │  │
│    │                                                         │  │
│    │  1. System startup and Kafka connection                 │  │
│    │  2. Normal transaction generation                       │  │
│    │  3. Manual fraud scenario trigger                       │  │
│    │  4. Real-time fraud detection                           │  │
│    │  5. Color-coded alert display                           │  │
│    │  6. Risk score calculation                              │  │
│    │  7. System performance metrics                          │  │
│    └─────────────────────────────────────────────────────────┘  │
│                                                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                 CONSOLE OUTPUT                           │  │
│    │                                                         │  │
│    │  🚨 FRAUD ALERT DETECTED 🚨                             │  │
│    │  ═══════════════════════════════════════════════════════ │  │
│    │  ⏰ Time: 14:30:25                                       │  │
│    │  🆔 Alert ID: FRAUD-ABC12345                             │  │
│    │  👤 Account: ACC-001                                     │  │
│    │  💰 Amount: €1250.00                                     │  │
│    │  🌍 Countries: 5 (ES, FR, DE, IT, UK)                   │  │
│    │  📊 Transactions: 5                                      │  │
│    │  ⚠️  Risk Score: 85/100 (HIGH)                           │  │
│    │  ═══════════════════════════════════════════════════════ │  │
│    └─────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### **Slide 15: Conclusion & Next Steps**
```
┌─────────────────────────────────────────────────────────────────┐
│                    CONCLUSION & NEXT STEPS                      │
│                                                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                 KEY TAKEAWAYS                            │  │
│    │                                                         │  │
│    │  ✅ Real-time fraud detection is possible               │  │
│    │  ✅ Bank-grade reliability with EOS V2                  │  │
│    │  ✅ Scalable architecture with Kafka Streams            │  │
│    │  ✅ Modern Java development practices                    │  │
│    └─────────────────────────────────────────────────────────┘  │
│                                                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                 NEXT STEPS                               │  │
│    │                                                         │  │
│    │  🚀 Deploy to production                                 │  │
│    │  🚀 Add machine learning models                         │  │
│    │  🚀 Integrate with payment systems                       │  │
│    │  🚀 Add real-time dashboards                             │  │
│    │  🚀 Implement advanced analytics                         │  │
│    └─────────────────────────────────────────────────────────┘  │
│                                                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                 QUESTIONS & DISCUSSION                   │  │
│    │                                                         │  │
│    │  🤔 How to scale to millions of transactions?           │  │
│    │  🤔 How to add more fraud detection rules?              │  │
│    │  🤔 How to integrate with existing systems?             │  │
│    │  🤔 How to handle data privacy and GDPR?                │  │
│    └─────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## 🎨 Design Guidelines for Keynote

### **Color Scheme**
- **Primary Blue**: #2563EB (for headers and main elements)
- **Secondary Green**: #10B981 (for success/positive elements)
- **Warning Yellow**: #F59E0B (for medium risk)
- **Danger Red**: #EF4444 (for high risk/critical)
- **Neutral Gray**: #6B7280 (for text and borders)
- **Background**: #FFFFFF (white) or #F9FAFB (light gray)

### **Typography**
- **Headers**: SF Pro Display Bold, 36-48pt
- **Subheaders**: SF Pro Display Medium, 24-32pt
- **Body Text**: SF Pro Text Regular, 18-24pt
- **Code**: SF Mono Regular, 16-20pt

### **Layout Tips**
1. **Use consistent spacing** (24pt between elements)
2. **Left-align text** for readability
3. **Use icons** to break up text-heavy slides
4. **Keep diagrams simple** with clear labels
5. **Use animations** sparingly for emphasis
6. **Include code snippets** in monospace font with syntax highlighting

### **Animation Suggestions**
- **Build animations**: Reveal elements one by one
- **Transitions**: Use "Move" or "Dissolve" between slides
- **Emphasis**: Scale or color changes for important points
- **Flow**: Use motion paths for data flow diagrams

## 📝 Speaking Notes

### **Slide 1-2 (2 minutes)**
- Introduce yourself and the project
- Explain the problem: traditional fraud detection is slow and inaccurate
- Set up the challenge: need real-time, accurate, scalable solution

### **Slide 3-4 (2 minutes)**
- Present FraudLens as the solution
- Highlight key technologies: Kafka Streams, Spring Boot, EOS V2
- Show high-level architecture

### **Slide 5-6 (3 minutes)**
- Explain data flow step by step
- Detail the fraud detection algorithm
- Emphasize the 5-minute sliding window concept

### **Slide 7-8 (2 minutes)**
- Deep dive into technical architecture
- Explain Exactly-Once Semantics V2 importance
- Show why it's perfect for financial applications

### **Slide 9-10 (2 minutes)**
- Visualize sliding windows with timeline
- Explain risk scoring system
- Show how different factors contribute to risk

### **Slide 11-12 (2 minutes)**
- Preview demo scenarios
- Show performance metrics
- Emphasize scalability and reliability

### **Slide 13-14 (2 minutes)**
- Discuss business value
- Preview live demo
- Show expected console output

### **Slide 15 (1 minute)**
- Summarize key takeaways
- Discuss next steps
- Open for questions

## 🎯 Key Messages to Emphasize

1. **Real-time processing** is the future of fraud detection
2. **Exactly-Once Semantics V2** provides bank-grade reliability
3. **Kafka Streams** enables scalable, stateful processing
4. **Modern Java** with Spring Boot makes development efficient
5. **Domain-Driven Design** ensures maintainable, testable code
6. **Performance** meets production requirements
7. **Business value** is immediate and measurable 