# Restaurant Management System

A modern microservices restaurant management system built with Spring Boot, featuring service discovery, event-driven architecture, and containerized deployment.

## Architecture Overview

```
┌─────────────┐    ┌──────────────┐    ┌─────────────────┐
│   Client    │───▶│  API Gateway  │───▶│  Eureka Server │
└─────────────┘    └──────────────┘    └─────────────────┘
                           │                    │
                           ▼                    ▼
                   ┌──────────────┐    ┌─────────────────┐
                   │ Order Service│    │Kitchen Service  │
                   └──────────────┘    └─────────────────┘
                           │                    │
                           ▼                    ▼
                   ┌──────────────┐    ┌─────────────────┐
                   │ PostgreSQL   │    │  Notification  │
                   │   Database  │    │    Service     │
                   └──────────────┘    └─────────────────┘
                           │                    │
                           ▼                    ▼
                   ┌──────────────┐    ┌─────────────────┐
                   │    Redis    │    │     Kafka      │
                   │    Cache    │    │   Message      │
                   └──────────────┘    │    Broker      │
                                      └─────────────────┘
```

### Services

- **API Gateway** (port 8081) - Single entry point, routing, and load balancing
- **Eureka Server** (port 8761) - Service discovery and registration
- **Order Service** (port 8082) - Customer order management and validation
- **Kitchen Service** (port 8083) - Food preparation and order processing
- **Notification Service** (port 8084) - **Email notifications and alerts**

### Infrastructure

- **PostgreSQL** (port 5432) - Primary database for all services
- **Redis** (port 6379) - Caching layer for performance optimization
- **Kafka** (port 9092) - Event-driven communication between services
- **Zookeeper** (port 2181) - Kafka coordination service

## Tech Stack

- **Backend**: Spring Boot 3.2.0 + Kotlin
- **Database**: PostgreSQL 15
- **Cache**: Redis 7
- **Messaging**: Kafka 7.5
- **Container**: Docker + Docker Compose
- **Java**: OpenJDK 17

## Quick Start

### Prerequisites

- Docker and Docker Compose installed
- Java 17+ (for local development)
- Git (for cloning)

### 1. Environment Setup

Copy the environment template and configure your values:

```bash
cp .env.example .env
```

Edit `.env` with your configuration:

```bash
# Database Configuration
POSTGRES_DB=restaurant_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_secure_password

# Service Ports (customize if needed)
API_GATEWAY_PORT=8081
ORDER_SERVICE_PORT=8082
KITCHEN_SERVICE_PORT=8083
NOTIFICATION_SERVICE_PORT=8084
EUREKA_SERVER_PORT=8761
```

### 2. Build Services

Build all Spring Boot applications:

```bash
# Build each service
cd api-gateway && ./gradlew build -x test
cd ../eureka-server && ./gradlew build -x test
cd ../order-service && ./gradlew build -x test
cd ../kitchen-service && ./gradlew build -x test
cd ../notification-service && ./gradlew build -x test
```

### 3. Start System

Launch all services with environment variables:

```bash
docker-compose -f docker-compose.env.yml --env-file .env up -d
```

### 4. Verify Deployment

Check that all services are running:

```bash
docker-compose -f docker-compose.env.yml ps
```

Test individual services:

```bash
# Eureka Dashboard
curl http://localhost:8761

# API Gateway Health
curl http://localhost:8081/actuator/health

# Order Service Health
curl http://localhost:8082/actuator/health
```

## API Endpoints

**Main Access Points:**
- API Gateway: http://localhost:8081
- Eureka Dashboard: http://localhost:8761
- Kafka UI: http://localhost:8090

**Order API:**
- `POST /api/orders` - Create order
- `GET /api/orders` - List orders (paginated)
- `GET /api/orders/{id}` - Get order by ID
- `PUT /api/orders/{id}/status` - Update order status

**Kitchen API:**
- `GET /api/kitchen/orders` - List kitchen orders
- `PUT /api/kitchen/orders/{id}/start-preparation` - Start prep
- `PUT /api/kitchen/orders/{id}/ready` - Mark as ready
- `PUT /api/kitchen/orders/{id}/complete` - Complete order

**Order Flow:** `PENDING → CONFIRMED → PREPARING → READY → COMPLETED`

## Testing

### Postman Collection

Import the provided Postman collection for API testing:

```bash
# Import this file into Postman
Restaurant-Management-API.postman_collection.json
```

The collection includes:
- Health checks for all services
- Complete order workflow
- Error scenario testing
- Automated test scripts

### Manual Testing

1. **System Health Check**
   ```bash
   curl http://localhost:8761  # Eureka should show registered services
   ```

2. **Order Creation Test**
   ```bash
   curl -X POST http://localhost:8081/api/orders \
     -H "Content-Type: application/json" \
     -d '{"customerName":"Test User","customerEmail":"test@example.com","items":[{"menuItemId":1,"quantity":1}]}'
   ```

3. **Kitchen Integration Test**
   ```bash
   curl http://localhost:8081/api/kitchen/orders  # Should show created orders
   ```

## Environment Configuration

### Environment Variables

The system uses environment variables for configuration. Key variables:

```bash
# Database
POSTGRES_DB=restaurant_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=password

# External URLs (for testing)
API_GATEWAY_URL_EXTERNAL=http://localhost:8081
ORDER_SERVICE_URL_EXTERNAL=http://localhost:8082

# Internal URLs (for Docker networking)
EUREKA_SERVER_URL=http://eureka-server:8761/eureka
ORDER_SERVICE_URL=http://order-service:8082

# Performance Tuning
DATABASE_MAX_POOL_SIZE=20
HTTP_CONNECT_TIMEOUT=5000
CIRCUIT_BREAKER_FAILURE_RATE_THRESHOLD=50
```

### Profiles

- **local-dev** - Development with local infrastructure
- **docker** - Production-ready containerized deployment

## Monitoring

**Health Checks:**
- All services: `/actuator/health`
- Eureka Dashboard: http://localhost:8761
- Kafka UI: http://localhost:8090

**Email Notifications:**
Automatic emails sent for order status changes (confirmation, preparing, ready, completed).

## Troubleshooting

**Common Issues:**
- **Services won't start**: Check port availability with `netstat -an | grep :808`
- **Database errors**: Check PostgreSQL logs: `docker-compose -f docker-compose.env.yml logs postgres`
- **Service discovery**: Verify Eureka: `curl http://localhost:8761/eureka/apps`

**Performance:**
- Database pooling configured in `.env`
- Redis caching enabled
- Circuit breaker prevents cascading failures

## Development

### Local Development

Run services individually for development:

```bash
# Start infrastructure first
docker-compose -f docker-compose.env.yml up -d postgres redis kafka zookeeper

# Run services in IDE
# Set SPRING_PROFILES_ACTIVE=local-dev
# Run each ApplicationKt class
```

### Code Structure

```
src/main/kotlin/com/restaurant/[service]/
├── controller/     # REST endpoints
├── service/       # Business logic
├── repository/    # Data access
├── model/         # Entity classes
└── config/        # Configuration classes
```

## Production Deployment

### Security Considerations

1. **Change default passwords** in production .env
2. **Use HTTPS** for external communication
3. **Configure firewalls** to expose only necessary ports
4. **Monitor logs** for security events
5. **Regular updates** of dependencies

### Scaling

- **Horizontal scaling**: Add more service instances
- **Database scaling**: Read replicas for high load
- **Caching**: Redis cluster for distributed caching
- **Load balancing**: Multiple API Gateway instances

## Contributing

1. Fork the repository
2. Create feature branch
3. Make changes with tests
4. Submit pull request

## License

This project is for educational purposes to demonstrate microservices architecture patterns.
