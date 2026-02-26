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

## Technology Stack

- **Backend**: Spring Boot 3.2.0 with Kotlin
- **Microservices**: Spring Cloud Gateway, Eureka, Circuit Breaker
- **Database**: PostgreSQL 15 with Hibernate/JPA
- **Caching**: Redis 7 with Spring Data Redis
- **Messaging**: Apache Kafka 7.5
- **Containerization**: Docker with Docker Compose
- **Java Runtime**: OpenJDK 17

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

## API Documentation

### Access Points

- **API Gateway**: http://localhost:8081 (main entry point)
- **Eureka Dashboard**: http://localhost:8761 (service registry)
- **Kafka UI**: http://localhost:8090 (Kafka monitoring)
- **Order Service**: http://localhost:8082 (direct access)
- **Kitchen Service**: http://localhost:8083 (direct access)
- **Notification Service**: http://localhost:8084 (direct access)

### Order Management Flow

1. **Create Order** - POST `/api/orders`
   ```json
   {
     "customerName": "John Doe",
     "customerEmail": "john@example.com", 
     "customerPhone": "+1234567890",
     "items": [
       {"menuItemId": 1, "quantity": 2},
       {"menuItemId": 2, "quantity": 1}
     ]
   }
   ```

2. **Email Notifications** - Automatic emails sent to customer
   - **Order Confirmation** - Immediate after order creation
   - **Order Preparing** - When kitchen starts preparation (1 min)
   - **Order Ready** - When food is ready for pickup (3 min)
   - **Order Completed** - When order is fulfilled

3. **View Orders** - GET `/api/orders`
   - Get all orders: `GET /api/orders`
   - Get by ID: `GET /api/orders/{id}`
   - Get by status: `GET /api/orders/status/{status}`

4. **Kitchen Operations** - Via `/api/kitchen/*`
   - Start preparation: `PUT /api/kitchen/orders/{id}/start-preparation`
   - Mark ready: `PUT /api/kitchen/orders/{id}/ready`
   - Complete order: `PUT /api/kitchen/orders/{id}/complete`

### Order Status Flow

```
PENDING → CONFIRMED → PREPARING → READY → COMPLETED
    ↓           ↓           ↓        ↓         ↓
  Created    Accepted    Kitchen   Ready for   Order
            by Order    Started    Pickup     Delivered
            Service
```

## Testing

### Postman Collection

Import the provided Postman collection for comprehensive API testing:

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

## Monitoring & Health

### Kafka Monitoring

Access the Kafka UI at http://localhost:8090 for:

- **Topic Management**: View all Kafka topics and their partitions
- **Message Browsing**: Browse messages in real-time (no password required)
- **Consumer Groups**: Monitor consumer lag and offsets
- **Broker Status**: Check Kafka cluster health
- **Producer/Consumer Metrics**: Performance monitoring

**Note**: Uses Kafdrop - a simple, password-free Kafka monitoring tool

### Email Notifications

**📧 Real-time email notifications** configured for:
- **Order Confirmation** - Immediate after order creation
- **Order Preparing** - When kitchen starts preparation (1 min)
- **Order Ready** - When food is ready for pickup (3 min)
- **Order Completed** - When order is fulfilled
- **Order Cancelled** - If order is cancelled

**Email Account**: `shyakasteven2023@gmail.com`  
**SMTP**: Gmail with TLS/STARTTLS  
**Templates**: Professional restaurant branding

### Health Endpoints

All services expose Spring Boot Actuator endpoints:

- `/actuator/health` - Service health status
- `/actuator/info` - Service information
- `/actuator/metrics` - Performance metrics

### Service Registry

Monitor registered services via Eureka:
- **Dashboard**: http://localhost:8761
- **Registered Services**: Should show all 4 microservices

## Troubleshooting

### Common Issues

**Services won't start:**
```bash
# Check if ports are available
netstat -an | grep :808

# Check Docker logs
docker-compose -f docker-compose.env.yml logs [service-name]
```

**Database connection errors:**
```bash
# Verify PostgreSQL is running
docker-compose -f docker-compose.env.yml logs postgres

# Check database credentials in .env
cat .env | grep POSTGRES
```

**Service discovery issues:**
```bash
# Verify Eureka is accessible
curl http://localhost:8761/eureka/apps

# Check service registration
curl http://localhost:8761/eureka/instances
```

### Performance Optimization

- **Database**: Use connection pooling (configured in .env)
- **Caching**: Redis automatically caches frequently accessed data
- **Circuit Breaker**: Prevents cascading failures
- **Load Balancing**: API Gateway distributes requests

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
