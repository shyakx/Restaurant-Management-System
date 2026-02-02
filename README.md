# Simple Restaurant Management System

A clean, simple microservices restaurant management system using Spring Boot, Eureka, Gateway, Redis, Kafka, and Docker.

## Architecture

- **Menu Service**: Manages restaurant menu items (CRUD operations)
- **Order Service**: Handles customer orders
- **Eureka Server**: Service discovery
- **API Gateway**: Single entry point for all requests
- **Redis**: Caching popular menu items
- **Kafka**: Order processing events

## Simple Workflow

1. Menu Service manages food items and prices
2. Customers place orders through Order Service
3. Order Service publishes events to Kafka
4. Redis caches popular menu items for fast access
5. Gateway routes all API requests

## Technologies

- Spring Boot (Kotlin)
- PostgreSQL
- Redis
- Kafka
- Docker & Docker Compose
- Spring Cloud Gateway
- Spring Cloud Eureka

## Quick Start

```bash
docker-compose up -d
```

## Access Points

- API Gateway: http://localhost:8080
- Eureka Dashboard: http://localhost:8761
- Menu Service: http://localhost:8081
- Order Service: http://localhost:8082

## API Examples

### Menu Service
- GET /api/menu/items - Get all menu items
- POST /api/menu/items - Add new menu item
- PUT /api/menu/items/{id} - Update menu item
- DELETE /api/menu/items/{id} - Delete menu item

### Order Service
- GET /api/orders - Get all orders
- POST /api/orders - Create new order
- GET /api/orders/{id} - Get order by ID
