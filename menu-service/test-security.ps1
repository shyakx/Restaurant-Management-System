# Enterprise Security Test Suite (Windows PowerShell)
# Purpose: Validate JWT authentication and RBAC implementation
# Author: Enterprise Development Team
# Version: 1.0

# Configuration
$BaseUrl = "http://localhost:8081"
$AdminUsername = "admin"
$AdminPassword = "admin123"
$UserUsername = "user"
$UserPassword = "user123"

# Test counters
$TestsTotal = 0
$TestsPassed = 0
$TestsFailed = 0

# Logging functions
function Log-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Blue
}

function Log-Success {
    param([string]$Message)
    Write-Host "[PASS] $Message" -ForegroundColor Green
    $script:TestsPassed++
}

function Log-Error {
    param([string]$Message)
    Write-Host "[FAIL] $Message" -ForegroundColor Red
    $script:TestsFailed++
}

function Log-Warning {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

# HTTP client function
function Http-Request {
    param(
        [string]$Method,
        [string]$Endpoint,
        [string]$Token = "",
        [string]$Data = ""
    )
    
    $headers = @{
        "Content-Type" = "application/json"
    }
    
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }
    
    try {
        $response = $null
        $statusCode = 200
        
        if ($Data) {
            $response = Invoke-RestMethod -Uri "$BaseUrl$Endpoint" -Method $Method -Headers $headers -Body $Data
        } else {
            $response = Invoke-RestMethod -Uri "$BaseUrl$Endpoint" -Method $Method -Headers $headers
        }
        
        # For successful requests, assume 200 (this is a limitation of the simple approach)
        $statusCode = 200
    } catch {
        $statusCode = 0
        $response = $null
        
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode.value__
            try {
                $stream = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($stream)
                $responseBody = $reader.ReadToEnd()
                $reader.Close()
                
                try {
                    $response = $responseBody | ConvertFrom-Json
                } catch {
                    $response = $responseBody
                }
            } catch {
                $response = "Error reading response: $($_.Exception.Message)"
            }
        } else {
            $response = "Connection error: $($_.Exception.Message)"
        }
    }
    
    return @{
        Body = $response
        Status = $statusCode
    }
}

# Test framework
function Run-Test {
    param(
        [string]$TestName,
        [string]$Method,
        [string]$Endpoint,
        [int]$ExpectedStatus,
        [string]$Token = "",
        [string]$Data = ""
    )
    
    $script:TestsTotal++
    Log-Info "Running: $TestName"
    
    $result = Http-Request -Method $Method -Endpoint $Endpoint -Token $Token -Data $Data
    
    if ($result.Status -eq $ExpectedStatus) {
        Log-Success "$TestName - HTTP $($result.Status)"
    } else {
        Log-Error "$TestName - Expected $ExpectedStatus, got $($result.Status)"
        if ($result.Body) {
            Log-Warning "Response: $($result.Body | ConvertTo-Json -Compress)"
        }
    }
}

# Authentication functions
function Authenticate-User {
    param(
        [string]$Username,
        [string]$Password
    )
    
    Log-Info "Authenticating user: $Username"
    
    $authData = @{
        username = $Username
        password = $Password
    } | ConvertTo-Json -Compress
    
    $result = Http-Request -Method "POST" -Endpoint "/api/auth/login" -Data $authData
    
    if ($result.Status -eq 200) {
        $token = $result.Body.token
        if ($token) {
            Log-Success "Authentication successful for $Username"
            return $token
        } else {
            Log-Error "Failed to extract token from response"
            return ""
        }
    } else {
        Log-Error "Authentication failed for $Username - HTTP $($result.Status)"
        return ""
    }
}

# Test data
$MenuItemData = @{
    name = "Test Burger"
    description = "A delicious test burger"
    price = 12.99
    category = "Main Course"
    available = $true
} | ConvertTo-Json -Compress

$UpdatedMenuItemData = @{
    name = "Updated Test Burger"
    description = "An updated delicious test burger"
    price = 14.99
    category = "Main Course"
    available = $true
} | ConvertTo-Json -Compress

# Main test execution
function Main {
    Log-Info "Starting Enterprise Security Test Suite"
    Log-Info "Target: $BaseUrl"
    Write-Host ""
    
    # Health check
    Log-Info "Performing health check..."
    $healthResult = Http-Request -Method "GET" -Endpoint "/actuator/health"
    
    if ($healthResult.Status -eq 200) {
        Log-Success "Service is healthy"
    } else {
        Log-Error "Service health check failed - HTTP $($healthResult.Status)"
        exit 1
    }
    
    Write-Host ""
    
    # Test 1: Public endpoints (no authentication required)
    Log-Info "Testing public endpoints..."
    Run-Test -TestName "GET /api/menu/items (public)" -Method "GET" -Endpoint "/api/menu/items" -ExpectedStatus 200
    Run-Test -TestName "GET /actuator/health (public)" -Method "GET" -Endpoint "/actuator/health" -ExpectedStatus 200
    
    Write-Host ""
    
    # Test 2: Protected endpoints without authentication (should fail)
    Log-Info "Testing protected endpoints without authentication..."
    Run-Test -TestName "POST /api/menu/items (no auth)" -Method "POST" -Endpoint "/api/menu/items" -ExpectedStatus 401 -Data $MenuItemData
    Run-Test -TestName "PUT /api/menu/items/1 (no auth)" -Method "PUT" -Endpoint "/api/menu/items/1" -ExpectedStatus 401
    Run-Test -TestName "DELETE /api/menu/items/1 (no auth)" -Method "DELETE" -Endpoint "/api/menu/items/1" -ExpectedStatus 401
    
    Write-Host ""
    
    # Test 3: Admin authentication
    Log-Info "Testing admin authentication..."
    $adminToken = Authenticate-User -Username $AdminUsername -Password $AdminPassword
    
    if ($adminToken) {
        Write-Host ""
        
        # Test 4: Admin operations (should succeed)
        Log-Info "Testing admin operations..."
        Run-Test -TestName "POST /api/menu/items (admin)" -Method "POST" -Endpoint "/api/menu/items" -ExpectedStatus 200 -Token $adminToken -Data $MenuItemData
        
        # Test 5: Token validation
        Log-Info "Testing token validation..."
        Run-Test -TestName "POST /api/auth/validate (admin)" -Method "POST" -Endpoint "/api/auth/validate" -ExpectedStatus 200 -Token $adminToken
    } else {
        Log-Error "Cannot proceed with admin tests - authentication failed"
    }
    
    Write-Host ""
    
    # Test 6: Regular user authentication
    Log-Info "Testing regular user authentication..."
    $userToken = Authenticate-User -Username $UserUsername -Password $UserPassword
    
    if ($userToken) {
        Write-Host ""
        
        # Test 7: Regular user operations (should fail for admin endpoints)
        Log-Info "Testing regular user access restrictions..."
        Run-Test -TestName "POST /api/menu/items (user)" -Method "POST" -Endpoint "/api/menu/items" -ExpectedStatus 403 -Token $userToken -Data $MenuItemData
        Run-Test -TestName "PUT /api/menu/items/1 (user)" -Method "PUT" -Endpoint "/api/menu/items/1" -ExpectedStatus 403 -Token $userToken -Data $UpdatedMenuItemData
        Run-Test -TestName "DELETE /api/menu/items/1 (user)" -Method "DELETE" -Endpoint "/api/menu/items/1" -ExpectedStatus 403 -Token $userToken
        
        # Test 8: Token validation for regular user
        Log-Info "Testing regular user token validation..."
        Run-Test -TestName "POST /api/auth/validate (user)" -Method "POST" -Endpoint "/api/auth/validate" -ExpectedStatus 200 -Token $userToken
    } else {
        Log-Error "Cannot proceed with user tests - authentication failed"
    }
    
    Write-Host ""
    
    # Test Results Summary
    Log-Info "Test Results Summary"
    Log-Info "===================="
    Log-Info "Total Tests: $TestsTotal"
    Log-Success "Passed: $TestsPassed"
    
    if ($TestsFailed -gt 0) {
        Log-Error "Failed: $TestsFailed"
    }
    
    Write-Host ""
    
    # Exit with appropriate code
    if ($TestsFailed -eq 0) {
        Log-Success "All tests passed! Security implementation is working correctly."
        exit 0
    } else {
        Log-Error "Some tests failed. Please review the security implementation."
        exit 1
    }
}

# Run main function
Main
