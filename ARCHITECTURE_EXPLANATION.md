# FraudLens - Arquitectura y Explicación Detallada

## 🏗️ Estructura del Repositorio

```
FraudLens-KafkaStreams/
├── 📁 kafka-config/           # Configuración de Kafka KRaft
├── 📁 kafka-data/             # Datos persistentes de Kafka
├── 📁 kafka-logs/             # Logs de Kafka
├── 📁 logs/                   # Logs de la aplicación
├── 📁 scripts/                # Scripts de automatización
├── 📁 src/                    # Código fuente principal
│   └── main/
│       ├── java/com/fraudlens/
│       │   ├── 📁 config/     # Configuración de Spring/Kafka
│       │   ├── 📁 controller/ # API REST
│       │   ├── 📁 domain/     # Lógica de negocio (DDD)
│       │   └── 📁 infrastructure/ # Integración externa
│       └── resources/         # Archivos de configuración
├── 📁 target/                 # Archivos compilados
├── 📄 pom.xml                 # Dependencias Maven
├── 📄 README.md               # Documentación principal
└── 📄 run-demo.sh            # Script de ejecución
```

## 🎯 Principios de Diseño

### **Domain-Driven Design (DDD)**
- **Separación de capas**: Domain, Infrastructure, Application
- **Entidades ricas**: Con lógica de negocio
- **Value Objects**: Inmutables y validados
- **Servicios de dominio**: Lógica de negocio centralizada

### **Clean Architecture**
- **Independencia de frameworks**: El dominio no depende de Spring/Kafka
- **Inversión de dependencias**: Interfaces en el dominio, implementaciones en infrastructure
- **Testabilidad**: Fácil de testear cada capa independientemente

### **Event-Driven Architecture**
- **Kafka Streams**: Procesamiento en tiempo real
- **Event sourcing**: Cada transacción es un evento
- **CQRS**: Separación de comandos y consultas

---

## 📁 EXPLICACIÓN DETALLADA DE CADA ARCHIVO

### **1. CONFIGURACIÓN Y SETUP**

#### **📄 pom.xml**
```xml
<!-- Dependencias principales -->
<dependencies>
    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Kafka Streams -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka-streams</artifactId>
    </dependency>
    
    <!-- Validación -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>
```

**¿Por qué estas dependencias?**
- **Spring Boot Web**: API REST para demos y monitoreo
- **Kafka Streams**: Procesamiento en tiempo real
- **Validation**: Validación de datos de entrada
- **Actuator**: Métricas y monitoreo

#### **📄 scripts/start-kafka-kraft.sh**
```bash
#!/bin/bash
# Script para iniciar Kafka con KRaft (sin Zookeeper)

# 1. Configuración de directorios
# 2. Generación de cluster ID
# 3. Formateo del storage
# 4. Inicio del broker
# 5. Creación de topics
```

**¿Por qué KRaft?**
- **Sin Zookeeper**: Arquitectura simplificada
- **Mejor rendimiento**: Menos latencia
- **Más fácil de configurar**: Un solo componente
- **Futuro de Kafka**: Zookeeper será deprecado

#### **📄 src/main/resources/application.properties**
```properties
# Server configuration
server.port=8081

# Kafka configuration
spring.kafka.bootstrap-servers=localhost:9092
fraudlens.kafka.application-id=fraudlens-app

# Fraud detection thresholds
fraudlens.fraud.amount-threshold=1000.00
fraudlens.fraud.country-threshold=3
fraudlens.fraud.window-size-minutes=5
```

**¿Por qué esta configuración?**
- **Puerto 8081**: Evita conflictos con otras aplicaciones
- **Thresholds configurables**: Fácil ajuste de reglas de fraude
- **Separación de configs**: Por ambiente (dev, test, prod)

---

### **2. DOMAIN LAYER (Lógica de Negocio)**

#### **📄 src/main/java/com/fraudlens/domain/model/Transaction.java**
```java
public class Transaction {
    private final String transactionId;
    private final String accountId;
    private final BigDecimal amount;
    private final String country;
    // ... más campos
    
    // Métodos de dominio
    public boolean isHighValue() {
        return amount.compareTo(new BigDecimal("1000.00")) >= 0;
    }
    
    public boolean isFromCountry(String targetCountry) {
        return country.equalsIgnoreCase(targetCountry);
    }
}
```

**¿Por qué esta estructura?**
- **Inmutabilidad**: Thread-safe para procesamiento concurrente
- **Validación en constructor**: Garantiza integridad de datos
- **Métodos de dominio**: Lógica de negocio encapsulada
- **Value Objects**: Representan conceptos del dominio

#### **📄 src/main/java/com/fraudlens/domain/model/AccountActivityWindow.java**
```java
public class AccountActivityWindow {
    private final String accountId;
    private final Instant windowStart;
    private final Instant windowEnd;
    private final BigDecimal totalAmount;
    private final Set<String> countries;
    
    // Factory method
    public static AccountActivityWindow createNew(String accountId, Instant windowStart, Instant windowEnd) {
        return new AccountActivityWindow(accountId, windowStart, windowEnd, 
                                       BigDecimal.ZERO, 0, new HashSet<>(), Instant.now());
    }
    
    // Métodos de dominio
    public boolean isSuspiciousActivity() {
        return exceedsAmountThreshold() && hasMultipleCountries();
    }
}
```

**¿Por qué ventanas de actividad?**
- **Agregación temporal**: Agrupa transacciones por tiempo
- **Estado mutable**: Se actualiza con cada transacción
- **Lógica de detección**: Centraliza las reglas de fraude
- **Inmutabilidad funcional**: Cada actualización crea una nueva instancia

#### **📄 src/main/java/com/fraudlens/domain/model/FraudAlert.java**
```java
public class FraudAlert {
    private final String alertId;
    private final String accountId;
    private final BigDecimal totalAmount;
    private final Set<String> countriesInvolved;
    private final int riskScore;
    
    // Métodos de dominio
    public boolean isHighRisk() {
        return riskScore >= 80;
    }
    
    public boolean isCritical() {
        return riskScore >= 95;
    }
}
```

**¿Por qué esta estructura?**
- **Información completa**: Todo lo necesario para tomar decisiones
- **Score de riesgo**: Cuantifica el nivel de amenaza
- **Métodos de dominio**: Facilita el procesamiento posterior
- **Inmutabilidad**: Garantiza consistencia

#### **📄 src/main/java/com/fraudlens/domain/service/FraudDetectionService.java**
```java
@Service
public class FraudDetectionService {
    
    public boolean isFraudulent(AccountActivityWindow activityWindow) {
        boolean exceedsAmount = activityWindow.exceedsAmountThreshold();
        boolean hasMultipleCountries = activityWindow.hasMultipleCountries();
        return exceedsAmount && hasMultipleCountries;
    }
    
    public FraudAlert generateFraudAlert(AccountActivityWindow activityWindow) {
        int riskScore = calculateRiskScore(activityWindow);
        String description = generateDescription(activityWindow);
        return new FraudAlert(/* ... */);
    }
    
    private int calculateRiskScore(AccountActivityWindow activityWindow) {
        // Lógica de cálculo de riesgo
        // Base: 50 puntos
        // Amount: hasta 30 puntos
        // Countries: hasta 20 puntos
        // Transactions: hasta 10 puntos
    }
}
```

**¿Por qué un servicio de dominio?**
- **Lógica centralizada**: Todas las reglas de fraude en un lugar
- **Cálculo de riesgo**: Algoritmo complejo encapsulado
- **Reutilización**: Usado por múltiples componentes
- **Testabilidad**: Fácil de testear independientemente

---

### **3. INFRASTRUCTURE LAYER (Integración Externa)**

#### **📄 src/main/java/com/fraudlens/infrastructure/kafka/FraudDetectionProcessor.java**
```java
@Component
public class FraudDetectionProcessor {
    
    @Autowired
    public void buildPipeline(StreamsBuilder streamsBuilder) {
        // 1. Stream de transacciones de entrada
        KStream<String, Transaction> transactionStream = streamsBuilder
                .stream(TRANSACTIONS_TOPIC, Consumed.with(Serdes.String(), new JsonSerde<>(Transaction.class)));
        
        // 2. Agrupar por accountId y procesar en ventanas deslizantes
        KTable<Windowed<String>, AccountActivityWindow> accountActivityTable = transactionStream
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeAndGrace(WINDOW_SIZE, GRACE_PERIOD))
                .aggregate(
                    // Inicializador
                    () -> AccountActivityWindow.createNew("", now.minus(WINDOW_SIZE), now),
                    // Agregador
                    (accountId, transaction, window) -> aggregateTransaction(accountId, transaction, window),
                    // Materializador
                    Materialized.as("account-activity-store")
                );
        
        // 3. Detectar fraudes y generar alertas
        KStream<String, FraudAlert> fraudAlertStream = accountActivityTable
                .toStream()
                .filter((windowedKey, window) -> fraudDetectionService.isFraudulent(window))
                .map((windowedKey, window) -> {
                    FraudAlert alert = fraudDetectionService.generateFraudAlert(window);
                    return KeyValue.pair(alert.getAccountId(), alert);
                });
        
        // 4. Enviar alertas al topic de salida
        fraudAlertStream.to(FRAUD_ALERTS_TOPIC);
    }
}
```

**¿Por qué Kafka Streams?**
- **Procesamiento en tiempo real**: Cada transacción se procesa inmediatamente
- **Estado distribuido**: Mantiene estado en múltiples nodos
- **Escalabilidad horizontal**: Puede procesar millones de transacciones
- **Exactly-once semantics**: Garantiza procesamiento exacto

**¿Por qué ventanas deslizantes?**
- **Análisis temporal**: Detecta patrones en ventanas de tiempo
- **Estado persistente**: Mantiene contexto entre transacciones
- **Procesamiento continuo**: No necesita reiniciar para nuevas ventanas

#### **📄 src/main/java/com/fraudlens/infrastructure/kafka/TransactionProducer.java**
```java
@Service
public class TransactionProducer {
    
    @Scheduled(fixedDelay = 2000) // Cada 2 segundos
    public void generateNormalTransaction() {
        Transaction transaction = createNormalTransaction();
        sendTransaction(transaction);
    }
    
    @Scheduled(fixedDelay = 15000) // Cada 15 segundos
    public void generateSuspiciousActivity() {
        String accountId = accountIds.get(random.nextInt(accountIds.size()));
        for (int i = 0; i < 4; i++) {
            Transaction transaction = createSuspiciousTransaction(accountId);
            sendTransaction(transaction);
            Thread.sleep(500);
        }
    }
    
    public void generateFraudScenario(String accountId) {
        // Genera 5 transacciones en diferentes países
        String[] fraudCountries = {"ES", "FR", "DE", "IT", "UK"};
        for (int i = 0; i < fraudCountries.length; i++) {
            Transaction transaction = new Transaction(/* ... */);
            sendTransaction(transaction);
        }
    }
}
```

**¿Por qué generación automática?**
- **Demo continuo**: Siempre hay datos para mostrar
- **Escenarios realistas**: Simula comportamiento real
- **Detección automática**: No necesita intervención manual
- **Múltiples patrones**: Normal y sospechoso

#### **📄 src/main/java/com/fraudlens/infrastructure/kafka/FraudAlertConsumer.java**
```java
@Component
public class FraudAlertConsumer {
    
    @KafkaListener(topics = "fraud-alerts", groupId = "fraudlens-consumer-group")
    public void handleFraudAlert(@Payload String alertJson) {
        FraudAlert alert = objectMapper.readValue(alertJson, FraudAlert.class);
        
        String riskLevel = getRiskLevel(alert.getRiskScore());
        String colorCode = getColorCode(riskLevel);
        
        // Mostrar alerta colorida en consola
        System.out.println("\n" + colorCode + "🚨 FRAUD ALERT DETECTED 🚨" + "\u001B[0m");
        System.out.println(colorCode + "👤 Account: " + alert.getAccountId() + "\u001B[0m");
        System.out.println(colorCode + "💰 Amount: €" + alert.getTotalAmount() + "\u001B[0m");
        // ... más información
    }
}
```

**¿Por qué output colorido?**
- **Visualización clara**: Fácil de identificar alertas
- **Niveles de riesgo**: Diferentes colores por severidad
- **Información completa**: Todo lo necesario para tomar decisiones
- **Demo impactante**: Perfecto para presentaciones

#### **📄 src/main/java/com/fraudlens/infrastructure/serde/JsonSerde.java**
```java
public class JsonSerde<T> implements Serde<T> {
    
    private final Class<T> type;
    private final ObjectMapper objectMapper;
    
    @Override
    public Serializer<T> serializer() {
        return new JsonSerializer();
    }
    
    @Override
    public Deserializer<T> deserializer() {
        return new JsonDeserializer();
    }
}
```

**¿Por qué Serdes personalizados?**
- **Serialización JSON**: Formato legible y flexible
- **Type safety**: Garantiza tipos correctos
- **Configuración centralizada**: Un solo lugar para configurar
- **Reutilización**: Usado por múltiples componentes

---

### **4. CONFIGURATION LAYER**

#### **📄 src/main/java/com/fraudlens/config/KafkaConfig.java**
```java
@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaConfig {
    
    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        
        // Exactly-Once Semantics V2
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
        
        // Configuración de rendimiento
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10000);
        props.put(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, 10 * 1024 * 1024);
        
        return new KafkaStreamsConfiguration(props);
    }
}
```

**¿Por qué Exactly-Once V2?**
- **Bank-grade reliability**: Garantiza procesamiento exacto
- **Sin duplicados**: Cada transacción se procesa una sola vez
- **Sin pérdidas**: No se pierden transacciones
- **Consistencia**: Estado consistente incluso con fallos

#### **📄 src/main/java/com/fraudlens/config/ApplicationProperties.java**
```java
@Configuration
@ConfigurationProperties(prefix = "fraudlens")
public class ApplicationProperties {
    
    private Demo demo = new Demo();
    private Fraud fraud = new Fraud();
    private Kafka kafka = new Kafka();
    
    public static class Fraud {
        private double amountThreshold = 1000.00;
        private int countryThreshold = 3;
        private int windowSizeMinutes = 5;
    }
}
```

**¿Por qué ConfigurationProperties?**
- **Configuración tipada**: Evita errores de configuración
- **Validación automática**: Spring valida los valores
- **Documentación**: IDE muestra las propiedades disponibles
- **Flexibilidad**: Fácil cambiar valores por ambiente

---

### **5. CONTROLLER LAYER (API REST)**

#### **📄 src/main/java/com/fraudlens/controller/DemoController.java**
```java
@RestController
@RequestMapping("/api/demo")
public class DemoController {
    
    @PostMapping("/fraud/{accountId}")
    public ResponseEntity<Map<String, Object>> generateFraudScenario(@PathVariable String accountId) {
        transactionProducer.generateFraudScenario(accountId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Fraud scenario generated successfully",
            "accountId", accountId,
            "expectedAlert", "Should trigger fraud alert within 5 minutes"
        ));
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        return ResponseEntity.ok(Map.of(
            "system", "FraudLens",
            "status", "running",
            "features", Map.of(
                "kafka", "KRaft enabled",
                "streams", "Exactly-Once Semantics V2",
                "windowSize", "5 minutes",
                "fraudThreshold", "€1,000 + 3 countries"
            )
        ));
    }
}
```

**¿Por qué API REST?**
- **Demo interactivo**: Permite control manual del sistema
- **Monitoreo**: Verificar estado del sistema
- **Integración**: Fácil integrar con otros sistemas
- **Testing**: Endpoints para testing automatizado

---

### **6. TESTING**

#### **📄 src/test/java/com/fraudlens/FraudLensApplicationTests.java**
```java
@SpringBootTest
@ActiveProfiles("test")
class FraudLensApplicationTests {
    
    @Test
    void testFraudDetectionLogic() {
        // Test fraud detection with suspicious activity
        AccountActivityWindow suspiciousWindow = new AccountActivityWindow(
            "ACC-001", windowStart, windowEnd,
            new BigDecimal("1200.00"), // Over €1000 threshold
            4, // 4 transactions
            countries, // 4 different countries
            now
        );
        
        assertTrue(suspiciousWindow.isSuspiciousActivity());
    }
    
    @Test
    void testRiskScoreCalculation() {
        // Test different risk score scenarios
        FraudAlert alert = fraudDetectionService.generateFraudAlert(window);
        assertTrue(alert.getRiskScore() > 50);
    }
}
```

**¿Por qué tests comprehensivos?**
- **Calidad del código**: Garantiza funcionamiento correcto
- **Refactoring seguro**: Permite cambios sin romper funcionalidad
- **Documentación**: Los tests explican cómo funciona el código
- **Confianza**: Permite desplegar con confianza

---

## 🎯 **¿Por qué esta arquitectura?**

### **1. Escalabilidad**
- **Kafka Streams**: Procesa millones de transacciones
- **Estado distribuido**: Se escala horizontalmente
- **Configuración flexible**: Fácil ajustar parámetros

### **2. Mantenibilidad**
- **Separación de capas**: Cambios aislados
- **DDD**: Código autodocumentado
- **Tests**: Refactoring seguro

### **3. Confiabilidad**
- **Exactly-Once V2**: Procesamiento garantizado
- **Validación**: Datos consistentes
- **Error handling**: Manejo robusto de errores

### **4. Demo-friendly**
- **Generación automática**: Siempre hay datos
- **API REST**: Control manual
- **Output visual**: Alertas coloridas
- **Tests**: Demuestra calidad

### **5. Enterprise-ready**
- **Monitoreo**: Métricas y logs
- **Configuración**: Por ambiente
- **Documentación**: Completa y clara
- **Deployment**: Docker-ready

Esta arquitectura combina **simplicidad** para el demo con **robustez** para producción, siguiendo las mejores prácticas de la industria. 