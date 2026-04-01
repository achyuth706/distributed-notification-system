# Distributed Notification System

A production-grade, event-driven notification delivery platform built with Spring Boot, Apache Kafka, Redis, PostgreSQL, React, and Kubernetes.

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                          React Dashboard                             │
│             (Vite + TypeScript + Recharts + WebSocket)               │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ REST + WebSocket (STOMP)
┌───────────────────────────────▼─────────────────────────────────────┐
│                         API Gateway :8080                            │
│         REST API · Kafka Producer · Redis Rate Limiting              │
│              WebSocket Broadcast · Swagger UI                        │
└──────┬────────────┬──────────────┬────────────────────────────────┘
       │            │              │
  notifications  notifications  notifications
     .email        .sms           .push
       │            │              │
┌──────▼────────────▼──────────────▼──────────────────────────────────┐
│                    Notification Service :8081                         │
│        Kafka Consumers · Retry (exp backoff) · Micrometer            │
│        Template Engine · Dead-Letter Publisher                        │
└──────┬──────────────────────┬────────────────┬─────────────────────┘
       │                      │                │
┌──────▼──────┐    ┌──────────▼────┐  ┌────────▼──────┐
│Email :8082  │    │  SMS :8083    │  │ Push :8084    │
│  Mailhog    │    │ Simulated     │  │ Simulated     │
│  JavaMail   │    │ Twilio SID    │  │ Firebase FCM  │
└─────────────┘    └───────────────┘  └───────────────┘

Infrastructure: Kafka · Zookeeper · Redis · PostgreSQL · Prometheus · Grafana · Mailhog
```

## Services

| Service | Port | Description |
|---------|------|-------------|
| api-gateway | 8080 | REST API, Kafka producer, WebSocket, rate limiting |
| notification-service | 8081 | Kafka consumers, retry logic, dead-letter handling |
| email-service | 8082 | Email delivery via JavaMailSender → Mailhog |
| sms-service | 8083 | Simulated SMS (Twilio-style SID, in-memory log) |
| push-service | 8084 | Simulated Firebase push (token registry) |
| Prometheus | 9090 | Metrics scraping for all services |
| Grafana | 3001 | Dashboards (anonymous access enabled) |
| Mailhog | 8025 | Local email UI — catch all outbound emails |
| Dashboard | 3000 | React real-time monitoring UI |

## Quick Start

### Prerequisites

- Docker Desktop
- Node.js 18+ (for dashboard)
- JDK 21 + Maven 3.9+ (to build JARs locally, optional with Docker)

### Run with Docker Compose

```bash
# Clone and start everything
git clone <repo-url>
cd distributed-notification-system

# Start all infrastructure + services
make dev

# In a separate terminal, start the React dashboard
make dashboard
```

Services take ~60s to be fully ready. Then open:

| URL | Description |
|-----|-------------|
| http://localhost:8080/swagger-ui.html | API documentation |
| http://localhost:3000 | Real-time dashboard |
| http://localhost:8025 | Mailhog email UI |
| http://localhost:3001 | Grafana (admin/admin) |
| http://localhost:9090 | Prometheus |

### Send a test notification

```bash
# Single notification (email + SMS + push)
make test-single

# Or with curl directly
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user_001",
    "channels": ["EMAIL", "SMS", "PUSH"],
    "priority": "HIGH",
    "templateId": "WELCOME",
    "variables": {"firstName": "Alice"},
    "subject": "Welcome!",
    "body": "Hello Alice, welcome to the platform."
  }'

# Check system stats
make stats

# Run 1000-request load test
make test
```

## Kafka Topics

| Topic | Partitions | Description |
|-------|-----------|-------------|
| notifications.email | 3 | Email delivery queue |
| notifications.sms | 3 | SMS delivery queue |
| notifications.push | 3 | Push notification queue |
| notifications.dead-letter | 3 | Failed messages after 3 retries |

## Notification Flow

```
POST /api/v1/notifications
  → Redis rate limit check (100 req/user/hour)
  → Persist to PostgreSQL (status: PENDING)
  → Publish to Kafka topic (per channel)
    → notification-service consumes
      → Apply template variable substitution
      → Route to channel worker topic
        → email/sms/push-service consumes and delivers
          → Update PostgreSQL status (SENT / FAILED)
          → Broadcast via WebSocket to dashboard
          → On failure: exponential backoff (1s → 2s → 4s)
            → After 3 failures: publish to dead-letter topic
```

## API Reference

### Send Notification

```http
POST /api/v1/notifications
Content-Type: application/json

{
  "userId": "string",
  "channels": ["EMAIL", "SMS", "PUSH"],
  "templateId": "WELCOME | ALERT | TRANSACTION",
  "variables": { "firstName": "Alice", "amount": "$50" },
  "priority": "CRITICAL | HIGH | NORMAL | LOW",
  "subject": "string",
  "body": "string"
}
```

### Other Endpoints

```
GET  /api/v1/notifications/{id}          - Get notification status
GET  /api/v1/notifications/user/{userId} - Get user history
GET  /api/v1/notifications/stats         - System statistics
GET  /api/v1/notifications/stream        - Last 50 events (Redis cache)
GET  /api/v1/email/metrics               - Email service counters
GET  /api/v1/sms/sent                    - All sent SMS (in-memory)
GET  /api/v1/push/tokens                 - Push token registry
WS   ws://localhost:8080/ws              - STOMP WebSocket feed
```

## Rate Limiting

- Max **100 notifications per user per hour** (enforced via Redis)
- Exceeding the limit returns HTTP **429** with a `RATE_LIMITED` status

## Templates

Three built-in templates (variable substitution with `{{key}}` syntax):

| ID | Variables |
|----|-----------|
| WELCOME | `firstName` |
| ALERT | `firstName`, `alertCode` |
| TRANSACTION | `firstName`, `amount`, `transactionId`, `date` |

## Makefile Targets

```bash
make dev            # docker compose up (full stack)
make dev-infra      # infrastructure only (Kafka, Redis, Postgres, etc.)
make dev-logs       # tail application service logs
make build          # build all Maven projects
make build-docker   # build all Docker images
make deploy         # deploy to Kubernetes (runs k8s/deploy.sh)
make test           # 1000-request load test
make test-single    # send one test notification
make stats          # print system stats
make clean          # stop everything, delete k8s namespace, clean builds
make dashboard      # start React dev server (npm install + vite)
make dashboard-build # production build of React dashboard
```

## Kubernetes Deployment

```bash
# Build images and deploy to cluster
make deploy

# Or manually
bash k8s/deploy.sh

# Port-forward Prometheus/Grafana
kubectl port-forward svc/prometheus 9090:9090 -n notification-system
kubectl port-forward svc/grafana    3001:3000 -n notification-system
```

Each service is configured with:
- **2 replicas** minimum
- **HorizontalPodAutoscaler**: min 2, max 10, CPU threshold 70%
- Resource limits: 512Mi memory, 500m CPU
- Liveness + readiness probes on `/actuator/health`

## Project Structure

```
.
├── .env                          # Environment variable defaults
├── docker-compose.yml            # Full local stack
├── Makefile                      # Developer shortcuts
├── scripts/
│   └── init-db.sql               # PostgreSQL schema
├── monitoring/
│   ├── prometheus.yml            # Scrape config
│   └── grafana/                  # Provisioning (datasource + dashboard)
├── services/
│   ├── shared-lib/               # Shared DTOs and enums (Maven jar)
│   ├── api-gateway/              # Spring Boot 3.2, port 8080
│   ├── notification-service/     # Spring Boot 3.2, port 8081
│   ├── email-service/            # Spring Boot 3.2, port 8082
│   ├── sms-service/              # Spring Boot 3.2, port 8083
│   └── push-service/             # Spring Boot 3.2, port 8084
├── dashboard/                    # React + Vite + TypeScript
│   └── src/
│       ├── App.tsx
│       ├── components/           # Sidebar, KpiCard, StatusBadge, ChannelIcon
│       └── pages/                # Dashboard, LiveFeed, Analytics, DeadLetters, Settings
└── k8s/
    ├── namespace.yaml
    ├── configmap.yaml
    ├── api-gateway/              # deployment, service, hpa, ingress
    ├── notification-service/     # deployment, service, hpa
    ├── email-service/            # deployment, service, hpa
    ├── sms-service/              # deployment, service, hpa
    ├── push-service/             # deployment, service, hpa
    ├── infrastructure/           # Kafka + Zookeeper StatefulSets
    ├── monitoring/               # Prometheus deployment + config
    └── deploy.sh                 # Build + apply + rollout script
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| API | Spring Boot 3.2, Spring Web, SpringDoc OpenAPI |
| Messaging | Apache Kafka 7.5, Spring Kafka |
| Caching / Rate limiting | Redis 7, Spring Data Redis |
| Database | PostgreSQL 15, Spring Data JPA |
| Real-time | WebSocket, STOMP, SockJS |
| Metrics | Micrometer, Prometheus, Grafana |
| Email (local) | Mailhog, JavaMailSender |
| Frontend | React 18, Vite, TypeScript, Recharts |
| Containers | Docker, Docker Compose |
| Orchestration | Kubernetes, nginx Ingress, HPA |
| Build | Maven 3.9, JDK 21, Node.js 18 |
