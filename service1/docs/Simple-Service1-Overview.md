# Simple Service1 Overview

## Overview
Service1 is now very simple with only one controller and one method that validates tokens with SSO and checks permissions with ACL.

## Service1 Components

### 1. HelloController
**Purpose**: Single controller with one hello method

**Method**: `GET /service1/hello`
- Validates token with SSO service
- Gets username from token
- Checks permission with ACL service
- Returns hello message if authorized

### 2. AclRegistrationService
**Purpose**: Registers Service1 methods with ACL service

**Functionality**:
- Registers hello method with ACL on startup
- Creates permission: `SERVICE1_HELLO_ACCESS`

### 3. Service1StartupListener
**Purpose**: Registers with ACL service on application startup

**Functionality**:
- Automatically registers Service1 methods when application starts
- Ensures ACL service knows about Service1 permissions

### 4. WebClientConfig
**Purpose**: Configuration for HTTP calls to SSO and ACL services

## Authentication Flow

### Step 1: Token Validation
```
HelloController → SSO Service (validate token)
```

### Step 2: Username Extraction
```
HelloController → Extract username from token
```

### Step 3: Permission Check
```
HelloController → ACL Service (check permission)
```

### Step 4: Response
```
HelloController → Return 200/401/403 response
```

## API Endpoint

### Hello Endpoint
```bash
GET /service1/hello
Authorization: Bearer <jwt-token>
```

**Response (Success - 200)**:
```json
{
  "message": "Hello from Service1!",
  "username": "testuser",
  "status": "success"
}
```

**Response (Unauthorized - 401)**:
```json
{
  "error": "Invalid token",
  "message": "Token validation failed"
}
```

**Response (Forbidden - 403)**:
```json
{
  "error": "Access denied",
  "message": "Insufficient permissions"
}
```

## ACL Registration

### Automatic Registration
When Service1 starts, it automatically registers with ACL service:

```json
{
  "service": "service1",
  "method": "hello",
  "httpMethod": "GET",
  "description": "Hello endpoint access",
  "permission": "SERVICE1_HELLO_ACCESS"
}
```

### Permission Check
When user calls `/service1/hello`, Service1 checks:
- Username: `testuser`
- Resource: `service1`
- Action: `hello`
- Required Permission: `SERVICE1_HELLO_ACCESS`

## Configuration

### application.properties
```properties
# Simple Service1 Configuration
server.port=8082
server.servlet.context-path=/service1

# Spring Boot 2.4+ Configuration Import
spring.config.import=optional:configserver:http://localhost:8888
spring.application.name=service1

# Spring Cloud Config Configuration
spring.cloud.config.name=service1,shared
spring.cloud.config.profile=default
spring.cloud.config.label=master
spring.cloud.config.fail-fast=true

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/service1_db
spring.datasource.username=service1
spring.datasource.password=service1

# Eureka Client Configuration
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

## Testing Service1

### 1. Test Hello Endpoint
```bash
curl -X GET http://localhost:8082/service1/hello \
  -H "Authorization: Bearer <jwt-token>"
```

### 2. Test Without Token
```bash
curl -X GET http://localhost:8082/service1/hello
```

### 3. Test With Invalid Token
```bash
curl -X GET http://localhost:8082/service1/hello \
  -H "Authorization: Bearer invalid-token"
```

## Benefits of Simple Service1

### ✅ **Minimal Code**
- **One Controller**: Only HelloController
- **One Method**: Only hello method
- **Simple Logic**: Clear authentication flow
- **Easy to Understand**: No complex services

### ✅ **Clear Authentication Flow**
- **SSO Validation**: Direct token validation with SSO
- **ACL Permission**: Direct permission check with ACL
- **Simple Responses**: Clear 200/401/403 responses

### ✅ **Automatic Registration**
- **Startup Registration**: Automatically registers with ACL
- **No Manual Setup**: No need to manually configure permissions
- **Self-Contained**: Service1 manages its own ACL registration

### ✅ **Easy Testing**
- **Single Endpoint**: Only one endpoint to test
- **Clear Responses**: Easy to understand success/error responses
- **Simple Flow**: Straightforward authentication flow

## Conclusion

Service1 is now **very simple**:
- ✅ **One Controller**: HelloController
- ✅ **One Method**: hello method
- ✅ **SSO Integration**: Token validation with SSO
- ✅ **ACL Integration**: Permission check with ACL
- ✅ **Automatic Registration**: Registers with ACL on startup
- ✅ **Easy Testing**: Simple endpoint to test

Perfect for getting started with the authentication flow!
