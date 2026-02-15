# Restaurant Management System - API Testing Guide

## Overview
This guide provides comprehensive Postman testing instructions for all Restaurant Management System microservices.

## Service Ports
- **Eureka Server**: 8761
- **Order Service**: 8082
- **Kitchen Service**: 8083
- **Notification Service**: 8084
- **API Gateway**: 8080

## Prerequisites
1. Start infrastructure services: `docker-compose up -d postgres zookeeper kafka`
2. Manually start all Java services in IntelliJ:
   - `EurekaServerApplication` (port 8761)
   - `OrderServiceApplication` (port 8082) 
   - `KitchenServiceApplication` (port 8083)
   - `NotificationServiceApplication` (port 8084)
3. Ensure PostgreSQL and Kafka are running

## Postman Setup

Since there's no JSON collection file, you'll need to create the requests manually in Postman. Use the detailed endpoint specifications below.

---

## Order Service (Port 8082)

### 1. Create Order
- **Method**: POST
- **URL**: `http://localhost:8082/api/orders`
- **Headers**: `Content-Type: application/json`
- **Body**:
```json
{
  "customerName": "Jean Mugabo",
  "customerEmail": "shyastyve@gmail.com",
  "customerPhone": "+250788123456",
  "items": [
    {
      "menuItemId": 1,
      "quantity": 2
    },
    {
      "menuItemId": 2,
      "quantity": 1
    }
  ]
}
```

### 2. Get All Orders
- **Method**: GET
- **URL**: `http://localhost:8082/api/orders`

### 3. Get Order by ID
- **Method**: GET
- **URL**: `http://localhost:8082/api/orders/1`

### 4. Get Orders by Status
- **Method**: GET
- **URL**: `http://localhost:8082/api/orders/status/PENDING`
- **Available Statuses**: PENDING, CONFIRMED, PREPARING, READY, COMPLETED, CANCELLED

### 5. Update Order Status
- **Method**: PUT
- **URL**: `http://localhost:8082/api/orders/1/status`
- **Headers**: `Content-Type: application/json`
- **Body**:
```json
{
  "status": "CONFIRMED"
}
```

### 6. Get Orders by Customer Email
- **Method**: GET
- **URL**: `http://localhost:8082/api/orders/customer/shyastyve@gmail.com`

### 7. Get Order Statistics
- **Method**: GET
- **URL**: `http://localhost:8082/api/orders/statistics`

---

## Kitchen Service (Port 8083)

### 1. Get All Kitchen Orders
- **Method**: GET
- **URL**: `http://localhost:8083/api/kitchen/orders`

### 2. Get Kitchen Order by ID
- **Method**: GET
- **URL**: `http://localhost:8083/api/kitchen/orders/1`

### 3. Get Kitchen Orders by Status
- **Method**: GET
- **URL**: `http://localhost:8083/api/kitchen/orders/status/RECEIVED`
- **Available Statuses**: RECEIVED, IN_PREPARATION, READY, COMPLETED

### 4. Start Preparation
- **Method**: PUT
- **URL**: `http://localhost:8083/api/kitchen/orders/1/start-preparation`

### 5. Mark as Ready
- **Method**: PUT
- **URL**: `http://localhost:8083/api/kitchen/orders/1/ready`

### 6. Mark as Completed
- **Method**: PUT
- **URL**: `http://localhost:8083/api/kitchen/orders/1/complete`

### 7. Get Kitchen Dashboard Stats
- **Method**: GET
- **URL**: `http://localhost:8083/api/kitchen/dashboard/stats`

---

## Notification Service (Port 8084)

### 1. Send Test Email
- **Method**: POST
- **URL**: `http://localhost:8084/api/notifications/test-email`
- **Headers**: `Content-Type: application/json`
- **Body**:
```json
{
  "to": "shyastyve@gmail.com",
  "subject": "Ibihebye bya Restaurant System",
  "message": "Murakaze neza! iyi ni imeri y'ikigeragezo guhera ku sisitemu yo gucanya ibiryo."
}
```

### 2. Health Check
- **Method**: GET
- **URL**: `http://localhost:8084/api/notifications/health`

---

## Service Health Checks

### Order Service Health
- **Method**: GET
- **URL**: `http://localhost:8082/actuator/health`

### Kitchen Service Health
- **Method**: GET
- **URL**: `http://localhost:8083/actuator/health`

### Notification Service Health
- **Method**: GET
- **URL**: `http://localhost:8084/actuator/health`

### Eureka Server
- **Method**: GET
- **URL**: `http://localhost:8761`

---

## Testing Workflow

### Complete Order Flow Test
1. **Create Order** (Order Service)
   - Use the Create Order endpoint
   - Note the returned order ID

2. **Check Order Status** (Order Service)
   - Use Get Order by ID with the returned ID

3. **View in Kitchen** (Kitchen Service)
   - Check Get All Kitchen Orders
   - The new order should appear with status RECEIVED

4. **Start Preparation** (Kitchen Service)
   - Use Start Preparation endpoint

5. **Mark as Ready** (Kitchen Service)
   - Use Mark as Ready endpoint

6. **Complete Order** (Kitchen Service)
   - Use Mark as Completed endpoint

7. **Verify Final Status** (Order Service)
   - Check order status again

### Email Notification Test
1. Configure Gmail settings in your `.env` file
2. **Important**: Use an App Password for Gmail (not your regular password)
   - Go to Google Account settings → Security → 2-Step Verification → App passwords
   - Generate a new app password for "Mail"
   - Use this app password in `SPRING_MAIL_PASSWORD`
3. Use the Send Test Email endpoint to send to `shyastyve@gmail.com`
4. Check the Gmail inbox for the test email
5. **Real Order Flow Test**: Create an order with `shyastyve@gmail.com` as customer email
   - The notification service should automatically send an email when order events are published

---

## Environment Variables Setup

Create a `.env` file in your project root with:

```env
# Database Configuration
POSTGRES_DB=restaurant_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_password

# Email Configuration (for Notification Service - Gmail)
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=shyakasteven2023@gmail.com
SPRING_MAIL_PASSWORD=your_app_password_here
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
```

---

## Troubleshooting

### Common Issues
1. **Connection Refused**: Ensure all services are running
2. **Database Errors**: Check PostgreSQL is running and credentials are correct
3. **Kafka Errors**: Verify Kafka is running on port 9092
4. **Email Failures**: Check Gmail configuration and app password

### Service Dependencies
- Order Service requires PostgreSQL and Kafka
- Kitchen Service requires PostgreSQL and Kafka
- Notification Service requires Kafka and email configuration
- All services require Eureka Server for service discovery

### Health Check Verification
Always run health checks first to ensure services are responsive before testing APIs.

---

## Advanced Testing

### Load Testing
Use Postman's Collection Runner to:
- Test multiple order creations
- Simulate concurrent kitchen operations
- Test notification throughput

### Integration Testing
Test the complete microservice communication:
1. Order creation triggers Kafka events
2. Kitchen service receives events
3. Notification service sends emails
4. Service discovery via Eureka

---

## API Response Examples

### Order Creation Response
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

---

For additional support, check the service logs or refer to the individual service documentation.
