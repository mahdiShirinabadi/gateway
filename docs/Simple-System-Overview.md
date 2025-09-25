# Simple System Overview

## Overview
This document describes the simplified microservices system with minimal code and no Redis dependencies.

## System Architecture

### 1. SSO Service (Port 8080)
**Purpose**: User authentication and JWT token generation

**Key Components**:
- User login endpoint
- JWT token generation
- Token validation endpoint

**Endpoints**:
```
POST /sso/api/auth/login     - User login
POST /sso/api/auth/validate  - Token validation
GET  /sso/api/auth/health    - Health check
```

### 2. Gateway Service (Port 8080)
**Purpose**: API Gateway with simple token validation

**Key Components**:
- SimpleAuthenticationFilter: Validates tokens with SSO
- No Redis caching
- Direct SSO validation

**Endpoints**:
```
GET  /api/gateway/health     - Health check
GET  /api/gateway/public     - Public endpoints
```

### 3. ACL Service (Port 8080)
**Purpose**: Access Control List for permission checking

**Key Components**:
- SimpleAclService: Basic permission checking
- SimpleAclController: Permission check endpoints

**Endpoints**:
```
POST /api/acl/check          - Check user permissions
GET  /api/acl/health         - Health check
```

### 4. Service1 (Port 8082)
**Purpose**: Business service with JWT authentication

**Key Components**:
- TokenValidationService: JWT validation
- SimpleJwtAuthenticationFilter: Authentication filter
- SimpleAclService: Permission checking
- SimpleServiceController: Business endpoints

**Endpoints**:
```
GET  /service1/hello         - Hello endpoint (requires SERVICE1_HELLO_ACCESS)
GET  /service1/admin         - Admin endpoint (requires SERVICE1_ADMIN_ACCESS)
GET  /service1/user          - User endpoint (requires SERVICE1_USER_ACCESS)
GET  /service1/health       - Health check (public)
GET  /service1/test/token-info    - Test token info
GET  /service1/test/permissions  - Test permissions
```

### 5. Config Server (Port 8888)
**Purpose**: Centralized configuration management

**Key Components**:
- shared.properties: Shared configuration
- service1.properties: Service1 specific configuration

## Authentication Flow

### Step 1: User Login
```
User → SSO Service → JWT Token
```

### Step 2: User Request to Gateway
```
User → Gateway (with JWT token in Authorization header)
```

### Step 3: Gateway Validates Token
```
Gateway → SSO Service (validate token) → Valid/Invalid
```

### Step 4: Gateway Forwards to Service1
```
Gateway → Service1 (with JWT token in Authorization header)
```

### Step 5: Service1 Validates and Checks Permissions
```
Service1 → JWT Validation → ACL Check → 200/403 Response
```

## Configuration

### Shared Configuration (shared.properties)
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

# Shared Signature Configuration
shared.signature.format=username|permissions|token
shared.signature.algorithm=SHA-256
shared.signature.encoding=UTF-8
```

### Service1 Configuration (service1.properties)
```properties
# Service1 specific configuration
server.port=8082
spring.application.name=service1
server.servlet.context-path=/service1

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/service1_db
spring.datasource.username=service1
spring.datasource.password=service1
```

## Testing the System

### 1. Test SSO Login
```bash
curl -X POST http://localhost:8080/sso/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password"}'
```

### 2. Test Token Validation
```bash
curl -X POST http://localhost:8080/sso/api/auth/validate \
  -H "Authorization: Bearer <jwt-token>"
```

### 3. Test Service1 Endpoints
```bash
# Test Hello endpoint
curl -X GET http://localhost:8082/service1/hello \
  -H "Authorization: Bearer <jwt-token>"

# Test Admin endpoint
curl -X GET http://localhost:8082/service1/admin \
  -H "Authorization: Bearer <jwt-token>"

# Test Health endpoint (public)
curl -X GET http://localhost:8082/service1/health
```

### 4. Test ACL Service
```bash
curl -X POST http://localhost:8080/api/acl/check \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","resource":"service1","action":"read"}'
```

## JWT Token Structure

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

## Response Examples

### Success Response (200)
```json
{
  "message": "Hello from Service1!",
  "endpoint": "/service1/hello",
  "status": "success"
}
```

### Unauthorized Response (401)
```json
{
  "error": "Unauthorized",
  "message": "Invalid or missing token"
}
```

### Forbidden Response (403)
```json
{
  "error": "Access denied",
  "message": "Insufficient permissions for SERVICE1_ADMIN_ACCESS"
}
```

## Benefits of Simple System

### ✅ No Redis Dependencies
- **Simpler Architecture**: No Redis server required
- **Easier Deployment**: Fewer components to manage
- **Lower Latency**: Direct JWT validation
- **Stateless**: No session storage needed

### ✅ Minimal Code
- **Simple Services**: Each service has minimal code
- **Clear Responsibilities**: Each service has a single purpose
- **Easy to Understand**: Simple authentication flow
- **Easy to Test**: Simple endpoints for testing

### ✅ Direct Communication
- **Gateway → SSO**: Direct token validation
- **Service1 → JWT**: Direct JWT parsing
- **No Caching**: No cache invalidation complexity
- **Real-time**: Always up-to-date permissions

## Future Enhancements

When needed, you can add:
- **Redis Caching**: For performance optimization
- **Token Refresh**: For long-lived sessions
- **Rate Limiting**: For security
- **Monitoring**: For observability

## Conclusion

This simple system provides:
- ✅ **Clean Architecture**: Minimal, focused services
- ✅ **No Dependencies**: No Redis or complex caching
- ✅ **Easy Testing**: Simple endpoints to test
- ✅ **Clear Flow**: Straightforward authentication flow
- ✅ **Maintainable**: Easy to understand and modify

Perfect for getting started with microservices authentication!

