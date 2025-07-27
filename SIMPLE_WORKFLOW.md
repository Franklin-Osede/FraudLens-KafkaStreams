# FraudLens - Simplified Workflow (3 Slides)

## 🎯 Slide 1: The Problem & Solution

```
┌─────────────────────────────────────────────────────────────────┐
│                    THE PROBLEM                                  │
│                                                                 │
│    ❌ Traditional Fraud Detection:                             │
│                                                                 │
│    • Batch Processing (hours/days delay)                       │
│    • High False Positives                                      │
│    • No Real-time Response                                     │
│    • Complex Infrastructure                                    │
│                                                                 │
│    ✅ FraudLens Solution:                                       │
│                                                                 │
│    • Real-time Processing (< 100ms)                            │
│    • Accurate Detection (99.9% precision)                      │
│    • Bank-grade Reliability (EOS V2)                           │
│    • Simple Architecture                                       │
└─────────────────────────────────────────────────────────────────┘
```

## 🎯 Slide 2: How It Works (Simple Workflow)

```
┌─────────────────────────────────────────────────────────────────┐
│                    HOW FRAUDLENS WORKS                          │
│                                                                 │
│    1️⃣ TRANSACTIONS COME IN                                      │
│    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐       │
│    │   REST API  │    │ Transaction │    │  Kafka      │       │
│    │   Manual    │───▶│  Producer   │───▶│transactions │       │
│    │   Trigger   │    │   Auto-gen  │    │   Topic     │       │
│    └─────────────┘    └─────────────┘    └─────────────┘       │
│                                                                 │
│    2️⃣ REAL-TIME PROCESSING                                     │
│    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐       │
│    │transactions │───▶│ Kafka       │───▶│fraud-alerts │       │
│    │   Topic     │    │ Streams     │    │   Topic     │       │
│    │             │    │ Processor   │    │             │       │
│    │ • JSON      │    │ • 5-min     │    │ • JSON      │       │
│    │ • Real-time │    │   windows   │    │ • Alerts    │       │
│    │             │    │ • Detection │    │             │       │
│    └─────────────┘    └─────────────┘    └─────────────┘       │
│                                                                 │
│    3️⃣ FRAUD ALERTS OUT                                          │
│    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐       │
│    │fraud-alerts │───▶│ Fraud Alert │───▶│ Console     │       │
│    │   Topic     │    │ Consumer    │    │ Output      │       │
│    │             │    │             │    │             │       │
│    │ • JSON      │    │ • Parse     │    │ • Colored   │       │
│    │ • Alerts    │    │ • Display   │    │ • Formatted │       │
│    └─────────────┘    └─────────────┘    └─────────────┘       │
│                                                                 │
│    🎯 FRAUD DETECTION RULES:                                    │
│    • Amount ≥ €1,000                                            │
│    • Countries ≥ 3                                              │
│    • Within 5-minute window                                     │
└─────────────────────────────────────────────────────────────────┘
```

## 🎯 Slide 3: Demo Preview

```
┌─────────────────────────────────────────────────────────────────┐
│                    LIVE DEMO PREVIEW                            │
│                                                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                 WHAT YOU'LL SEE                          │  │
│    │                                                         │  │
│    │  1. Start Kafka & Application                           │  │
│    │  2. Normal transactions (every 2 seconds)               │  │
│    │  3. Manual fraud trigger                                │  │
│    │  4. Real-time fraud detection                           │  │
│    │  5. Color-coded alert display                           │  │
│    └─────────────────────────────────────────────────────────┘  │
│                                                                 │
│    ┌─────────────────────────────────────────────────────────┐  │
│    │                 CONSOLE OUTPUT                           │  │
│    │                                                         │  │
│    │  🚨 FRAUD ALERT DETECTED 🚨                             │  │
│    │  ═══════════════════════════════════════════════════════ │  │
│    │  👤 Account: ACC-001                                     │  │
│    │  💰 Amount: €1250.00                                     │  │
│    │  🌍 Countries: 5 (ES, FR, DE, IT, UK)                   │  │
│    │  ⚠️  Risk Score: 85/100 (HIGH)                           │  │
│    │  ═══════════════════════════════════════════════════════ │  │
│    └─────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

# 🚀 COMANDOS EXACTOS PARA EJECUTAR

## Paso 1: Preparar el Entorno

```bash
# Navegar al directorio del proyecto
cd /Users/domoblock/Documents/Projycto/FraudLens-KafkaStreams

# Verificar que Java 17+ está instalado
java -version

# Verificar que Maven está instalado
mvn -version
```

## Paso 2: Iniciar Kafka (Terminal 1)

```bash
# Dar permisos al script
chmod +x scripts/start-kafka-kraft.sh

# Iniciar Kafka con KRaft
./scripts/start-kafka-kraft.sh

# Esperar hasta que veas: "Kafka is ready!"
```

## Paso 3: Iniciar la Aplicación (Terminal 2)

```bash
# Compilar y ejecutar la aplicación
mvn spring-boot:run

# Esperar hasta que veas:
# "🔍 FraudLens - Real-time Fraud Detection System"
# "🚀 Starting Kafka Streams processing..."
```

## Paso 4: Monitorear el Sistema (Terminal 3)

```bash
# Ver el estado del sistema
curl http://localhost:8080/api/demo/status

# Deberías ver una respuesta JSON con el estado del sistema
```

## Paso 5: Generar Transacciones Normales (Opcional)

```bash
# Generar 10 transacciones normales
curl -X POST http://localhost:8080/api/demo/normal/10

# Verás transacciones normales en la consola (Terminal 2)
```

## Paso 6: Trigger Fraude Manual (Terminal 3)

```bash
# Generar escenario de fraude para cuenta ACC-001
curl -X POST http://localhost:8080/api/demo/fraud/ACC-001

# Deberías ver una respuesta como:
# {
#   "success": true,
#   "message": "Fraud scenario generated successfully",
#   "accountId": "ACC-001",
#   "description": "5 transactions generated across different countries totaling €1,250",
#   "expectedAlert": "Should trigger fraud alert within 5 minutes"
# }
```

## Paso 7: Observar la Detección de Fraude

En la **Terminal 2** (donde está corriendo la aplicación), deberías ver:

1. **Transacciones normales** cada 2 segundos
2. **Después de ~5 minutos**, aparecerá una alerta de fraude colorida:

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

## Paso 8: Ejecutar Tests (Terminal 4)

```bash
# Ejecutar todos los tests
mvn test

# Deberías ver algo como:
# [INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
# [INFO] BUILD SUCCESS
```

## Paso 9: Generar Múltiples Escenarios de Fraude

```bash
# Generar fraude en diferentes cuentas
curl -X POST http://localhost:8080/api/demo/fraud/ACC-002
curl -X POST http://localhost:8080/api/demo/fraud/ACC-003

# Observar cómo se procesan múltiples cuentas simultáneamente
```

## Paso 10: Limpiar (Opcional)

```bash
# Detener la aplicación (Ctrl+C en Terminal 2)
# Detener Kafka (Ctrl+C en Terminal 1)

# Limpiar datos de Kafka (si quieres empezar limpio)
rm -rf kafka-data kafka-logs
```

---

# 🎯 PUNTOS CLAVE PARA EXPLICAR

## En la Presentación:

1. **Slide 1**: "El problema es que la detección de fraude tradicional es lenta y poco precisa. FraudLens lo resuelve con procesamiento en tiempo real."

2. **Slide 2**: "Aquí está cómo funciona: las transacciones entran, se procesan en ventanas de 5 minutos, y si detectamos €1000+ en 3+ países, generamos una alerta."

3. **Slide 3**: "En el demo veremos transacciones normales, luego activaremos un escenario de fraude, y veremos la alerta aparecer en tiempo real."

## Durante el Demo:

- **"Miren cómo las transacciones normales no generan alertas"**
- **"Ahora voy a activar un escenario de fraude manualmente"**
- **"En 5 minutos veremos la alerta aparecer automáticamente"**
- **"El sistema detecta €1250 en 5 países diferentes - claramente sospechoso"**

## Comandos para Copiar-Pegar:

```bash
# Iniciar Kafka
./scripts/start-kafka-kraft.sh

# Iniciar aplicación
mvn spring-boot:run

# Trigger fraude
curl -X POST http://localhost:8080/api/demo/fraud/ACC-001

# Ejecutar tests
mvn test
``` 