#!/bin/bash

# ===================================================================
# FraudLens - Kafka KRaft Setup Script
# This script sets up Kafka with KRaft (no Zookeeper) and creates topics
# ===================================================================

set -e

echo "🚀 Starting FraudLens Kafka KRaft Setup..."

# Variables
KAFKA_HOME=${KAFKA_HOME:-"/opt/kafka"}
KAFKA_DATA_DIR="./kafka-data"
KAFKA_LOGS_DIR="./kafka-logs"
KAFKA_CONFIG_DIR="./kafka-config"
CLUSTER_ID="fraudlens-cluster-$(date +%s)"
LOG_FILE="./kafka-setup.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Check if Kafka is installed
check_kafka() {
    if ! command -v kafka-server-start.sh &> /dev/null; then
        print_error "Kafka not found in PATH. Please install Kafka and set KAFKA_HOME."
        print_status "You can download Kafka from: https://kafka.apache.org/downloads"
        exit 1
    fi
    print_status "Kafka found in PATH"
}

# Create necessary directories
create_directories() {
    print_step "Creating necessary directories..."
    mkdir -p "$KAFKA_DATA_DIR" "$KAFKA_LOGS_DIR" "$KAFKA_CONFIG_DIR"
    print_status "Directories created successfully"
}

# Generate KRaft configuration
generate_kraft_config() {
    print_step "Generating KRaft configuration..."
    
    # Generate cluster ID
    CLUSTER_ID=$(kafka-storage.sh random-uuid)
    print_status "Generated cluster ID: $CLUSTER_ID"
    
    # Create server.properties for KRaft
    cat > "$KAFKA_CONFIG_DIR/server.properties" << EOF
# KRaft Configuration for FraudLens
process.roles=broker,controller
node.id=1
controller.quorum.voters=1@localhost:9093
listeners=PLAINTEXT://localhost:9092,CONTROLLER://localhost:9093
inter.broker.listener.name=PLAINTEXT
advertised.listeners=PLAINTEXT://localhost:9092
controller.listener.names=CONTROLLER
listener.security.protocol.map=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT

# Log configuration
log.dirs=$KAFKA_DATA_DIR/kraft-combined-logs
num.network.threads=3
num.io.threads=8
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600

# Log retention
log.retention.hours=168
log.retention.check.interval.ms=300000
log.segment.bytes=1073741824

# Replication configuration
default.replication.factor=1
min.insync.replicas=1
offsets.topic.replication.factor=1
transaction.state.log.replication.factor=1
transaction.state.log.min.isr=1

# Group coordinator configuration
group.initial.rebalance.delay.ms=0

# Exactly-Once configuration
enable.idempotence=true
max.in.flight.requests.per.connection=1
retries=2147483647
acks=all
EOF

    print_status "KRaft configuration generated"
}

# Format storage
format_storage() {
    print_step "Formatting storage for KRaft..."
    kafka-storage.sh format -t "$CLUSTER_ID" -c "$KAFKA_CONFIG_DIR/server.properties"
    print_status "Storage formatted successfully"
}

# Start Kafka
start_kafka() {
    print_step "Starting Kafka server..."
    
    # Check if Kafka is already running
    if pgrep -f "kafka.Kafka" > /dev/null; then
        print_warning "Kafka is already running. Stopping existing instance..."
        stop_kafka
        sleep 5
    fi
    
    # Start Kafka in background
    nohup kafka-server-start.sh "$KAFKA_CONFIG_DIR/server.properties" > "$KAFKA_LOGS_DIR/kafka.log" 2>&1 &
    
    # Wait for Kafka to start
    print_status "Waiting for Kafka to start..."
    for i in {1..30}; do
        if kafka-broker-api-versions.sh --bootstrap-server localhost:9092 > /dev/null 2>&1; then
            print_status "Kafka started successfully!"
            break
        fi
        if [ $i -eq 30 ]; then
            print_error "Kafka failed to start. Check logs in $KAFKA_LOGS_DIR/kafka.log"
            exit 1
        fi
        sleep 2
    done
}

# Create topics
create_topics() {
    print_step "Creating Kafka topics..."
    
    # Create transactions topic
    kafka-topics.sh --create \
        --bootstrap-server localhost:9092 \
        --topic transactions \
        --partitions 3 \
        --replication-factor 1 \
        --config cleanup.policy=delete \
        --config retention.ms=3600000
    
    # Create fraud-alerts topic
    kafka-topics.sh --create \
        --bootstrap-server localhost:9092 \
        --topic fraud-alerts \
        --partitions 3 \
        --replication-factor 1 \
        --config cleanup.policy=delete \
        --config retention.ms=3600000
    
    print_status "Topics created successfully"
    
    # List topics to verify
    print_step "Listing created topics..."
    kafka-topics.sh --list --bootstrap-server localhost:9092
}

# Stop Kafka
stop_kafka() {
    print_step "Stopping Kafka..."
    pkill -f "kafka.Kafka" || true
    print_status "Kafka stopped"
}

# Main function
main() {
    print_status "🔍 FraudLens Kafka KRaft Setup"
    print_status "================================"
    
    check_kafka
    create_directories
    generate_kraft_config
    format_storage
    start_kafka
    create_topics
    
    print_status "✅ Kafka KRaft setup completed successfully!"
    print_status "🚀 Kafka is running on localhost:9092"
    print_status "📊 Topics created: transactions, fraud-alerts"
    print_status "📝 Logs available in: $KAFKA_LOGS_DIR/kafka.log"
    print_status ""
    print_status "🎯 Ready to run FraudLens application!"
    print_status "To stop Kafka: $0 stop"
}

# Handle script arguments
case "${1:-start}" in
    start)
        main
        ;;
    stop)
        stop_kafka
        ;;
    restart)
        stop_kafka
        sleep 5
        main
        ;;
    *)
        echo "Usage: $0 {start|stop|restart}"
        exit 1
        ;;
esac 