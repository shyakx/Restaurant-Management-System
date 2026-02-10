# Restaurant Management System

A modern microservices restaurant management system using Spring Boot, Eureka, API Gateway, Kafka, and PostgreSQL.

## Architecture

- **Eureka Server**: Service discovery and registration (port 8761)
- **API Gateway**: Single entry point for all requests (port 8081)
- **Order Service**: Handles customer orders and publishes events (port 8082)
- **Kitchen Service**: Manages food preparation and consumes order events (port 8083)
- **Notification Service**: Sends email notifications for order events (port 8084)
- **PostgreSQL**: Database persistence for all services
- **Kafka**: Event-driven communication between services

## Event Flow

1. Order Service receives customer orders
2. Order Service publishes ORDER_PLACED events to Kafka
3. Kitchen Service consumes events and starts food preparation
4. Kitchen Service publishes ORDER_READY events to Kafka
5. Notification Service consumes all order events and sends emails
6. API Gateway routes all external requests to appropriate services

## Technologies

- Spring Boot 3.2.0 (Kotlin)
- Spring Cloud 2023.0.0
- PostgreSQL Database
- Apache Kafka
- Docker & Docker Compose
- Spring Cloud Gateway
- Spring Cloud Eureka
- Java 17

## Quick Start

```bash
# Start infrastructure
docker-compose up -d postgres zookeeper kafka

# Start services (in separate terminals)
cd eureka-server && ./gradlew bootRun
cd api-gateway && ./gradlew bootRun
cd order-service && ./gradlew bootRun
cd kitchen-service && ./gradlew bootRun
cd notification-service && ./gradlew bootRun
```

## Access Points

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8081
- **Order Service**: http://localhost:8082
- **Kitchen Service**: http://localhost:8083
- **Notification Service**: http://localhost:8084

## API Examples

### Order Service (via API Gateway)
- GET /api/orders - Get all orders
- POST /api/orders - Create new order
- GET /api/orders/{id} - Get order by ID

### Kitchen Service (via API Gateway)
- GET /api/kitchen/orders - Get all kitchen orders
- GET /api/kitchen/orders/{id} - Get kitchen order by ID

### Notification Service (via API Gateway)
- GET /api/notifications - Get notification status
- GET /actuator/health - Service health check

## Service Health

All services expose Spring Boot Actuator endpoints:
- `/actuator/health` - Service health status
- `/actuator/info` - Service information
- `/actuator/metrics` - Service metrics
