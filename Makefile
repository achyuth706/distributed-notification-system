.PHONY: dev dev-infra dev-logs build build-docker deploy test test-single stats clean dashboard dashboard-build help

SERVICES = api-gateway notification-service email-service sms-service push-service
REGISTRY  = nds
TAG       = latest

help: ## Show available targets
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-18s\033[0m %s\n", $$1, $$2}'
	@echo ""
	@echo "  Ports:  API=8080  NotifSvc=8081  Email=8082  SMS=8083  Push=8084"
	@echo "          Grafana=3001  Prometheus=9090  Mailhog=8025  Dashboard=3000"

dev: ## Start everything with Docker Compose
	docker compose up -d
	@echo ""
	@echo "  Starting (allow ~60s for Spring Boot services to be ready)"
	@echo "  API Gateway:  http://localhost:8080/swagger-ui.html"
	@echo "  Grafana:      http://localhost:3001  (admin/admin)"
	@echo "  Mailhog:      http://localhost:8025"

dev-infra: ## Start only infrastructure (Kafka, Redis, Postgres, Mailhog, monitoring)
	docker compose up -d zookeeper kafka kafka-init redis postgres mailhog prometheus grafana

dev-logs: ## Tail logs for all application services
	docker compose logs -f api-gateway notification-service email-service sms-service push-service

build: ## Build all Spring Boot services with Maven (requires JDK 21 + Maven 3.9+)
	@echo "Installing shared-lib..."
	cd services/shared-lib && mvn install -q -DskipTests
	@for svc in $(SERVICES); do \
		echo "Building $$svc..."; \
		cd services/$$svc && mvn package -q -DskipTests && cd ../..; \
	done
	@echo "All services built."

build-docker: ## Build all Docker images (requires shared-lib available)
	@for svc in $(SERVICES); do \
		echo "Building $(REGISTRY)/$$svc:$(TAG)..."; \
		docker build -t $(REGISTRY)/$$svc:$(TAG) -f services/$$svc/Dockerfile services/; \
	done
	@echo "All images built."

deploy: ## Deploy to Kubernetes cluster
	@chmod +x k8s/deploy.sh && bash k8s/deploy.sh

test: ## Load test: send 1000 notifications (requires curl)
	@echo "Sending 1000 notifications to http://localhost:8080 ..."
	@SUCCESS=0; FAIL=0; \
	for i in $$(seq 1 1000); do \
		USER="user_$$(printf '%04d' $$((RANDOM % 200)))"; \
		if   [ $$((i % 3)) -eq 0 ]; then CH='"SMS"';  \
		elif [ $$((i % 5)) -eq 0 ]; then CH='"PUSH"'; \
		else CH='"EMAIL"'; fi; \
		CODE=$$(curl -s -o /dev/null -w "%{http_code}" \
			-X POST http://localhost:8080/api/v1/notifications \
			-H "Content-Type: application/json" \
			-d "{\"userId\":\"$$USER\",\"channels\":[$$CH],\"priority\":\"NORMAL\",\"subject\":\"Load test\",\"body\":\"Notification $$i\"}"); \
		if [ "$$CODE" = "201" ]; then SUCCESS=$$((SUCCESS+1)); else FAIL=$$((FAIL+1)); fi; \
		if [ $$((i % 100)) -eq 0 ]; then echo "  $$i/1000  ok=$$SUCCESS err=$$FAIL"; fi; \
	done; \
	echo ""; echo "  Result: $$SUCCESS ok, $$FAIL failed."

test-single: ## Send one test notification and pretty-print the response
	curl -s -X POST http://localhost:8080/api/v1/notifications \
		-H "Content-Type: application/json" \
		-d '{"userId":"demo_user","channels":["EMAIL","SMS","PUSH"],"priority":"HIGH","subject":"Hello NDS","body":"Test notification","templateId":"WELCOME","variables":{"firstName":"Demo"}}' \
		| python3 -m json.tool 2>/dev/null || cat

stats: ## Show system stats from API Gateway
	curl -s http://localhost:8080/api/v1/notifications/stats | python3 -m json.tool 2>/dev/null || cat

clean: ## Stop Docker Compose, delete k8s namespace, clean Maven targets
	@echo "Stopping Docker Compose..."
	docker compose down -v --remove-orphans 2>/dev/null || true
	@echo "Deleting Kubernetes namespace..."
	kubectl delete namespace notification-system --ignore-not-found 2>/dev/null || true
	@echo "Cleaning Maven artifacts..."
	@for svc in shared-lib $(SERVICES); do \
		[ -f "services/$$svc/pom.xml" ] && (cd services/$$svc && mvn clean -q 2>/dev/null) || true; \
	done
	@echo "Done."

dashboard: ## Start React dashboard dev server (requires Node.js 18+)
	cd dashboard && npm install && npm run dev

dashboard-build: ## Build React dashboard for production
	cd dashboard && npm install && npm run build
