# Restaurant Management System - Startup Guide

## Prerequisites
- Docker Desktop installed and running
- Java 17+
- Gradle 8.5+

## 🚀 Complete Startup Sequence

### Step 0: Start Docker Infrastructure
```bash
# 1. Start Docker Desktop from Applications

# 2. Start infrastructure containers
docker-compose up -d postgres zookeeper kafka

# 3. Verify containers are running
docker ps
```
**Expected:** 3 containers running (postgres, zookeeper, kafka)

### Step 1: Start Eureka Server (Service Registry)
```bash
cd eureka-server
.\gradlew bootRun
```
**Wait for:** `Started EurekaServer on port 8761`
**Access:** http://localhost:8761

### Step 2: Start API Gateway
```bash
cd api-gateway
.\gradlew bootRun
```
**Wait for:** `Started ApiGatewayApplicationKt on port 8080`
**Access:** http://localhost:8080

### Step 3: Start Order Service
```bash
cd order-service
.\gradlew bootRun
```
**Wait for:** `Started OrderServiceApplicationKt on port 8082`
**Access:** http://localhost:8082

### Step 4: Start Kitchen Service
```bash
cd kitchen-service
.\gradlew bootRun
```
**Wait for:** `Started KitchenServiceApplicationKt on port 8083`
**Access:** http://localhost:8083

### Step 5: Start Notification Service
```bash
cd notification-service
.\gradlew bootRun
```
**Wait for:** `Started NotificationServiceApplicationKt on port 8084`
**Access:** http://localhost:8084

## 📊 Service Ports & URLs
| Service | Port | URL | Description |
|---------|------|-----|-------------|
| Eureka | 8761 | http://localhost:8761 | Service Registry |
| API Gateway | 8080 | http://localhost:8080 | Main Entry Point |
| Order Service | 8082 | http://localhost:8082 | Order Management |
| Kitchen Service | 8083 | http://localhost:8083 | Kitchen Operations |
| Notification Service | 8084 | http://localhost:8084 | Email Notifications |

## 🔧 Troubleshooting

### Memory Issues
If services fail with out-of-memory errors:
```bash
# Kill Java processes
taskkill /F /IM java.exe

# Restart with higher memory (already configured in gradle.properties)
```

### Docker Issues
```bash
# Check Docker status
docker ps

# Restart containers
docker-compose restart

# View logs
docker-compose logs [service-name]
```

### Service Registration
Check Eureka dashboard (http://localhost:8761) to verify all services registered.

### API Routes via Gateway
- **Order APIs**: `http://localhost:8080/api/orders/**`
- **Kitchen APIs**: `http://localhost:8080/api/kitchen/**`
- **Notification APIs**: `http://localhost:8080/api/notifications/**`

## 🛠️ Development Notes

### Fixed Issues
- ✅ Removed rate limiting from API Gateway
- ✅ Fixed Kafka deserialization in Notification Service
- ✅ Increased Gradle memory allocation (2GB max)
- ✅ Simplified service dependencies

### Important Files Modified
- `api-gateway/build.gradle.kts` - Removed rate limiting dependencies
- `api-gateway/src/main/resources/application.yml` - Removed rate limiting
- `notification-service/src/main/resources/application.yml` - Fixed OrderEvent class
- All `gradle.properties` - Increased memory to 2GB

## 📝 Logs Location
- **Gradle**: `~/.gradle/daemon/8.5/daemon-*.out.log`
- **Docker**: `docker-compose logs [service-name]`
- **Spring Boot**: Console output in each terminal

## ⚡ Quick Start Commands
```bash
# 1. Infrastructure
docker-compose up -d postgres zookeeper kafka

# 2. Services (in separate terminals)
cd eureka-server && .\gradlew bootRun &
cd api-gateway && .\gradlew bootRun &
cd order-service && .\gradlew bootRun &
cd kitchen-service && .\gradlew bootRun &
cd notification-service && .\gradlew bootRun &
```

**Remember:** Start each service in its own terminal window!
