# 🔍 FraudLens - Real-time Fraud Detection System

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Kafka-3.6.1-blue.svg)](https://kafka.apache.org/)
[![KRaft](https://img.shields.io/badge/KRaft-Enabled-yellow.svg)](https://kafka.apache.org/documentation/#kraft)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## 📋 Descripción del Proyecto

**FraudLens** es un sistema de detección de fraudes bancarios en tiempo real que utiliza **Kafka Streams** con **KRaft** (sin Zookeeper) y **Spring Boot** siguiendo principios de **Domain-Driven Design (DDD)**. 

El sistema detecta automáticamente patrones sospechosos de transacciones financieras utilizando **ventanas deslizantes de 5 minutos** y **Exactly-Once Semantics V2** para garantizar la confiabilidad a nivel bancario.

### 🚨 Criterios de Detección de Fraude

- **Importe Total**: ≥ €1,000 en la ventana de 5 minutos
- **Múltiples Países**: ≥ 3 países diferentes en la ventana
- **Procesamiento**: Tiempo real con EOS V2 (bank-grade reliability)
- **Alertas**: Colores ANSI en terminal con niveles de riesgo

## 🏗️ Arquitectura del Sistema

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│                 │    │                 │    │                 │
│   Transaction   │───▶│  Kafka Streams  │───▶│   Fraud Alert   │
│    Producer     │    │   Processor     │    │    Consumer     │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│                 │    │                 │    │                 │
│   transactions  │    │ Windowed State  │    │  fraud-alerts   │
│     Topic       │    │    (5 min)      │    │     Topic       │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 🔧 Componentes Principales

1. **Domain Layer (DDD)**:
   - `Transaction`: Entidad principal con validaciones
   - `FraudAlert`: Alerta de fraude con score de riesgo
   - `AccountActivityWindow`: Value object para ventanas deslizantes
   - `FraudDetectionService`: Servicio de dominio

2. **Infrastructure Layer**:
   - `TransactionProducer`: Genera transacciones demo
   - `FraudDetectionProcessor`: Procesamiento Kafka Streams
   - `FraudAlertConsumer`: Consumidor con output colorido
   - `JsonSerde`: Serialización personalizada

3. **Configuration**:
   - KRaft sin Zookeeper
   - Exactly-Once Semantics V2
   - Ventanas deslizantes de 5 minutos
   - Agregación por cuenta con estado

## 🚀 Inicio Rápido

### Prerrequisitos

- ☕ Java 17+
- 🔧 Maven 3.8+
- 📦 Kafka 3.6+ (se incluye script de setup)
- 💻 macOS/Linux (para scripts bash)

### 1. Clonar el Repositorio

```bash
git clone https://github.com/your-username/FraudLens-KafkaStreams.git
cd FraudLens-KafkaStreams
```

### 2. Configurar y Ejecutar Kafka KRaft

```bash
# Dar permisos de ejecución al script
chmod +x scripts/start-kafka-kraft.sh

# Iniciar Kafka con KRaft (sin Zookeeper)
./scripts/start-kafka-kraft.sh start
```

Este script:
- ✅ Configura Kafka con KRaft
- ✅ Crea los topics necesarios (`transactions`, `fraud-alerts`)
- ✅ Inicia el broker en localhost:9092

### 3. Compilar y Ejecutar la Aplicación

```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar la aplicación
mvn spring-boot:run
```

### 4. Observar las Alertas de Fraude

Una vez iniciada la aplicación, verás en la terminal:

```
🔍 FraudLens - Real-time Fraud Detection System
🚀 Starting Kafka Streams processing...

═══════════════════════════════════════════════════════════════
                        🚨 FRAUD ALERT DETECTED 🚨
═══════════════════════════════════════════════════════════════
Alert ID: FRAUD-A7B2C3D4
Account: ACC-003
Risk Level: HIGH
Risk Score: 85/100
💰 Total Amount: €1,250.00
🌍 Countries: 4 countries (ES, FR, DE, IT)
Transactions: 5 transactions
⏰ Time Window: 14:32:15 - 14:37:15
⏰ Alert Time: 14:37:18
🛡️ Description: Suspicious activity detected: €1,250.00 across 4 countries in 5 transactions within 5-minute window

⚠️ RECOMMENDED ACTIONS:
   • Review account activity manually
   • Consider temporary limits
═══════════════════════════════════════════════════════════════
```

## 🎯 API REST para Demo

### Generar Escenario de Fraude

```bash
# Generar transacciones fraudulentas para una cuenta específica
curl -X POST "http://localhost:8080/api/demo/fraud/ACC-001"

# Generar transacciones normales
curl -X POST "http://localhost:8080/api/demo/normal/10"

# Verificar estado de la aplicación
curl "http://localhost:8080/actuator/health"
```

## 📊 Características Técnicas

### 🔐 Exactly-Once Semantics V2
- Garantiza que cada transacción se procesa exactamente una vez
- Ideal para aplicaciones bancarias críticas
- Configuración optimizada para confiabilidad

### 🪟 Ventanas Deslizantes
- Ventana de 5 minutos por cuenta
- Agregación de importes y países
- Detección en tiempo real

### 🎨 Alertas Visuales
- Colores ANSI en terminal
- Niveles de riesgo (LOW, MEDIUM, HIGH, CRITICAL)
- Recomendaciones automatizadas

### 📈 Métricas y Monitoreo
- Actuator endpoints habilitados
- Métricas de Prometheus
- Logs estructurados con niveles

## 🔧 Configuración Avanzada

### Variables de Entorno

```bash
# Configuración de Kafka
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export KAFKA_APPLICATION_ID=fraudlens-app

# Configuración de detección
export FRAUD_AMOUNT_THRESHOLD=1000.00
export FRAUD_COUNTRY_THRESHOLD=3
export FRAUD_WINDOW_SIZE_MINUTES=5
```

### Personalización de Alertas

Edita `src/main/resources/application.properties`:

```properties
# Cambiar umbrales de detección
fraudlens.fraud.amount-threshold=500.00
fraudlens.fraud.country-threshold=2
fraudlens.fraud.window-size-minutes=3

# Configurar intervalos de demo
fraudlens.demo.transaction-interval=1000
fraudlens.demo.fraud-interval=10000
```

## 📚 Conceptos Técnicos Destacados

### 1. **KRaft (Kafka Raft)**
- Eliminación de Zookeeper
- Arquitectura simplificada
- Mejor rendimiento y escalabilidad

### 2. **Exactly-Once Semantics V2**
- Garantías de entrega exacta
- Idempotencia de productores
- Transacciones de Kafka Streams

### 3. **Domain-Driven Design**
- Separación clara de capas
- Entidades con lógica de negocio
- Servicios de dominio especializados

### 4. **Ventanas Deslizantes**
- Agregación temporal por cuenta
- Estado persistente en RocksDB
- Procesamiento de eventos ordenados

## 🎥 Demo para LinkedIn

### Pasos para el Video

1. **Mostrar Arquitectura**: Explicar KRaft, Streams, EOS V2
2. **Ejecutar Setup**: `./scripts/start-kafka-kraft.sh start`
3. **Iniciar App**: `mvn spring-boot:run`
4. **Observar Transacciones**: Ver logs normales
5. **Trigger Fraude**: API REST o automático
6. **Mostrar Alertas**: Colores ANSI y niveles de riesgo
7. **Explicar Métricas**: Actuator endpoints

### Puntos Clave a Mencionar

- 🔄 **Tiempo Real**: Procesamiento de eventos en streaming
- 🛡️ **Confiabilidad**: EOS V2 para aplicaciones críticas
- 🏗️ **Arquitectura**: KRaft moderno sin Zookeeper
- 🎯 **DDD**: Diseño orientado al dominio
- 📊 **Observabilidad**: Métricas y logs estructurados

## 🧪 Testing

```bash
# Ejecutar tests unitarios
mvn test

# Ejecutar tests de integración
mvn integration-test

# Verificar cobertura
mvn jacoco:report
```

## 🔍 Troubleshooting

### Kafka no inicia
```bash
# Verificar si hay procesos Kafka ejecutándose
ps aux | grep kafka

# Limpiar datos anteriores
rm -rf kafka-data kafka-logs

# Reiniciar Kafka
./scripts/start-kafka-kraft.sh restart
```

### Aplicación no detecta fraudes
```bash
# Verificar topics
kafka-topics.sh --list --bootstrap-server localhost:9092

# Verificar logs
tail -f logs/fraudlens.log

# Generar fraude manual
curl -X POST "http://localhost:8080/api/demo/fraud/ACC-001"
```

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo [LICENSE](LICENSE) para más detalles.

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor:

1. Fork el proyecto
2. Crea una rama para tu feature
3. Commit tus cambios
4. Push a la rama
5. Abre un Pull Request

## 📞 Contacto

- **Autor**: Tu Nombre
- **LinkedIn**: [tu-perfil-linkedin]
- **Email**: tu-email@ejemplo.com
- **GitHub**: [tu-username]

---

⭐ Si te gusta este proyecto, ¡dale una estrella en GitHub!

🔍 **FraudLens** - Detectando fraudes en tiempo real con Kafka Streams y Spring Boot 