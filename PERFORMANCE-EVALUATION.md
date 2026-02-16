# Performance Evaluation Guide

## Technology Stack Overview

### Core Technologies
- **Spring Boot 3.2** - Application framework
- **Kotlin** - Programming language
- **PostgreSQL** - Primary database
- **Redis** - Caching layer
- **Apache Kafka** - Event streaming
- **Docker** - Containerization
- **Eureka** - Service discovery

### Architecture Pattern
- **Microservices** - Order Service (8082), Kitchen Service (8083), Notification Service (8084)
- **Event-Driven** - Kafka-based communication
- **Caching Strategy** - Redis for performance optimization

---

## Performance Evaluation Methods

### 1. Database Performance (PostgreSQL)

#### Monitoring Tools
```bash
# PostgreSQL connection monitoring
docker exec -it restaurant-postgres psql -U postgres -d restaurant_db -c "SELECT * FROM pg_stat_activity;"

# Query performance analysis
docker exec -it restaurant-postgres psql -U postgres -d restaurant_db -c "SELECT query, calls, total_time, mean_time FROM pg_stat_statements ORDER BY total_time DESC LIMIT 10;"

# Index usage analysis
docker exec -it restaurant-postgres psql -U postgres -d restaurant_db -c "SELECT schemaname, tablename, attname, n_distinct, correlation FROM pg_stats WHERE tablename LIKE '%order%';"
```

#### Key Metrics to Track
- **Query Response Time** - Average execution time per query
- **Connection Pool Usage** - Active vs idle connections
- **Index Efficiency** - Query plans and index usage
- **Database Size Growth** - Table and index sizes over time

#### Performance Tests
```bash
# Load test with Apache Bench
ab -n 1000 -c 10 http://localhost:8082/api/orders/

# Database stress test
docker exec -it restaurant-postgres pgbench -U postgres -d restaurant_db -c 10 -j 2 -t 1000
```

### 2. Cache Performance (Redis)

#### Monitoring Commands
```bash
# Redis connection and activity
docker exec -it restaurant-redis redis-cli info stats

# Memory usage analysis
docker exec -it restaurant-redis redis-cli info memory

# Cache hit/miss ratios
docker exec -it restaurant-redis redis-cli info keyspace

# Real-time monitoring
docker exec -it restaurant-redis redis-cli monitor

# Cache key analysis
docker exec -it restaurant-redis redis-cli keys "*"
docker exec -it restaurant-redis redis-cli ttl "kitchen-stats::dashboard"
```

#### Key Performance Indicators
- **Cache Hit Ratio** - `hits / (hits + misses)` - Target: >80%
- **Memory Usage** - `used_memory` vs `maxmemory`
- **Response Time** - Cache operations should be <1ms
- **Eviction Rate** - Keys evicted due to memory limits

#### Cache Performance Tests
```bash
# Test cache performance
time curl http://localhost:8083/api/kitchen/dashboard/stats  # First call (cache miss)
time curl http://localhost:8083/api/kitchen/dashboard/stats  # Second call (cache hit)

# Benchmark Redis operations
docker exec -it restaurant-redis redis-benchmark -t set,get -n 100000 -c 50
```

### 3. Message Queue Performance (Kafka)

#### Monitoring Commands
```bash
# Kafka topic and consumer metrics
docker exec -it restaurant-kafka kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic order-events

# Consumer lag monitoring
docker exec -it restaurant-kafka kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group kitchen-service-group

# Producer performance test
docker exec -it restaurant-kafka kafka-producer-perf-test.sh --topic order-events --num-records 10000 --record-size 1024 --throughput 1000 --producer-props bootstrap.servers=localhost:9092
```

#### Key Metrics
- **Throughput** - Messages per second
- **Latency** - Message production to consumption time
- **Consumer Lag** - Messages pending consumption
- **Broker Load** - CPU, memory, and network usage

#### Performance Tests
```bash
# Load test Kafka producers
docker exec -it restaurant-kafka kafka-producer-perf-test.sh --topic order-events --num-records 50000 --record-size 2048 --throughput 5000 --producer-props bootstrap.servers=localhost:9092

# Test consumer performance
docker exec -it restaurant-kafka kafka-consumer-perf-test.sh --topic order-events --bootstrap-server localhost:9092 --messages 10000 --threads 4
```

### 4. Application Performance (Spring Boot)

#### Monitoring Endpoints
```bash
# Actuator health checks
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health

# Application metrics
curl http://localhost:8082/actuator/metrics
curl http://localhost:8083/actuator/metrics/http.server.requests

# JVM memory usage
curl http://localhost:8082/actuator/metrics/jvm.memory.used
curl http://localhost:8083/actuator/metrics/jvm.memory.used
```

#### Load Testing Tools
```bash
# Apache Bench - Simple load testing
ab -n 5000 -c 20 http://localhost:8082/api/orders/
ab -n 5000 -c 20 http://localhost:8083/api/kitchen/orders/
ab -n 5000 -c 20 http://localhost:8083/api/kitchen/dashboard/stats/

# JMeter - Advanced load testing
# Create JMeter test plan with:
# - Thread Group: 100 users, ramp-up 10s
# - HTTP Requests for all API endpoints
# - Response time assertions
# - Throughput monitoring
```

#### Key Application Metrics
- **Response Time** - API endpoint latency
- **Throughput** - Requests per second
- **Error Rate** - Failed requests percentage
- **Memory Usage** - Heap and non-heap memory
- **CPU Usage** - Application processor utilization

### 5. Container Performance (Docker)

#### Monitoring Commands
```bash
# Container resource usage
docker stats

# Container health status
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# Container logs analysis
docker logs restaurant-order-service --tail 100
docker logs restaurant-kitchen-service --tail 100
docker logs restaurant-redis --tail 100
```

#### Key Metrics
- **CPU Usage** - Container processor utilization
- **Memory Usage** - Container memory consumption
- **Network I/O** - Container network traffic
- **Disk I/O** - Container disk usage

---

## Performance Benchmarks & Targets

### Response Time Targets
| Endpoint | Target (ms) | Acceptable (ms) | Critical (ms) |
|----------|-------------|-----------------|----------------|
| Order CRUD operations | <200 | <500 | >1000 |
| Kitchen dashboard stats | <100 (cached) | <300 | >500 |
| Kitchen orders list | <150 (cached) | <400 | >800 |
| Notification send | <500 | <1000 | >2000 |

### Throughput Targets
| Service | Target (req/s) | Acceptable (req/s) |
|---------|----------------|-------------------|
| Order Service | 500+ | 200+ |
| Kitchen Service | 300+ | 150+ |
| Notification Service | 100+ | 50+ |

### Cache Performance Targets
| Metric | Target | Acceptable |
|--------|--------|------------|
| Hit Ratio | >85% | >70% |
| Memory Usage | <80% of max | <90% of max |
| Response Time | <1ms | <5ms |

### Database Performance Targets
| Metric | Target | Acceptable |
|--------|--------|------------|
| Query Time | <100ms | <500ms |
| Connection Pool | <80% used | <95% used |
| Index Usage | >95% | >80% |

---

## Performance Testing Scenarios

### Scenario 1: Peak Load Simulation
```bash
# Simulate restaurant peak hours (6-9 PM)
# 200 concurrent users placing orders
# 50 kitchen staff checking dashboard
# 100 notifications per minute

# Run for 30 minutes
ab -n 360000 -c 200 http://localhost:8082/api/orders/
ab -n 90000 -c 50 http://localhost:8083/api/kitchen/dashboard/stats/
```

### Scenario 2: Cache Failure Test
```bash
# Stop Redis and test performance degradation
docker stop restaurant-redis

# Test API response times without cache
time curl http://localhost:8083/api/kitchen/dashboard/stats/

# Restart Redis
docker start restaurant-redis
```

### Scenario 3: Database Stress Test
```bash
# Simulate high database load
docker exec -it restaurant-postgres pgbench -U postgres -d restaurant_db -c 20 -j 4 -t 10000

# Monitor database performance during test
docker exec -it restaurant-postgres psql -U postgres -d restaurant_db -c "SELECT * FROM pg_stat_activity;"
```

### Scenario 4: Kafka Message Flood
```bash
# Flood Kafka with messages
for i in {1..10000}; do
  curl -X POST http://localhost:8082/api/orders \
    -H "Content-Type: application/json" \
    -d '{"customerName":"Test'$i'","customerEmail":"test'$i'@example.com","items":[{"menuItemId":1,"quantity":1}]}'
done

# Monitor consumer lag
docker exec -it restaurant-kafka kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group kitchen-service-group
```

---

## Monitoring & Alerting Setup

### Real-time Monitoring Dashboard
```yaml
# docker-compose.monitoring.yml
version: '3.8'
services:
  prometheus:
    image: prom/prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml

  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin

  node-exporter:
    image: prom/node-exporter
    ports:
      - "9100:9100"
```

### Key Alerts to Configure
- **High Response Time** - API endpoints >1s
- **Low Cache Hit Ratio** - <70% for 5 minutes
- **Database Connection Pool** - >90% utilization
- **Kafka Consumer Lag** - >1000 messages
- **Memory Usage** - >85% container memory
- **Error Rate** - >5% for any service

### Log Analysis
```bash
# Aggregate error logs
docker logs restaurant-order-service 2>&1 | grep ERROR | wc -l
docker logs restaurant-kitchen-service 2>&1 | grep ERROR | wc -l

# Performance log analysis
docker logs restaurant-kitchen-service 2>&1 | grep "Completed\|ms" | tail -20
```

---

## Performance Optimization Checklist

### Database Optimization
- [ ] Add indexes for frequently queried columns
- [ ] Optimize slow queries with `EXPLAIN ANALYZE`
- [ ] Configure connection pooling properly
- [ ] Monitor and vacuum PostgreSQL regularly

### Cache Optimization
- [ ] Set appropriate TTL for cache entries
- [ ] Monitor cache hit ratios
- [ ] Implement cache warming strategies
- [ ] Configure Redis memory limits

### Application Optimization
- [ ] Enable Spring Boot caching
- [ ] Optimize JVM heap size
- [ ] Configure thread pools
- [ ] Implement circuit breakers

### Infrastructure Optimization
- [ ] Right-size container resources
- [ ] Configure load balancing
- [ ] Implement health checks
- [ ] Set up log aggregation

---

## Performance Testing Schedule

### Daily Checks
- [ ] API response times
- [ ] Cache hit ratios
- [ ] Database query performance
- [ ] Error rates

### Weekly Tests
- [ ] Load testing scenarios
- [ ] Cache failure testing
- [ ] Database stress tests
- [ ] Kafka throughput tests

### Monthly Reviews
- [ ] Performance trend analysis
- [ ] Capacity planning
- [ ] Optimization opportunities
- [ ] Benchmark updates

---

## Tools Summary

| Category | Tools | Purpose |
|----------|-------|---------|
| Load Testing | Apache Bench, JMeter, k6 | API performance testing |
| Database | pgbench, pg_stat_statements | Query performance |
| Cache | redis-cli, redis-benchmark | Cache performance |
| Messaging | kafka-tools | Message throughput |
| Monitoring | Prometheus, Grafana | Real-time metrics |
| Container | docker stats | Resource usage |
| Application | Spring Actuator | Application metrics |

This comprehensive performance evaluation guide will help you maintain optimal performance across your entire restaurant management system stack.
