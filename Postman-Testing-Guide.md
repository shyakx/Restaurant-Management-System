# Postman Testing Guide - Restaurant Management System

## 🧪 Kafka Integration Testing

This guide will help you test of complete event-driven flow from Order → Kitchen → Notification with email delivery to `shyakasteven2023@gmail.com`.

## 📋 Prerequisites

### ✅ System Status Check
Ensure all services are running:
- **Eureka Server**: http://localhost:8761
- **API Gateway**: http://localhost:8081
- **Order Service**: http://localhost:8082
- **Kitchen Service**: http://localhost:8083
- **Notification Service**: http://localhost:8084
- **Kafka**: localhost:9092
- **PostgreSQL**: localhost:5432

### 📧 Email Setup
- **Test Email**: shyakasteven2023@gmail.com
- **Check**: Inbox and Spam folder
- **Expected**: 4 emails during complete test

---

## 🎯 Test Scenario Overview

### 📋 Test Flow:
1. **Create Order** → ORDER_PLACED event → Confirmation email
2. **Start Preparation** → ORDER_PREPARING event → Preparation email
3. **Mark Ready** → ORDER_READY event → Ready email
4. **Complete Order** → ORDER_COMPLETED event → Completion email

---

## 📦 Test Case 1: Create Order

### 🌐 Request Details
- **Method**: `POST`
- **URL**: `http://localhost:8081/api/orders`
- **Headers**: 
  - `Content-Type: application/json`

### 📝 Request Body
```json
{
    "customerName": "Test Customer",
    "customerEmail": "shyakasteven2023@gmail.com",
    "customerPhone": "+1234567890",
    "items": [
        {
            "menuItemId": 1,
            "menuItemName": "Test Burger",
            "quantity": 2,
            "unitPrice": 12.99
        }
    ]
}
```

### ✅ Expected Response (HTTP 201)
```json
{
    "id": 1,
    "customerName": "Test Customer",
    "customerEmail": "shyakasteven2023@gmail.com",
    "customerPhone": "+1234567890",
    "status": "PENDING",
    "totalAmount": 25.98,
    "items": [
        {
            "menuItemId": 1,
            "menuItemName": "Test Burger",
            "quantity": 2,
            "unitPrice": 12.99,
            "totalPrice": 25.98
        }
    ],
    "createdAt": "2026-02-10T21:15:00"
}
```

### 📧 Expected Email
- **Subject**: "Order Confirmation - #1"
- **To**: shyakasteven2023@gmail.com
- **Content**: Order details and confirmation message

---

## 🍳 Test Case 2: Start Preparation

### 🌐 Request Details
- **Method**: `PUT`
- **URL**: `http://localhost:8081/api/kitchen/orders/1/start-preparation`
- **Headers**: 
  - `Content-Type: application/json`

### ✅ Expected Response (HTTP 200)
```json
{
    "id": 1,
    "orderId": 1,
    "customerName": "Test Customer",
    "customerEmail": "shyakasteven2023@gmail.com",
    "status": "IN_PREPARATION",
    "totalAmount": 25.98,
    "startedPreparationAt": "2026-02-10T21:16:00",
    "estimatedCompletionTime": "2026-02-10T21:41:00"
}
```

### 📧 Expected Email
- **Subject**: "Order in Preparation - #1"
- **To**: shyakasteven2023@gmail.com
- **Content**: Order preparation notification

---

## 🍳 Test Case 3: Mark as Ready

### 🌐 Request Details
- **Method**: `PUT`
- **URL**: `http://localhost:8081/api/kitchen/orders/1/mark-ready`
- **Headers**: 
  - `Content-Type: application/json`

### ✅ Expected Response (HTTP 200)
```json
{
    "id": 1,
    "orderId": 1,
    "customerName": "Test Customer",
    "customerEmail": "shyakasteven2023@gmail.com",
    "status": "READY",
    "totalAmount": 25.98,
    "completedAt": "2026-02-10T21:17:00"
}
```

### 📧 Expected Email
- **Subject**: "Order Ready for Pickup - #1"
- **To**: shyakasteven2023@gmail.com
- **Content**: Pickup notification

---

## 🍳 Test Case 4: Complete Order

### 🌐 Request Details
- **Method**: `PUT`
- **URL**: `http://localhost:8081/api/kitchen/orders/1/mark-completed`
- **Headers**: 
  - `Content-Type: application/json`

### ✅ Expected Response (HTTP 200)
```json
{
    "id": 1,
    "orderId": 1,
    "customerName": "Test Customer",
    "customerEmail": "shyakasteven2023@gmail.com",
    "status": "COMPLETED",
    "totalAmount": 25.98
}
```

### 📧 Expected Email
- **Subject**: "Order Completed - #1"
- **To**: shyakasteven2023@gmail.com
- **Content**: Order completion notification

---

## 🔍 Verification Steps

### 📊 Check Service Logs
```bash
# Order Service - Check Kafka publish
docker logs order-service | grep "ORDER_PLACED"

# Kitchen Service - Check event consumption
docker logs kitchen-service | grep "Kitchen order received"

# Notification Service - Check email sends
docker logs notification-service | grep "Email sent"
```

### 🌐 Verify Kafka Events
```bash
# Monitor real-time events
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic order-events --from-beginning \
  --property print.key=true --property print.value=true
```

### 📧 Email Verification Checklist
- [ ] **Email 1**: "Order Confirmation - #1" received
- [ ] **Email 2**: "Order in Preparation - #1" received
- [ ] **Email 3**: "Order Ready for Pickup - #1" received
- [ ] **Email 4**: "Order Completed - #1" received
- [ ] **Check spam folder** if emails not in inbox
- [ ] **Verify order details** in email content

---

## 🎯 Success Criteria

### ✅ HTTP Response Validation
- [ ] **Order Creation**: HTTP 201 with order ID
- [ ] **Status Updates**: HTTP 200 with updated status
- [ ] **Response Times**: < 500ms for all requests

### ✅ Event Flow Validation
- [ ] **Kafka Events**: All 4 events published and consumed
- [ ] **Service Logs**: Event processing confirmed
- [ ] **Database Updates**: Order status changes persisted

### ✅ Email Validation
- [ ] **All 4 emails** received within 10 seconds
- [ ] **Correct subjects** and order details
- [ ] **Proper formatting** and content

---

## 🚨 Troubleshooting

### Common Issues

#### 📧 Emails Not Received
1. **Check spam folder** in Gmail
2. **Verify email configuration** in notification-service
3. **Check notification logs** for send errors

#### 🔄 Events Not Processing
1. **Check Kafka connectivity**: `docker logs restaurant-kafka`
2. **Verify consumer groups**: `kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list`
3. **Check service registration**: http://localhost:8761

#### 🌐 API Gateway Issues
1. **Check routing configuration** in application.yml
2. **Verify service registration** in Eureka
3. **Check gateway logs** for routing errors

### Debug Commands
```bash
# Check all service health
curl http://localhost:8761/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health

# Check Kafka topics
kafka-topics.sh --bootstrap-server localhost:9092 --list

# Check consumer lag
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group notification-service-group --describe
```

---

## 📈 Advanced Testing

### 🔄 Concurrent Orders
Create multiple orders simultaneously to test concurrent processing:

```json
{
    "customerName": "Customer 2",
    "customerEmail": "shyakasteven2023@gmail.com",
    "customerPhone": "+1234567891",
    "items": [
        {
            "menuItemId": 2,
            "menuItemName": "Test Pizza",
            "quantity": 1,
            "unitPrice": 15.99
        }
    ]
}
```

### 🚫 Order Cancellation
Test order cancellation flow:

```bash
# Cancel order
PUT http://localhost:8081/api/orders/1/cancel

# Expected email: "Order Cancelled - #1"
```

### 📊 Performance Testing
Monitor response times and system load during testing:
- **Order Creation**: < 200ms target
- **Status Updates**: < 100ms target
- **Email Delivery**: < 2 seconds target

---

## 🎉 Test Completion

### ✅ Final Validation
After completing all test cases:

1. **Verify all 4 emails** received in shyakasteven2023@gmail.com
2. **Check Kafka event flow** in logs
3. **Confirm order status progression** in database
4. **Validate system performance** metrics

### 📊 Test Results Summary
- **Total Orders Created**: ___
- **Emails Received**: ___
- **Event Processing Time**: ___ ms
- **System Response Time**: ___ ms
- **Issues Found**: ___

---

## 🚀 Ready to Test!

**Your Restaurant Management System is ready for comprehensive Kafka testing!**

1. **Open Postman**
2. **Import the requests** from this guide
3. **Start with Test Case 1** - Create Order
4. **Monitor your email** for notifications
5. **Verify system logs** for event processing

**Good luck with your testing!** 🎯
