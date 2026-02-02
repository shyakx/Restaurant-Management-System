# Restaurant Management System - Stop

Write-Host "🛑 Stopping Restaurant System..." -ForegroundColor Red
docker-compose -f docker-compose.yml down
Write-Host "✅ All services stopped!" -ForegroundColor Green
