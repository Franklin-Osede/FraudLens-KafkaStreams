#!/bin/bash

# ===================================================================
# FraudLens Demo Script
# One-command setup and demo execution
# ===================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

print_header() {
    echo -e "${PURPLE}${1}${NC}"
}

print_success() {
    echo -e "${GREEN}✅ ${1}${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  ${1}${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  ${1}${NC}"
}

print_error() {
    echo -e "${RED}❌ ${1}${NC}"
}

print_step() {
    echo -e "${BLUE}🔹 ${1}${NC}"
}

# Banner
print_header "🔍 FraudLens - Real-time Fraud Detection System"
print_header "═══════════════════════════════════════════════════════════"

# Check prerequisites
print_step "Checking prerequisites..."

# Check Java
if ! command -v java &> /dev/null; then
    print_error "Java not found. Please install Java 17 or higher."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
print_success "Java found: $JAVA_VERSION"

# Check Maven
if ! command -v mvn &> /dev/null; then
    print_error "Maven not found. Please install Maven 3.8+."
    exit 1
fi

MVN_VERSION=$(mvn --version | head -n1 | awk '{print $3}')
print_success "Maven found: $MVN_VERSION"

# Check if Kafka is available
if ! command -v kafka-server-start.sh &> /dev/null; then
    print_warning "Kafka not found in PATH."
    print_info "Checking for Kafka installation..."
    
    # Try to find Kafka in common locations
    KAFKA_LOCATIONS=("/opt/kafka" "/usr/local/kafka" "./kafka" "$HOME/kafka")
    KAFKA_FOUND=false
    
    for location in "${KAFKA_LOCATIONS[@]}"; do
        if [ -d "$location/bin" ]; then
            export PATH="$location/bin:$PATH"
            KAFKA_FOUND=true
            print_success "Kafka found at: $location"
            break
        fi
    done
    
    if [ "$KAFKA_FOUND" = false ]; then
        print_error "Kafka not found. Please install Kafka 3.6+ or set KAFKA_HOME."
        print_info "Download from: https://kafka.apache.org/downloads"
        exit 1
    fi
fi

# Make scripts executable
print_step "Making scripts executable..."
chmod +x scripts/start-kafka-kraft.sh 2>/dev/null || true

# Start Kafka
print_step "Starting Kafka with KRaft..."
if [ -f "scripts/start-kafka-kraft.sh" ]; then
    ./scripts/start-kafka-kraft.sh start
else
    print_warning "Kafka script not found. Starting manually..."
    
    # Create minimal setup
    mkdir -p kafka-data kafka-logs kafka-config
    
    # Generate cluster ID
    CLUSTER_ID=$(kafka-storage.sh random-uuid)
    
    # Create minimal server.properties
    cat > kafka-config/server.properties << EOF
process.roles=broker,controller
node.id=1
controller.quorum.voters=1@localhost:9093
listeners=PLAINTEXT://localhost:9092,CONTROLLER://localhost:9093
inter.broker.listener.name=PLAINTEXT
advertised.listeners=PLAINTEXT://localhost:9092
controller.listener.names=CONTROLLER
listener.security.protocol.map=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
log.dirs=./kafka-data/kraft-combined-logs
num.network.threads=3
num.io.threads=8
default.replication.factor=1
offsets.topic.replication.factor=1
transaction.state.log.replication.factor=1
transaction.state.log.min.isr=1
EOF

    # Format storage
    kafka-storage.sh format -t "$CLUSTER_ID" -c kafka-config/server.properties
    
    # Start Kafka
    nohup kafka-server-start.sh kafka-config/server.properties > kafka-logs/kafka.log 2>&1 &
    
    # Wait for Kafka to start
    print_info "Waiting for Kafka to start..."
    sleep 10
    
    # Create topics
    kafka-topics.sh --create --bootstrap-server localhost:9092 --topic transactions --partitions 3 --replication-factor 1 || true
    kafka-topics.sh --create --bootstrap-server localhost:9092 --topic fraud-alerts --partitions 3 --replication-factor 1 || true
fi

# Compile the application
print_step "Compiling FraudLens application..."
mvn clean compile -q

# Start the application
print_step "Starting FraudLens application..."
print_info "The application will start automatically generating transactions..."
print_info "Watch for colorful fraud alerts in the terminal!"
print_info ""
print_info "Demo API endpoints:"
print_info "- Generate fraud: curl -X POST http://localhost:8080/api/demo/fraud/ACC-001"
print_info "- Generate normal: curl -X POST http://localhost:8080/api/demo/normal/10"
print_info "- Check status: curl http://localhost:8080/api/demo/status"
print_info ""
print_warning "Press Ctrl+C to stop the demo"
print_info ""

# Run the application
mvn spring-boot:run

# Cleanup function
cleanup() {
    print_info "Cleaning up..."
    pkill -f "kafka.Kafka" || true
    print_success "Demo stopped. Thanks for using FraudLens!"
}

# Set trap for cleanup
trap cleanup EXIT 