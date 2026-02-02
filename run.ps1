# Restaurant Management System - Simple Startup

Write-Host "Starting Restaurant System..." -ForegroundColor Green

# Start infrastructure
Write-Host "Starting infrastructure..." -ForegroundColor Blue
docker-compose -f docker-compose.yml up -d postgres redis zookeeper kafka

Write-Host "Infrastructure ready!" -ForegroundColor Green
Write-Host ""
Write-Host "Services:" -ForegroundColor Cyan
Write-Host "   PostgreSQL: localhost:5432"
Write-Host "   Redis: localhost:6379" 
Write-Host "   Kafka: localhost:9092"
Write-Host ""
Write-Host "Run Java services in IntelliJ:" -ForegroundColor Yellow
Write-Host "   - EurekaServerApplication (port 8761)"
Write-Host "   - MenuServiceApplication (port 8081)"
