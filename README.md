# Restaurant Management System

A modern microservices restaurant management system using Spring Boot, Eureka, Kafka, and PostgreSQL.

## Architecture

- **Eureka Server**: Service discovery and registration (port 8761)
- **Order Service**: Handles customer orders and publishes events (port 8082)
- **Kitchen Service**: Manages food preparation and consumes order events (port 8083)
- **Notification Service**: Sends email notifications for order events (port 8084)
- **Redis**: Caching layer for frequently accessed data (port 6379)
- **PostgreSQL**: Database persistence for all services
- **Kafka**: Event-driven communication between services

## Event Flow

1. Order Service receives customer orders
2. Order Service publishes ORDER_PLACED events to Kafka
3. Kitchen Service consumes events and starts food preparation
4. Kitchen Service publishes ORDER_READY events to Kafka
5. Notification Service consumes all order events and sends emails

## Technologies

- Spring Boot 3.2.0 (Kotlin)
- Spring Cloud 2023.0.0
- PostgreSQL Database
- Apache Kafka
- Redis Cache
- Docker & Docker Compose
- Spring Cloud Eureka
- Java 17

## Quick Start

### 1. Start Infrastructure
```bash
docker-compose up -d postgres zookeeper kafka redis
```

### 2. Start Services
Run each service in IntelliJ or terminal:
- EurekaServerApplication (port 8761)
- OrderServiceApplication (port 8082)
- KitchenServiceApplication (port 8083)
- NotificationServiceApplication (port 8084)

### 3. Environment Setup
Create `.env` file in project root:

```env
# Database
POSTGRES_DB=restaurant_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_password

# Gmail (Notification Service)
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=shyakasteven2023@gmail.com
SPRING_MAIL_PASSWORD=your_gmail_app_password
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Eureka
EUREKA_SERVER_URL=http://localhost:8761/eureka
```

**Important**: Use Gmail App Password, not regular password.

## Caching Strategy

### Redis Implementation
- **Order Service**: Caches order statistics and frequently accessed orders
- **Kitchen Service**: Caches dashboard statistics and active orders  
- **Cache TTL**: 10 minutes auto-expiration
- **Cache Eviction**: Automatic on data updates

### Cached Endpoints
- `GET /api/orders/statistics` - Order statistics (cached)
- `GET /api/kitchen/dashboard/stats` - Kitchen dashboard (cached)
- `GET /api/kitchen/orders` - Active kitchen orders (cached)

## Access Points

- **Eureka Dashboard**: http://localhost:8761
- **Order Service**: http://localhost:8082
- **Kitchen Service**: http://localhost:8083
- **Notification Service**: http://localhost:8084

## API Testing Guide

### Order Service (8082)

#### Create Order
```
POST http://localhost:8082/api/orders
Content-Type: application/json

{
  "customerName": "Jean Mugabo",
  "customerEmail": "shyastyve@gmail.com",
  "customerPhone": "+250788123456",
  "items": [
    {"menuItemId": 1, "quantity": 2},
    {"menuItemId": 2, "quantity": 1}
  ]
}
```

#### Get Orders
- `GET http://localhost:8082/api/orders` - All orders
- `GET http://localhost:8082/api/orders/1` - By ID
- `GET http://localhost:8082/api/orders/status/PENDING` - By status
- `GET http://localhost:8082/api/orders/customer/shyastyve@gmail.com` - By email
- `GET http://localhost:8082/api/orders/statistics` - Stats

#### Update Status
```
PUT http://localhost:8082/api/orders/1/status
Content-Type: application/json

{"status": "CONFIRMED"}
```

**Statuses**: PENDING, CONFIRMED, PREPARING, READY, COMPLETED, CANCELLED

### Kitchen Service (8083)

#### Get Kitchen Orders
- `GET http://localhost:8083/api/kitchen/orders` - All orders
- `GET http://localhost:8083/api/kitchen/orders/1` - By ID
- `GET http://localhost:8083/api/kitchen/orders/status/RECEIVED` - By status
- `GET http://localhost:8083/api/kitchen/dashboard/stats` - Dashboard stats

#### Update Kitchen Status
- `PUT http://localhost:8083/api/kitchen/orders/1/start-preparation` - Start prep
- `PUT http://localhost:8083/api/kitchen/orders/1/ready` - Mark ready
- `PUT http://localhost:8083/api/kitchen/orders/1/complete` - Complete

**Statuses**: RECEIVED, IN_PREPARATION, READY, COMPLETED

### Notification Service (8084)

#### Test Email
```
POST http://localhost:8084/api/notifications/test-email
Content-Type: application/json

{
  "to": "shyastyve@gmail.com",
  "subject": "Test Email",
  "message": "This is a test from Restaurant System"
}
```

#### Health Check
- `GET http://localhost:8084/api/notifications/health` - Service health

## Health Checks

Always test health endpoints first:
- `GET http://localhost:8082/actuator/health` - Order Service
- `GET http://localhost:8083/actuator/health` - Kitchen Service  
- `GET http://localhost:8084/actuator/health` - Notification Service
- `GET http://localhost:8761` - Eureka Server

## Test Workflow

### Complete Order Flow
1. Create order → Note order ID
2. Check order appears in kitchen (status: RECEIVED)
3. Start preparation → Status: IN_PREPARATION
4. Mark as ready → Status: READY
5. Complete order → Status: COMPLETED
6. Verify final status in order service

### Email Testing
1. Configure Gmail in `.env` (use App Password)
2. Send test email via notification service
3. Create order with your email → Auto-notification sent

## Troubleshooting

### Common Issues
- **Connection refused**: Services not running
- **Database errors**: PostgreSQL down or wrong credentials
- **Kafka errors**: Kafka not running on 9092
- **Email fails**: Wrong Gmail password (use App Password)

### Dependencies
- All services need Eureka (8761)
- Order/Kitchen need PostgreSQL + Kafka
- Notification needs Kafka + email config

## Response Examples

### Order Response
```json
{
  "id": 1,
  "customerName": "Jean Mugabo",
  "customerEmail": "shyastyve@gmail.com",
  "customerPhone": "+250788123456",
  "status": "PENDING",
  "totalAmount": 8500.00,
  "items": [...],
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": null
}
```

### Kitchen Order Response
```json
{
  "orderId": 1,
  "customerName": "Jean Mugabo",
  "customerEmail": "shyastyve@gmail.com",
  "status": "RECEIVED",
  "totalAmount": 8500.00,
  "items": [...],
  "receivedAt": "2024-01-15T10:30:00",
  "startedPreparationAt": null,
  "completedAt": null,
  "estimatedCompletionTime": null
}
```

## Advanced Testing

### Load Testing
Use Postman Collection Runner for:
- Multiple order creations
- Concurrent kitchen operations
- Notification throughput

### Integration Testing
Verify microservice communication:
1. Order → Kafka event → Kitchen service
2. Kitchen → Kafka event → Notification service
3. Service discovery via Eureka

## Service Health

All services expose Spring Boot Actuator endpoints:
- `/actuator/health` - Service health status
- `/actuator/info` - Service information
- `/actuator/metrics` - Service metrics
