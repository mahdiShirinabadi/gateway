# Simple Authentication Flow

## Overview
This document explains the simplified authentication flow without Redis, as requested by the user.

## Flow Steps

### 1. User Login in SSO
```
User → SSO Service → JWT Token
```

### 2. User Sends Token to Gateway
```
User → Gateway (with JWT token in Authorization header)
```

### 3. Gateway Validates Token with SSO
```
Gateway → SSO Service (validate token) → Valid/Invalid
```

### 4. Gateway Redirects to Service1
```
Gateway → Service1 (with username and token in headers)
```

### 5. Service1 Checks Token and ACL
```
Service1 → JWT Validation → ACL Check → 200/403 Response
```

## Implementation Details

### Service1 Components

#### 1. TokenValidationService
```java
@Service
public class TokenValidationService {
    
    // Validate JWT token using public key
    public boolean validateToken(String token) {
        // Parse JWT with public key
        // Check expiration
        // Return validation result
    }
    
    // Extract username from token
    public String getUsernameFromToken(String token) {
        // Parse JWT claims
        // Return subject (username)
    }
    
    // Check user permissions
    public boolean hasPermission(String token, String permission) {
        // Parse JWT claims
        // Check permissions array
        // Return permission result
    }
}
```

#### 2. SimpleJwtAuthenticationFilter
```java
@Component
public class SimpleJwtAuthenticationFilter extends OncePerRequestFilter {
    
    // Extract token from Authorization header
    // Validate token with TokenValidationService
    // Set Spring Security context
    // Continue filter chain
}
```

#### 3. SimpleAclService
```java
@Service
public class SimpleAclService {
    
    // Check if user has access to resource
    public boolean hasAccess(String token, String path, String method) {
        // Validate token
        // Determine required permission
        // Check user permissions
        // Return access result
    }
}
```

#### 4. SimpleServiceController
```java
@RestController
public class SimpleServiceController {
    
    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> hello(@RequestHeader("Authorization") String token) {
        // Check ACL permission
        // Return 200 if authorized, 403 if not
    }
}
```

## API Endpoints

### Service1 Endpoints

#### 1. Hello Endpoint
```bash
GET /service1/hello
Authorization: Bearer <jwt-token>
```
**Required Permission**: `SERVICE1_HELLO_ACCESS`

#### 2. Admin Endpoint
```bash
GET /service1/admin
Authorization: Bearer <jwt-token>
```
**Required Permission**: `SERVICE1_ADMIN_ACCESS`

#### 3. User Endpoint
```bash
GET /service1/user
Authorization: Bearer <jwt-token>
```
**Required Permission**: `SERVICE1_USER_ACCESS`

#### 4. Health Endpoint
```bash
GET /service1/health
```
**Required Permission**: None (Public)

#### 5. Test Endpoints
```bash
GET /service1/test/token-info
Authorization: Bearer <jwt-token>

GET /service1/test/permissions
Authorization: Bearer <jwt-token>
```

## Configuration

### application.properties
```properties
# Spring Boot 2.4+ Configuration Import
spring.config.import=optional:configserver:http://localhost:8888
spring.application.name=service1

# Spring Cloud Config Configuration
spring.cloud.config.name=service1,shared
spring.cloud.config.profile=default
spring.cloud.config.label=master
spring.cloud.config.fail-fast=true

# JWT Configuration (from config-server)
# shared.public-key will be loaded from shared.properties
```

### shared.properties (Config-Server)
```properties
# Shared Public Key for JWT validation
shared.public-key=-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyG2RcM8Rd7T2Qc8Cl4Be
zKFgKD5kw6aDub1OJedMrZFRk/JDw+rGzX9mQBWG6uBdlI8CadhC3BRpkTQ7X+d+
eCzOi1mXTSmTIGUHw+VTzpiI5CzSPwQb1bOghutLtl7vHHFh1kcQ5SJ9SkR+GSmd
o5IxFZDDX1orIW0Mxu01guGNyKEh8MAuJZV+hfwaiQ441dMPMCyiO+FU/97BNQCo
IA1M6xPMe2MncsvHDKVBcj4fBqAw1pCtQtWVF5nWZKYQ/1z7igirUw/72/nTDpCH
65SX9a02ytx9rburr6UQq7WmnI/lcJm0+EfosQSZj/2DP/lcV7tnkZuB0rYEKw9x
oQIDAQAB
-----END PUBLIC KEY-----
```

## Testing the Flow

### 1. Test Token Validation
```bash
curl -X GET http://localhost:8082/service1/test/token-info \
  -H "Authorization: Bearer <jwt-token>"
```

**Expected Response**:
```json
{
  "tokenValid": true,
  "username": "testuser",
  "message": "Token is valid"
}
```

### 2. Test Permissions
```bash
curl -X GET http://localhost:8082/service1/test/permissions \
  -H "Authorization: Bearer <jwt-token>"
```

**Expected Response**:
```json
{
  "adminAccess": false,
  "userAccess": true,
  "helloAccess": true,
  "message": "Permission check completed"
}
```

### 3. Test Hello Endpoint
```bash
curl -X GET http://localhost:8082/service1/hello \
  -H "Authorization: Bearer <jwt-token>"
```

**Expected Response** (if authorized):
```json
{
  "message": "Hello from Service1!",
  "endpoint": "/service1/hello",
  "status": "success"
}
```

**Expected Response** (if not authorized):
```json
{
  "error": "Access denied",
  "message": "Insufficient permissions for SERVICE1_HELLO_ACCESS"
}
```

### 4. Test Admin Endpoint
```bash
curl -X GET http://localhost:8082/service1/admin \
  -H "Authorization: Bearer <jwt-token>"
```

**Expected Response** (if authorized):
```json
{
  "message": "Admin access granted!",
  "endpoint": "/service1/admin",
  "status": "success"
}
```

**Expected Response** (if not authorized):
```json
{
  "error": "Access denied",
  "message": "Insufficient permissions for SERVICE1_ADMIN_ACCESS"
}
```

## JWT Token Structure

The JWT token should contain the following claims:

```json
{
  "sub": "username",
  "permissions": [
    "SERVICE1_HELLO_ACCESS",
    "SERVICE1_USER_ACCESS"
  ],
  "exp": 1234567890,
  "iat": 1234567890
}
```

## Error Responses

### 401 Unauthorized
```json
{
  "error": "Invalid token",
  "message": "JWT token validation failed"
}
```

### 403 Forbidden
```json
{
  "error": "Access denied",
  "message": "Insufficient permissions for SERVICE1_ADMIN_ACCESS"
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal server error",
  "message": "Error processing request"
}
```

## Benefits of Simple Approach

### ✅ No Redis Dependency
- **Simpler Architecture**: No Redis server required
- **Easier Deployment**: Fewer components to manage
- **Lower Latency**: Direct JWT validation
- **Stateless**: No session storage needed

### ✅ Direct JWT Validation
- **Fast Validation**: Direct JWT parsing and validation
- **Public Key Verification**: RSA signature verification
- **Expiration Check**: Built-in JWT expiration validation
- **Permission Extraction**: Direct permission extraction from JWT claims

### ✅ Simple ACL
- **Path-Based Permissions**: Permissions based on endpoint path
- **Method-Based Permissions**: Different permissions for different HTTP methods
- **Flexible Mapping**: Easy to add new permission mappings
- **Clear Responses**: Clear 403 responses for unauthorized access

## Future Enhancements

When latency becomes an issue, you can add:
- **Redis Caching**: Cache validated tokens
- **Token Refresh**: Implement token refresh mechanism
- **Permission Caching**: Cache user permissions
- **Rate Limiting**: Add rate limiting for security

## Conclusion

This simple approach provides:
- ✅ **Clean Architecture**: No unnecessary complexity
- ✅ **Fast Performance**: Direct JWT validation
- ✅ **Easy Testing**: Simple endpoints to test
- ✅ **Clear Error Messages**: Clear 401/403 responses
- ✅ **Flexible Permissions**: Easy to add new permissions

Perfect for getting started with the authentication flow!
