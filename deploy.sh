#!/bin/bash

# ===================================================================
# FraudLens - Complete Deployment Script
# Quarkus + GitHub Actions + Grafana + Kubernetes
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
print_header "🚀 FraudLens - Complete Deployment"
print_header "═══════════════════════════════════════════════════════════"

# Check prerequisites
print_step "Checking prerequisites..."

# Check kubectl
if ! command -v kubectl &> /dev/null; then
    print_error "kubectl not found. Please install kubectl."
    exit 1
fi
print_success "kubectl found"

# Check helm
if ! command -v helm &> /dev/null; then
    print_warning "helm not found. Installing helm..."
    curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
fi
print_success "helm found"

# Check if cluster is accessible
if ! kubectl cluster-info &> /dev/null; then
    print_error "Cannot connect to Kubernetes cluster. Please check your kubeconfig."
    exit 1
fi
print_success "Kubernetes cluster accessible"

# Deploy monitoring stack
print_step "Deploying monitoring stack..."

# Add Prometheus Helm repository
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo add grafana https://grafana.github.io/helm-charts
helm repo update

# Create monitoring namespace
kubectl create namespace monitoring --dry-run=client -o yaml | kubectl apply -f -

# Install Prometheus
print_info "Installing Prometheus..."
helm upgrade --install prometheus prometheus-community/kube-prometheus-stack \
    --namespace monitoring \
    --set grafana.adminPassword=admin123 \
    --set prometheus.prometheusSpec.retention=30d \
    --set alertmanager.alertmanagerSpec.retention=30d \
    --wait

print_success "Prometheus installed"

# Deploy FraudLens application
print_step "Deploying FraudLens application..."

# Create production namespace
kubectl apply -f k8s/production/namespace.yaml

# Apply configuration
kubectl apply -f k8s/production/configmap.yaml

# Deploy application
kubectl apply -f k8s/production/deployment.yaml

# Apply ingress
kubectl apply -f k8s/production/ingress.yaml

print_success "FraudLens application deployed"

# Wait for deployment to be ready
print_info "Waiting for deployment to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/fraudlens-app -n fraudlens-production

# Get service information
print_step "Getting service information..."
SERVICE_IP=$(kubectl get service fraudlens-service -n fraudlens-production -o jsonpath='{.spec.clusterIP}')
SERVICE_PORT=$(kubectl get service fraudlens-service -n fraudlens-production -o jsonpath='{.spec.ports[0].port}')

print_info "FraudLens service available at: http://${SERVICE_IP}:${SERVICE_PORT}"
print_info "Health check: http://${SERVICE_IP}:${SERVICE_PORT}/q/health"
print_info "Metrics: http://${SERVICE_IP}:${SERVICE_PORT}/q/metrics"

# Get Grafana access information
print_step "Getting Grafana access information..."
GRAFANA_POD=$(kubectl get pods -n monitoring -l app.kubernetes.io/name=grafana -o jsonpath='{.items[0].metadata.name}')
GRAFANA_PORT=$(kubectl get service grafana -n monitoring -o jsonpath='{.spec.ports[0].port}')

print_info "Grafana available at: http://localhost:3000"
print_info "Username: admin"
print_info "Password: admin123"

# Port forward Grafana
print_info "Setting up port forwarding for Grafana..."
kubectl port-forward -n monitoring svc/grafana 3000:80 &
GRAFANA_PID=$!

print_success "Grafana port forwarding started (PID: $GRAFANA_PID)"

# Import Grafana dashboards
print_step "Importing Grafana dashboards..."

# Wait for Grafana to be ready
sleep 10

# Import dashboards
print_info "Importing Fraud Detection Overview dashboard..."
curl -X POST \
  -H "Content-Type: application/json" \
  -d @grafana/dashboards/fraud-detection-overview.json \
  http://admin:admin123@localhost:3000/api/dashboards/db

print_info "Importing System Performance dashboard..."
curl -X POST \
  -H "Content-Type: application/json" \
  -d @grafana/dashboards/system-performance.json \
  http://admin:admin123@localhost:3000/api/dashboards/db

print_info "Importing Business Intelligence dashboard..."
curl -X POST \
  -H "Content-Type: application/json" \
  -d @grafana/dashboards/business-intelligence.json \
  http://admin:admin123@localhost:3000/api/dashboards/db

print_success "Grafana dashboards imported"

# Show final status
print_step "Deployment Status:"
kubectl get pods -n fraudlens-production
kubectl get services -n fraudlens-production
kubectl get ingress -n fraudlens-production

print_header "🎉 Deployment Complete!"
print_info "Access points:"
print_info "- FraudLens API: http://${SERVICE_IP}:${SERVICE_PORT}"
print_info "- Grafana: http://localhost:3000 (admin/admin123)"
print_info "- Prometheus: http://localhost:9090"
print_info ""
print_info "To stop port forwarding: kill $GRAFANA_PID"
print_info "To view logs: kubectl logs -f deployment/fraudlens-app -n fraudlens-production"
print_info "To scale: kubectl scale deployment fraudlens-app --replicas=5 -n fraudlens-production"
