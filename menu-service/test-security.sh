#!/bin/bash

# Enterprise Security Test Suite
# Purpose: Validate JWT authentication and RBAC implementation
# Author: Enterprise Development Team
# Version: 1.0

set -euo pipefail

# Configuration
readonly BASE_URL="http://localhost:8081"
readonly ADMIN_USERNAME="admin"
readonly ADMIN_PASSWORD="admin123"
readonly USER_USERNAME="user"
readonly USER_PASSWORD="user123"

# Colors for output
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly YELLOW='\033[1;33m'
readonly BLUE='\033[0;34m'
readonly NC='\033[0m' # No Color

# Test counters
TESTS_TOTAL=0
TESTS_PASSED=0
TESTS_FAILED=0

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[PASS]${NC} $1"
    ((TESTS_PASSED++))
}

log_error() {
    echo -e "${RED}[FAIL]${NC} $1"
    ((TESTS_FAILED++))
}

log_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# HTTP client function
http_request() {
    local method=$1
    local endpoint=$2
    local token=${3:-}
    local data=${4:-}
    
    local curl_cmd="curl -s -w '%{http_code}' -X ${method}"
    
    if [[ -n "$token" ]]; then
        curl_cmd+=" -H 'Authorization: Bearer ${token}'"
    fi
    
    curl_cmd+=" -H 'Content-Type: application/json'"
    
    if [[ -n "$data" ]]; then
        curl_cmd+=" -d '${data}'"
    fi
    
    curl_cmd+=" '${BASE_URL}${endpoint}'"
    
    local response
    response=$(eval "$curl_cmd")
    
    local status_code="${response: -3}"
    local body="${response%???}"
    
    echo "$body"
    echo "$status_code"
}

# Test framework
run_test() {
    local test_name="$1"
    local method="$2"
    local endpoint="$3"
    local expected_status="$4"
    local token="$5"
    local data="$6"
    
    ((TESTS_TOTAL++))
    
    log_info "Running: $test_name"
    
    local response
    response=$(http_request "$method" "$endpoint" "$token" "$data")
    
    local body
    local status
    body=$(echo "$response" | head -n -1)
    status=$(echo "$response" | tail -n 1)
    
    if [[ "$status" -eq "$expected_status" ]]; then
        log_success "$test_name - HTTP $status"
    else
        log_error "$test_name - Expected $expected_status, got $status"
        if [[ -n "$body" ]]; then
            log_warning "Response: $body"
        fi
    fi
}

# Authentication functions
authenticate_user() {
    local username="$1"
    local password="$2"
    
    log_info "Authenticating user: $username"
    
    local auth_data="{\"username\":\"${username}\",\"password\":\"${password}\"}"
    local response
    response=$(http_request "POST" "/api/auth/login" "" "$auth_data")
    
    local body
    local status
    body=$(echo "$response" | head -n -1)
    status=$(echo "$response" | tail -n 1)
    
    if [[ "$status" -eq 200 ]]; then
        local token
        token=$(echo "$body" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        
        if [[ -n "$token" ]]; then
            log_success "Authentication successful for $username"
            echo "$token"
        else
            log_error "Failed to extract token from response"
            echo ""
        fi
    else
        log_error "Authentication failed for $username - HTTP $status"
        echo ""
    fi
}

# Test data
MENU_ITEM_DATA='{
    "name": "Test Burger",
    "description": "A delicious test burger",
    "price": 12.99,
    "category": "Main Course",
    "available": true
}'

UPDATED_MENU_ITEM_DATA='{
    "name": "Updated Test Burger",
    "description": "An updated delicious test burger",
    "price": 14.99,
    "category": "Main Course",
    "available": true
}'

# Main test execution
main() {
    log_info "Starting Enterprise Security Test Suite"
    log_info "Target: $BASE_URL"
    echo
    
    # Health check
    log_info "Performing health check..."
    local health_response
    health_response=$(http_request "GET" "/actuator/health")
    local health_status
    health_status=$(echo "$health_response" | tail -n 1)
    
    if [[ "$health_status" -eq 200 ]]; then
        log_success "Service is healthy"
    else
        log_error "Service health check failed - HTTP $health_status"
        exit 1
    fi
    
    echo
    
    # Test 1: Public endpoints (no authentication required)
    log_info "Testing public endpoints..."
    run_test "GET /api/menu/items (public)" "GET" "/api/menu/items" 200
    run_test "GET /actuator/health (public)" "GET" "/actuator/health" 200
    
    echo
    
    # Test 2: Protected endpoints without authentication (should fail)
    log_info "Testing protected endpoints without authentication..."
    run_test "POST /api/menu/items (no auth)" "POST" "/api/menu/items" 401 "" "$MENU_ITEM_DATA"
    run_test "PUT /api/menu/items/1 (no auth)" "PUT" "/api/menu/items/1" 401
    run_test "DELETE /api/menu/items/1 (no auth)" "DELETE" "/api/menu/items/1" 401
    
    echo
    
    # Test 3: Admin authentication
    log_info "Testing admin authentication..."
    local admin_token
    admin_token=$(authenticate_user "$ADMIN_USERNAME" "$ADMIN_PASSWORD")
    
    if [[ -n "$admin_token" ]]; then
        echo
        
        # Test 4: Admin operations (should succeed)
        log_info "Testing admin operations..."
        run_test "POST /api/menu/items (admin)" "POST" "/api/menu/items" 201 "$admin_token" "$MENU_ITEM_DATA"
        
        # Test 5: Token validation
        log_info "Testing token validation..."
        run_test "POST /api/auth/validate (admin)" "POST" "/api/auth/validate" 200 "$admin_token"
    else
        log_error "Cannot proceed with admin tests - authentication failed"
    fi
    
    echo
    
    # Test 6: Regular user authentication
    log_info "Testing regular user authentication..."
    local user_token
    user_token=$(authenticate_user "$USER_USERNAME" "$USER_PASSWORD")
    
    if [[ -n "$user_token" ]]; then
        echo
        
        # Test 7: Regular user operations (should fail for admin endpoints)
        log_info "Testing regular user access restrictions..."
        run_test "POST /api/menu/items (user)" "POST" "/api/menu/items" 403 "$user_token" "$MENU_ITEM_DATA"
        run_test "PUT /api/menu/items/1 (user)" "PUT" "/api/menu/items/1" 403 "$user_token" "$UPDATED_MENU_ITEM_DATA"
        run_test "DELETE /api/menu/items/1 (user)" "DELETE" "/api/menu/items/1" 403 "$user_token"
        
        # Test 8: Token validation for regular user
        log_info "Testing regular user token validation..."
        run_test "POST /api/auth/validate (user)" "POST" "/api/auth/validate" 200 "$user_token"
    else
        log_error "Cannot proceed with user tests - authentication failed"
    fi
    
    echo
    
    # Test Results Summary
    log_info "Test Results Summary"
    log_info "===================="
    log_info "Total Tests: $TESTS_TOTAL"
    log_success "Passed: $TESTS_PASSED"
    
    if [[ $TESTS_FAILED -gt 0 ]]; then
        log_error "Failed: $TESTS_FAILED"
    fi
    
    echo
    
    # Exit with appropriate code
    if [[ $TESTS_FAILED -eq 0 ]]; then
        log_success "All tests passed! Security implementation is working correctly."
        exit 0
    else
        log_error "Some tests failed. Please review the security implementation."
        exit 1
    fi
}

# Check dependencies
if ! command -v curl &> /dev/null; then
    log_error "curl is required but not installed. Please install curl and try again."
    exit 1
fi

# Run main function
main "$@"
