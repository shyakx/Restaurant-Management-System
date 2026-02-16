# Kafka Event-Driven Communication Verification Guide

## 🎯 Purpose
This guide helps you verify that services communicate via Kafka events, NOT direct API calls.

## 🔍 How to Check Kafka is Working

### 1. **Check Code Evidence**

**Order Service (Publisher):**
```kotlin
// OrderService.kt
companion object {
    const val ORDER_TOPIC = "order-events"
}

// When order is created:
kafkaTemplate.send(ORDER_TOPIC, orderEvent.orderId.toString(), orderEvent)
```

**Kitchen Service (Consumer):**
```kotlin
// KitchenService.kt
@KafkaListener(topics = ["order-events"], groupId = "kitchen-service-group")
fun handleOrderEvent(orderEvent: OrderEvent) {
    println("=== KAFKA EVENT RECEIVED ===")
    println("Event Type: ${orderEvent.eventType}")
    println("Order ID: ${orderEvent.orderId}")
}
```

**Notification Service (Consumer):**
```kotlin
// NotificationService.kt
@KafkaListener(topics = ["order-events"], groupId = "notification-service-group")
fun handleOrderEvent(orderEvent: OrderEvent) {
    when (orderEvent.eventType) {
        EventType.ORDER_PLACED -> sendOrderConfirmation(orderEvent)
    }
}
```

### 2. **Live Test to Prove Kafka Communication**

#### Step 1: Start All Services
```bash
# Start infrastructure
docker-compose up -d postgres zookeeper kafka

# Start services (in separate terminals)
./gradlew :eureka-server:bootRun
./gradlew :order-service:bootRun
./gradlew :kitchen-service:bootRun
./gradlew :notification-service:bootRun
```

#### Step 2: Create an Order
```bash
curl -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Test User",
    "customerEmail": "test@example.com",
    "customerPhone": "+250788123456",
    "items": [
      {"menuItemId": 1, "quantity": 2}
    ]
  }'
```

#### Step 3: Check Console Logs

**Kitchen Service Console should show:**
```
=== KAFKA EVENT RECEIVED ===
Event Type: ORDER_PLACED
Order ID: 1
Customer: Test User
Items count: 1
```

**Notification Service Console should show:**
```
=== NOTIFICATION ===
To: test@example.com
Event: ORDER_PLACED
Order ID: 1
Message: [email content]
```

#### Step 4: Verify No Direct API Calls

**Check Network Traffic:**
- Kitchen service NEVER calls Order service API directly
- Notification service NEVER calls Order service API directly
- All communication happens via Kafka topic "order-events"

### 3. **Proof Points**

#### ✅ Kafka Topic Evidence
- **Topic Name**: `order-events`
- **Publisher**: Order Service
- **Consumers**: Kitchen Service, Notification Service

#### ✅ Event Flow Evidence
1. **Order Created** → Order Service publishes `ORDER_PLACED` event
2. **Kitchen Receives** → Kitchen Service consumes event, creates kitchen order
3. **Notification Sent** → Notification Service consumes event, sends email
4. **Status Updates** → Kitchen Service publishes `ORDER_READY` event

#### ✅ No Direct API Evidence
- No `RestTemplate` or `WebClient` calls between services
- No service-to-service HTTP communication
- Only Kafka producers/consumers

### 4. **Advanced Verification**

#### Check Kafka Topics (if tools available)
```bash
# List topics
docker exec restaurant-kafka kafka-topics.sh --bootstrap-server localhost:9092 --list

# Watch messages on topic
docker exec restaurant-kafka kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic order-events --from-beginning
```

#### Network Traffic Analysis
```bash
# Monitor network connections
netstat -an | grep :8082  # Order service
netstat -an | grep :8083  # Kitchen service
netstat -an | grep :8084  # Notification service
```

### 5. **Test Scenarios**

#### Scenario 1: Order Creation
- **Expected**: Order service publishes event, Kitchen & Notification consume
- **Proof**: Console logs show Kafka event processing

#### Scenario 2: Kitchen Status Update
- **Expected**: Kitchen service publishes event, Notification consumes
- **Proof**: Email sent when kitchen marks order as ready

#### Scenario 3: Service Isolation
- **Test**: Stop Kitchen service, create order
- **Expected**: Order still created, Notification still gets event
- **Proof**: Event-driven architecture survives service failures

## 🎉 Conclusion

**Your system IS using Kafka for event-driven communication because:**

1. ✅ **Code Evidence**: Clear Kafka producers/consumers
2. ✅ **Topic Evidence**: `order-events` topic handles all communication
3. ✅ **No Direct APIs**: No HTTP calls between services
4. ✅ **Event Flow**: Complete event-driven lifecycle
5. ✅ **Service Decoupling**: Services work independently

**This is exactly what microservices should do!** 🚀
