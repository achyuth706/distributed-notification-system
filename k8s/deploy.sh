#!/usr/bin/env bash
set -euo pipefail

REGISTRY="${REGISTRY:-nds}"
TAG="${TAG:-latest}"
SERVICES=(api-gateway notification-service email-service sms-service push-service)

echo "================================================"
echo "  NDS — Kubernetes Deployment"
echo "================================================"

# Build images (context is services/ so Dockerfiles can access shared-lib)
echo ""
echo ">>> Building Docker images..."
for svc in "${SERVICES[@]}"; do
  echo "  nds/${svc}:${TAG}"
  docker build -t "${REGISTRY}/${svc}:${TAG}" \
    -f "services/${svc}/Dockerfile" \
    services/
done
echo "  Done."

# Apply manifests
echo ""
echo ">>> Applying manifests..."
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/infrastructure/
for svc in "${SERVICES[@]}"; do
  [ -d "k8s/${svc}" ] && kubectl apply -f "k8s/${svc}/"
done
kubectl apply -f k8s/monitoring/
echo "  Done."

# Wait for rollouts
echo ""
echo ">>> Waiting for rollouts..."
for svc in "${SERVICES[@]}"; do
  kubectl rollout status deployment/"${svc}" \
    -n notification-system --timeout=300s \
    && echo "  ✓ ${svc}" \
    || echo "  ✗ ${svc} (timed out)"
done

echo ""
echo "================================================"
echo "  Deployment complete!"
echo "  API:     http://nds.local/api/v1"
echo "  Swagger: http://nds.local/swagger-ui.html"
echo "  Port-forward Prometheus:"
echo "    kubectl port-forward svc/prometheus 9090:9090 -n notification-system"
echo "================================================"
