# Menu Service - Banking Interview Demo

A production-ready Spring Boot microservice demonstrating enterprise-grade features for banking backend interviews.

## 🏗️ Architecture

- **Spring Boot 3.1.5** - Modern Java framework
- **PostgreSQL 15** - Primary database with ACID compliance
- **Redis 7** - High-performance caching layer
- **Apache Kafka** - Event-driven architecture
- **JWT Authentication** - Secure stateless authentication
- **Docker Compose** - Containerized infrastructure

## 🔐 Security Features

- **JWT with HS512** - 512-bit signing keys (RFC 7518 compliant)
- **Environment Variables** - No hardcoded secrets
- **Rate Limiting** - API protection (100 req/min, 20 req/min for writes)
- **Audit Trails** - Comprehensive user tracking
- **Input Validation** - Bean validation with custom constraints

## 📊 Performance & Monitoring

- **Redis Caching** - 10-minute TTL with key prefixing
- **Business Metrics** - Custom operation tracking
- **Prometheus Integration** - Full observability
- **Health Checks** - Production-ready monitoring
- **Connection Pooling** - HikariCP with optimized settings

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Gradle 7+

### Setup

1. **Clone and configure:**
```bash
cp .env.example .env
# Edit .env with your values
```

2. **Start infrastructure:**
```bash
docker-compose up -d
```

3. **Run application:**
```bash
./gradlew bootRun --args="--spring.profiles.active=local-dev"
```

### Test the Application

1. **Authentication:**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

2. **Menu Operations:**
```bash
# Get JWT token from login response, then:
curl -X GET http://localhost:8081/api/menu/items/all \
  -H "Authorization: Bearer <your-token>"
```

3. **Monitoring:**
```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8081/actuator/metrics
curl http://localhost:8081/actuator/prometheus
```

## 🏦 Banking Interview Talking Points

### Security Implementation
- "I implemented enterprise-grade JWT security with 512-bit HS512 keys meeting RFC 7518 standards"
- "Environment variable management ensures no secrets are hardcoded in the codebase"
- "Rate limiting prevents API abuse with different limits for read vs write operations"

### Performance Optimization
- "Redis caching with proper TTL and key prefixing reduces database load"
- "HikariCP connection pooling optimizes database performance"
- "Custom business metrics provide operational insights"

### Production Readiness
- "Comprehensive audit trails track all data modifications with user attribution"
- "Health checks and Prometheus metrics enable production monitoring"
- "Docker containerization ensures consistent deployment environments"

### Enterprise Patterns
- "Event-driven architecture with Kafka for loose coupling"
- "Proper exception handling with correlation IDs for debugging"
- "Configuration management supports multiple deployment environments"

## 📁 Project Structure

```
src/main/kotlin/com/restaurant/menu/
├── config/          # Configuration classes
├── controller/      # REST endpoints
├── entity/          # JPA entities with audit
├── exception/       # Global exception handling
├── filter/          # Rate limiting filter
├── metrics/         # Business metrics
├── repository/      # Data access layer
├── security/        # JWT configuration
└── service/         # Business logic
```

## 🔧 Configuration

### Environment Variables
- `DB_PASSWORD` - PostgreSQL database password
- `JWT_SECRET` - 512+ bit JWT signing key
- `SERVER_PORT` - Application port (default: 8081)

### Profiles
- `local-dev` - Development environment
- `test` - Testing environment
- `prod` - Production environment (create as needed)

## 📈 Monitoring Endpoints

- `/actuator/health` - Application health status
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics
- `/actuator/info` - Application information

## 🎯 Key Features Demonstrated

1. **Security Expertise** - JWT, rate limiting, audit trails
2. **Performance Optimization** - Caching, connection pooling, metrics
3. **Production Readiness** - Monitoring, health checks, error handling
4. **Enterprise Patterns** - Event-driven, configuration management
5. **Code Quality** - Clean architecture, proper separation of concerns

---

**This project demonstrates the skills and knowledge expected of a banking backend engineer.**
