# Microservices Authentication & Authorization System

This project demonstrates a complete authentication and authorization flow using multiple microservices.

## Architecture Overview

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │───▶│   Gateway   │───▶│   Service1  │    │     SSO     │
│             │    │   (8080)    │    │   (8082)    │    │   (8081)    │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
                          │                    │                    │
                          ▼                    ▼                    ▼
                   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
                   │     ACL     │    │   Service2  │    │ PostgreSQL  │
                   │   (8083)    │    │   (8084)    │    │   (5432)    │
                   └─────────────┘    └─────────────┘    └─────────────┘
```

## Services

1. **Gateway (Port 8080)** - API Gateway with authentication/authorization
2. **SSO (Port 8081)** - Authentication service with JWT tokens
3. **Service1 (Port 8082)** - Protected microservice
4. **ACL (Port 8083)** - Access Control List service
5. **PostgreSQL** - Database for users and permissions

## Complete Flow

### 1. User Authentication Flow

```
1. User calls: POST http://localhost:8081/api/auth/login
   Body: {"username": "testuser", "password": "password123"}
   
2. SSO validates credentials and returns JWT token
   Response: {"token": "eyJ...", "success": true}
```

### 2. Service Access Flow

```
1. User calls: GET http://localhost:8080/service1/app1/hello
   Header: Authorization: Bearer <token>
   
2. Gateway checks for Authorization header
   - If missing: Returns 401 Unauthorized
   
3. Gateway validates token with SSO
   - Calls: POST http://localhost:8081/api/auth/validate
   
4. Gateway checks authorization with ACL
   - Calls: POST http://localhost:8083/api/acl/check
   
5. If both auth & authz successful:
   - Request forwarded to Service1
   - Headers added: X-Authenticated-User, X-Validated-Token
   
6. If authorization fails:
   - Returns 403 Forbidden
```

## Setup Instructions

### 1. Database Setup

Create PostgreSQL databases:
```sql
CREATE DATABASE sso_db;
CREATE DATABASE acl_db;
```

### 2. Start Services

```bash
# Start SSO Service
cd sso && mvn spring-boot:run

# Start ACL Service  
cd acl && mvn spring-boot:run

# Start Service1
cd service1 && mvn spring-boot:run

# Start Gateway
cd gateway && mvn spring-boot:run
```

## Testing the Flow

### Step 1: Login to Get Token

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJSUzI1NiJ9...",
  "message": "Login successful",
  "success": true
}
```

### Step 2: Access Service Without Token (Should Fail)

```bash
curl http://localhost:8080/service1/app1/hello
```

**Expected Response:**
```
401 Unauthorized
```

### Step 3: Access Service With Token (Should Succeed)

```bash
curl http://localhost:8080/service1/app1/hello \
  -H "Authorization: Bearer <token-from-step-1>"
```

**Expected Response:**
```
Hello From Service 1 - Authenticated User: testuser
```

### Step 4: Test Unauthorized Access

Try accessing a resource that the user doesn't have permission for:

```bash
curl http://localhost:8080/service1/app1/admin \
  -H "Authorization: Bearer <token-from-step-1>"
```

**Expected Response:**
```
403 Forbidden
```

## API Endpoints

### SSO Service (Port 8081)

- `POST /api/auth/login` - User login
- `POST /api/auth/validate` - Token validation
- `GET /api/auth/public-key` - Get RSA public key
- `GET /api/auth/health` - Health check

### ACL Service (Port 8083)

- `POST /api/acl/check` - Check permissions
- `POST /api/acl/add` - Add permissions
- `GET /api/acl/health` - Health check

### Gateway (Port 8080)

- `GET /service1/**` - Route to Service1 (requires authentication)

### Service1 (Port 8082)

- `GET /app1/hello` - Public endpoint
- `GET /app1/admin` - Admin endpoint

## Default Users & Permissions

### Users
- **testuser** / **password123** - Regular user
- **admin** / **admin123** - Admin user

### Permissions
- **testuser** can access `/service1/app1/hello`
- **admin** can access `/service1/**` (all service1 endpoints)

## Security Features

1. **JWT Authentication** - RSA-signed tokens
2. **Role-based Authorization** - ACL-based permissions
3. **Gateway-level Security** - All requests go through gateway
4. **Token Validation** - Real-time token validation with SSO
5. **Permission Checking** - Real-time permission checking with ACL

## Error Responses

### 401 Unauthorized
- Missing Authorization header
- Invalid token
- Token validation failure

### 403 Forbidden
- Valid token but insufficient permissions
- User not authorized for requested resource

## Monitoring & Logging

All services include comprehensive logging:
- Authentication attempts
- Authorization checks
- Request routing
- Error conditions

## Configuration

### Gateway Configuration
- Routes all `/service1/**` requests to Service1
- Applies AuthenticationFilter to all service1 routes
- Validates tokens with SSO service
- Checks permissions with ACL service

### SSO Configuration
- RSA 2048-bit key generation
- JWT token expiration: 24 hours
- BCrypt password encryption

### ACL Configuration
- PostgreSQL-based permission storage
- Real-time permission checking
- Support for resource-based permissions

## Development Notes

### Adding New Services
1. Create service with unique port
2. Add route configuration in gateway
3. Add permissions in ACL service
4. Test authentication/authorization flow

### Adding New Permissions
1. Use ACL service to add permissions
2. Test with appropriate user tokens
3. Verify authorization works correctly

### Troubleshooting
1. Check service logs for authentication/authorization issues
2. Verify database connections
3. Check token validity with SSO service
4. Verify permissions with ACL service 