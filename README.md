# CloudStorage Enterprise 

CloudStorage is a commercial-grade, multi-tenant SaaS application offering intelligent file management, AI-driven document insights, and enterprise collaboration features.

## 🚀 Phase 8: Distributed Microservices Architecture
The application has been completely refactored from a monolithic Spring Boot architecture into a massive-scale, cloud-native distributed system.

### Key Architectural Upgrades
- **Microservices Orchestration**: Broken down into domain-driven independent services (Auth, Users, Files, Billing, AI, Webhooks).
- **API Gateway**: Integrated **Spring Cloud Gateway** for centralized routing, JWT validation, and Redis-backed rate limiting.
- **Service Registry**: Deployed **Netflix Eureka** for dynamic DNS and microservice auto-registration.
- **Event-Driven Pub/Sub**: Replaced synchronous blocking calls with **Apache Kafka** event topics (`FileUploadedEvent`, `DocumentAnalyzedEvent`) for massive throughput.
- **Distributed Caching**: Deployed a **Redis Cluster** for lightning-fast session, API, and metadata caching.
- **Search Engine**: **Elasticsearch** is fully configured for deep full-text indexing, autocomplete, and fuzziness over AI/OCR extracted data.

### ☸️ Kubernetes & Infrastructure as Code
This project provides a true production deployment matrix:
- **Terraform (`/terraform`)**: Contains AWS configuration scripts for spinning up EKS (Elastic Kubernetes Service), RDS PostgreSQL clusters, S3 buckets, and ElastiCache.
- **Kubernetes Manifests (`/k8s`)**: Complete declarative configuration for Deployments, ConfigMaps, Secrets, Ingress Controllers, and HPA (Horizontal Pod Autoscalers).
- **Docker Compose Enterprise**: A robust local orchestration script spinning up the entire microservices mesh + Zookeeper, Kafka, Redis, Elasticsearch, and the Monitoring Stack.

### 📊 Observability & Monitoring
- **Prometheus & Grafana**: Time-series metric aggregation visualizing API Latency, JVM Memory, and Kafka throughput.
- **Zipkin / OpenTelemetry**: Distributed tracing injected into every service, allowing complete visibility of requests as they travel from the Gateway through Kafka to the Database.

## System Architecture

Check out the diagrams in the repository root for detailed visual overviews:
- `architecture-microservices.mermaid`: Service Mesh and API Gateway routing.
- `architecture-k8s.mermaid`: Kubernetes Cluster, Nodes, Pods, and Ingress.
- `event-flow.mermaid`: Kafka Pub/Sub asynchronous flow.
- `ER_diagram.mermaid`: The multi-tenant organizational data model.

## Quick Start (Enterprise Stack)

1. **Start the Infrastructure**
```bash
docker-compose -f docker-compose-enterprise.yml up -d
```

2. **Start the Service Registry & API Gateway**
```bash
cd cloudstorage-eureka-server
./mvnw spring-boot:run

cd ../cloudstorage-api-gateway
./mvnw spring-boot:run
```

3. **Start Core Services**
```bash
cd cloudstorage
./mvnw clean spring-boot:run -Dspring-boot.run.profiles=prod
```

4. **Access the Application**
Navigate to `http://localhost:8080/` (API Gateway). The Gateway will automatically route your requests to the downstream services securely.