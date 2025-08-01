# Microservices Authentication & Authorization System

## Overview
This project implements a comprehensive authentication and authorization system using microservices architecture with Spring Boot, JWT tokens, Redis caching, and PostgreSQL databases.

## Architecture

### Services
- **SSO Service** (Port 8081): Central authentication service with JWT token generation
- **Gateway Service** (Port 8080): API Gateway with authentication/authorization filtering
- **ACL Service** (Port 8083): Access Control List service for role-based permissions
- **Service1** (Port 8082): Example microservice with automatic permission registration
- **Redis** (Port 6379): High-performance caching layer
- **PostgreSQL**: Database for all services

## Public Key Distribution Flow

### New Architecture: Gateway as Public Key Provider
```
Service1 → Gateway → SSO → Gateway → Service1
```

### Benefits of Gateway Public Key Distribution:
- ✅ **Centralized Control**: Gateway manages all public key distribution
- ✅ **Consistent Security**: All services use the same public key source
- ✅ **Better Performance**: Gateway caches public key, reduces SSO load
- ✅ **Simplified Architecture**: Service1 doesn't need direct SSO access

### Complete Flow:

#### Step 1: Service1 Requests Public Key
```
Service1 → Gateway (/api/gateway/public-key)
    ↓
Gateway → SSO (/api/auth/public-key)
    ↓
SSO returns public key to Gateway
    ↓
Gateway returns public key to Service1
    ↓
Service1 caches public key in Redis
```

#### Step 2: Signature Verification
```
Service1 receives signed token from Gateway
    ↓
Service1 gets cached public key from Redis
    ↓
Service1 verifies signature using RSA
    ↓
If verification fails, Service1 refreshes public key from Gateway
```

### API Endpoints:

#### Gateway Public Key Endpoints:
- `GET /api/gateway/public-key`: Get SSO public key through Gateway
- `GET /api/gateway/public-key/health`: Check public key availability

#### Service1 Test Endpoints:
- `GET /service1/test/public-key/status`: Check public key status
- `GET /service1/test/public-key/refresh`: Force refresh public key

### Configuration:

#### Service1 Configuration:
```properties
# Gateway Service URL for Public Key
gateway.service.url=http://localhost:8080/api/gateway/public-key
```

#### Gateway Configuration:
```properties
# SSO Service URL for public key requests
sso.service.url=http://localhost:8081/api/auth/public-key
```

### Security Benefits:
- **Cryptographic Integrity**: Signed tokens prevent tampering
- **Public Key Verification**: RSA signature verification
- **Fallback Mechanism**: Hash verification if RSA fails
- **Automatic Refresh**: Public key refresh on verification failure

### Performance Benefits:
- **Cached Public Keys**: Redis caching reduces network calls
- **Single Source**: Gateway as central public key provider
- **Reduced SSO Load**: Gateway handles public key distribution
- **Fast Verification**: Local signature verification in Service1

## Redis Caching Strategy

### Overview
The system implements a sophisticated Redis-based caching strategy to reduce the load on SSO and ACL services while maintaining security through cryptographic signatures.

### Architecture
```
Client Request
    ↓
Gateway (Port 8080)
    ↓
1. Check Redis for cached signed token
    ↓
2. If cache miss:
   - Call SSO to validate token
   - Call ACL to get ALL permissions
   - Cache signed token with permissions
    ↓
3. Forward request to Service1
    ↓
Service1 uses cached token (no validation needed)
```

### Benefits
- **Cache Hit**: ~1ms (Redis lookup + signature verification)
- **Cache Miss**: ~50ms (SSO + ACL calls + caching)
- **Service1**: ~0ms (no validation needed)

### Security Features
- **Cryptographic signatures** prevent tampering
- **Single validation point** reduces attack surface
- **Consistent security** across all services

### Scalability Benefits
- **Reduced SSO load** (cached tokens)
- **Reduced ACL load** (cached permissions)
- **Better response times** for all services

## Automatic API Discovery

### Overview
Service1 automatically discovers all API endpoints on startup and registers them with the ACL service, eliminating manual permission configuration.

### Implementation
- **Automatic Scanning**: Scans all `@RestController` beans on startup
- **Permission Generation**: Automatically generates permission names from endpoints
- **ACL Registration**: Registers endpoints with ACL service
- **Metadata Support**: Includes extra data like Persian names, criticality flags

### Features
- **Complete URL Discovery**: Full endpoint URLs with HTTP methods
- **Permission Naming**: Automatic permission name generation
- **Criticality Detection**: Identifies critical endpoints automatically
- **Persian Names**: Generates Persian names for endpoints
- **Extra Data**: Includes category, description, and metadata

## Cryptographic Integrity Check

### Overview
The system implements cryptographic signing of cached token data to prevent manual insertion by hackers and ensure data integrity.

### Implementation
- **SignedTokenData**: New model with cryptographic signature
- **SHA-256 Signing**: Signs token + sorted permissions + timestamps
- **RSA Verification**: Uses SSO public key for signature verification
- **Fallback Mechanism**: Hash verification if RSA fails

### Security Features
- **Tamper Prevention**: Cryptographic signatures prevent manual insertion
- **Public Key Distribution**: Gateway distributes SSO public keys
- **Signature Verification**: Service1 verifies signatures before using cached data
- **Automatic Refresh**: Public key refresh on verification failure

## Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- Redis 6+
- Docker (optional)

### Database Setup
```sql
-- Create databases for each service
CREATE DATABASE sso_db;
CREATE DATABASE acl_db;
CREATE DATABASE service1_db;
CREATE DATABASE gateway_db;
```

### Redis Setup
```bash
# Start Redis server
redis-server

# Test Redis connection
redis-cli ping
```

### Service Startup Order
1. **PostgreSQL**: Database server
2. **Redis**: Caching layer
3. **SSO Service**: Authentication service
4. **ACL Service**: Authorization service
5. **Gateway Service**: API Gateway
6. **Service1**: Example microservice

### Build and Run
```bash
# Build all services
mvn clean install

# Run services (in separate terminals)
cd sso && mvn spring-boot:run
cd acl && mvn spring-boot:run
cd gateway && mvn spring-boot:run
cd service1 && mvn spring-boot:run
```

## Testing

### Test Public Key Flow
```bash
# Test Gateway public key endpoint
curl http://localhost:8080/api/gateway/public-key

# Test Service1 public key status
curl http://localhost:8082/service1/test/public-key/status

# Test public key refresh
curl http://localhost:8082/service1/test/public-key/refresh
```

### Test Authentication Flow
```bash
# 1. Login to get token
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"password"}'

# 2. Use token to access Service1 through Gateway
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/service1/app1/hello
```

### Test Caching
```bash
# First request (cache miss)
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/service1/app1/hello

# Second request (cache hit)
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/service1/app1/hello
```

## Monitoring

### Redis Monitoring
```bash
# Check Redis keys
redis-cli keys "*"

# Check token cache
redis-cli keys "token:*"

# Check public key cache
redis-cli keys "public_key:*"
```

### Service Logs
- **Gateway**: Look for "GATEWAY CACHE HIT/MISS" messages
- **Service1**: Look for "SIGNED CACHE HIT/MISS" messages
- **SSO**: Look for token validation requests
- **ACL**: Look for permission check requests

## Security Considerations

### Defense in Depth
- **Gateway Level**: Primary authentication/authorization
- **Service Level**: Secondary validation with cached data
- **Cryptographic Integrity**: Signed tokens prevent tampering
- **Public Key Verification**: RSA signature verification

### Cache Security
- **Signed Data**: All cached tokens are cryptographically signed
- **Signature Verification**: Service1 verifies signatures before using cached data
- **Automatic Refresh**: Public keys refresh on verification failure
- **Tamper Prevention**: Cryptographic signatures prevent manual insertion

### Network Security
- **HTTPS**: All inter-service communication should use HTTPS
- **Firewall**: Restrict access to service ports
- **Authentication**: All services require proper authentication
- **Authorization**: Role-based access control on all endpoints

## Performance Optimization

### Caching Strategy
- **Token Caching**: 30-minute TTL for signed tokens
- **Public Key Caching**: 24-hour TTL for public keys
- **Permission Caching**: All permissions cached with tokens
- **Signature Verification**: Local verification in Service1

### Load Distribution
- **Gateway**: Handles all initial authentication/authorization
- **Service1**: Uses cached data for fast responses
- **SSO**: Reduced load through caching
- **ACL**: Reduced load through caching

### Response Times
- **Cache Hit**: ~1ms (Redis lookup + signature verification)
- **Cache Miss**: ~50ms (SSO + ACL calls + caching)
- **Service1**: ~0ms (no validation needed)
- **Overall**: 90%+ cache hit rate expected

## Troubleshooting

### Common Issues

#### Public Key Issues
```bash
# Check Gateway public key endpoint
curl http://localhost:8080/api/gateway/public-key

# Check Service1 public key status
curl http://localhost:8082/service1/test/public-key/status

# Force refresh public key
curl http://localhost:8082/service1/test/public-key/refresh
```

#### Cache Issues
```bash
# Check Redis connection
redis-cli ping

# Check cached tokens
redis-cli keys "token:*"

# Clear all caches
redis-cli flushall
```

#### Service Communication
```bash
# Check SSO health
curl http://localhost:8081/api/auth/health

# Check ACL health
curl http://localhost:8083/api/acl/health

# Check Gateway health
curl http://localhost:8080/api/gateway/public-key/health
```

### Log Analysis
- **Gateway Logs**: Look for authentication/authorization messages
- **Service1 Logs**: Look for signature verification messages
- **SSO Logs**: Look for token validation requests
- **ACL Logs**: Look for permission check requests

## Future Enhancements

### Planned Features
- **Key Rotation**: Automatic RSA key rotation
- **Load Balancing**: Multiple SSO/ACL instances
- **Metrics**: Prometheus metrics integration
- **Tracing**: Distributed tracing with Jaeger
- **Circuit Breaker**: Resilience patterns
- **Rate Limiting**: API rate limiting
- **Audit Logging**: Comprehensive audit trails

### Security Enhancements
- **HTTPS**: TLS encryption for all communications
- **Certificate Management**: Automatic certificate rotation
- **Secrets Management**: External secrets management
- **Network Policies**: Kubernetes network policies
- **Pod Security**: Pod security standards

### Performance Enhancements
- **Connection Pooling**: Optimized database connections
- **Query Optimization**: Database query optimization
- **CDN Integration**: Content delivery network
- **Load Testing**: Comprehensive load testing
- **Auto Scaling**: Kubernetes auto-scaling 