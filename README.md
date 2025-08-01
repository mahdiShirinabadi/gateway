# Microservices Authentication & Authorization System

## Overview
This project implements a comprehensive authentication and authorization system using microservices architecture with Spring Boot, JWT tokens, Redis caching, PostgreSQL databases, and **Spring Cloud Config** for centralized configuration management.

## Architecture

### Services
- **Config Server** (Port 8888): Centralized configuration management
- **SSO Service** (Port 8081): Central authentication service with JWT token generation
- **Gateway Service** (Port 8080): API Gateway with authentication/authorization filtering
- **ACL Service** (Port 8083): Access Control List service for role-based permissions
- **Service1** (Port 8082): Example microservice with automatic permission registration
- **Redis** (Port 6379): High-performance caching layer
- **PostgreSQL**: Database for all services

## Spring Cloud Config Architecture

### Overview
**Spring Cloud Config** provides centralized configuration management for all public keys and sensitive configuration data across all microservices.

### Benefits
- ✅ **Centralized Configuration**: All public keys in one place
- ✅ **Dynamic Updates**: Keys can be updated without restart
- ✅ **Version Control**: Keys are versioned in Git
- ✅ **Environment Specific**: Different keys for different environments
- ✅ **Security**: Encrypted sensitive data

### Architecture Flow
```
Service1 → Config Server (Port 8888) → Configuration Files
    ↓
Service1 caches public keys in Redis
    ↓
Service1 uses cached keys for signature verification
```

### Configuration Structure

#### Config Server (Port 8888)
```
config-server/
├── src/main/resources/
│   ├── application.properties
│   └── config/
│       ├── gateway.properties
│       ├── service1.properties
│       └── sso.properties
```

#### Service1 Configuration
```properties
# Public Keys from Config Server
public.keys.gateway.public-key=GATEWAY_PUBLIC_KEY
public.keys.gateway.key-type=RSA
public.keys.gateway.key-size=2048

public.keys.sso.public-key=SSO_PUBLIC_KEY
public.keys.sso.key-type=RSA
public.keys.sso.key-size=2048

public.keys.service1.public-key=SERVICE1_PUBLIC_KEY
public.keys.service1.key-type=RSA
public.keys.service1.key-size=2048
```

### Complete Flow

#### Step 1: Service1 Connects to Config Server
```
Service1 startup
    ↓
Service1 reads bootstrap.properties
    ↓
Service1 connects to Config Server (Port 8888)
    ↓
Config Server provides configuration
    ↓
Service1 caches public keys in Redis
```

#### Step 2: Signature Verification
```
Service1 receives signed token
    ↓
Service1 gets cached public key from Redis
    ↓
If not cached, Service1 gets from Config Server
    ↓
Service1 verifies signature using public key
    ↓
Service1 uses token if verification succeeds
```

### API Endpoints

#### Config Server Endpoints:
- `GET /{application}/{profile}`: Get configuration for application
- `GET /{application}/{profile}/{label}`: Get configuration with label

#### Service1 Test Endpoints:
- `GET /service1/test/config/status`: Check Config Server status
- `GET /service1/test/config/refresh/{serviceName}`: Refresh config for service
- `GET /service1/test/config/keys`: Get all config keys

### Configuration Properties

#### Service1 Bootstrap Configuration:
```properties
# Spring Cloud Config Client Configuration
spring.application.name=service1
spring.cloud.config.uri=http://localhost:8888
spring.cloud.config.fail-fast=true
spring.cloud.config.retry.initial-interval=1000
spring.cloud.config.retry.max-interval=2000
spring.cloud.config.retry.max-attempts=6
```

#### Config Server Configuration:
```properties
# Server Configuration
server.port=8888

# For local development (file-based config)
spring.profiles.active=native
spring.cloud.config.server.native.search-locations=classpath:/config
```

### Security Benefits

#### ✅ Centralized Key Management
- **Single Source of Truth**: All public keys in Config Server
- **Version Control**: Keys are versioned and tracked
- **Environment Isolation**: Different keys for different environments
- **Dynamic Updates**: Keys can be updated without service restart

#### ✅ Enhanced Security
- **Encrypted Configuration**: Sensitive data can be encrypted
- **Access Control**: Config Server can be secured
- **Audit Trail**: All configuration changes are tracked
- **Rollback Capability**: Previous configurations can be restored

### Performance Benefits

#### ✅ Caching Strategy
- **Config Caching**: Service1 caches configuration in Redis
- **Fast Access**: Local cache for quick configuration access
- **Reduced Network Calls**: Minimize calls to Config Server
- **Fallback Mechanism**: Local cache if Config Server unavailable

#### ✅ Load Distribution
- **Config Server**: Handles all configuration requests
- **Service1**: Uses cached configuration for fast access
- **Redis**: High-performance caching layer
- **Reduced Latency**: Local configuration access

### Testing the Spring Cloud Config Integration

#### Test 1: Check Config Server Status
```bash
# Check if Config Server is running
curl http://localhost:8888/service1/default

# Check specific configuration
curl http://localhost:8888/service1/default/master
```

#### Test 2: Check Service1 Config Status
```bash
# Check if Service1 can access Config Server
curl http://localhost:8082/service1/test/config/status

# Get all configuration keys
curl http://localhost:8082/service1/test/config/keys
```

#### Test 3: Refresh Configuration
```bash
# Refresh Gateway configuration
curl http://localhost:8082/service1/test/config/refresh/gateway

# Refresh SSO configuration
curl http://localhost:8082/service1/test/config/refresh/sso
```

### Setup Instructions

#### 1. Start Config Server
```bash
cd config-server
mvn spring-boot:run
```

#### 2. Update Configuration Files
```bash
# Update public keys in config files
# config-server/src/main/resources/config/service1.properties
public.keys.gateway.public-key=YOUR_GATEWAY_PUBLIC_KEY
public.keys.sso.public-key=YOUR_SSO_PUBLIC_KEY
```

#### 3. Start Service1
```bash
cd service1
mvn spring-boot:run
```

#### 4. Verify Configuration
```bash
# Check if Service1 can access Config Server
curl http://localhost:8082/service1/test/config/status
```

### Monitoring

#### Config Server Monitoring
```bash
# Check Config Server health
curl http://localhost:8888/actuator/health

# Check configuration endpoints
curl http://localhost:8888/service1/default
```

#### Service1 Configuration Monitoring
```bash
# Check cached configuration
redis-cli keys "public_key:*"

# Check configuration status
curl http://localhost:8082/service1/test/config/status
```

### Troubleshooting

#### Common Issues

##### Config Server Connection Issues
```bash
# Check Config Server logs
tail -f config-server/logs/application.log

# Check Service1 bootstrap logs
tail -f service1/logs/application.log
```

##### Configuration Refresh Issues
```bash
# Force refresh configuration
curl -X POST http://localhost:8082/actuator/refresh

# Check configuration cache
redis-cli keys "*config*"
```

##### Public Key Issues
```bash
# Check public key availability
curl http://localhost:8082/service1/test/config/keys

# Refresh specific service keys
curl http://localhost:8082/service1/test/config/refresh/gateway
```

## Public Key Distribution Flow

### New Architecture: Config Server as Public Key Provider
```
Service1 → Config Server → Configuration Files
```

### Benefits of Config Server Public Key Distribution:
- ✅ **Centralized Control**: Config Server manages all public key distribution
- ✅ **Consistent Security**: All services use the same public key source
- ✅ **Better Performance**: Config Server caches public keys, reduces network load
- ✅ **Simplified Architecture**: Service1 doesn't need direct service access

### Complete Flow:

#### Step 1: Service1 Requests Public Keys
```
Service1 → Config Server (Port 8888)
    ↓
Config Server provides public keys from configuration files
    ↓
Service1 caches public keys in Redis
```

#### Step 2: Signature Verification
```
Service1 receives signed token from Gateway
    ↓
Service1 gets cached public key from Redis
    ↓
Service1 verifies signature using RSA
    ↓
If verification fails, Service1 refreshes public key from Config Server
```

### API Endpoints:

#### Config Server Public Key Endpoints:
- `GET /service1/default`: Get Service1 configuration with public keys
- `GET /gateway/default`: Get Gateway configuration with public keys
- `GET /sso/default`: Get SSO configuration with public keys

#### Service1 Test Endpoints:
- `GET /service1/test/config/status`: Check Config Server status
- `GET /service1/test/config/refresh/{serviceName}`: Force refresh public key
- `GET /service1/test/config/keys`: Get all configuration keys

### Configuration:

#### Service1 Bootstrap Configuration:
```properties
# Config Server URL
spring.cloud.config.uri=http://localhost:8888
spring.application.name=service1
spring.cloud.config.fail-fast=true
```

#### Config Server Configuration:
```properties
# Local file-based configuration
spring.profiles.active=native
spring.cloud.config.server.native.search-locations=classpath:/config
```

### Security Benefits:
- **Centralized Key Management**: All public keys in Config Server
- **Version Control**: Keys are versioned and tracked
- **Environment Isolation**: Different keys for different environments
- **Dynamic Updates**: Keys can be updated without service restart

### Performance Benefits:
- **Config Caching**: Service1 caches configuration in Redis
- **Fast Access**: Local cache for quick configuration access
- **Reduced Network Calls**: Minimize calls to Config Server
- **Fallback Mechanism**: Local cache if Config Server unavailable

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
- **RSA Verification**: Uses public keys from Config Server for signature verification
- **Fallback Mechanism**: Hash verification if RSA fails

### Security Features
- **Tamper Prevention**: Cryptographic signatures prevent manual insertion
- **Config Server Distribution**: Config Server distributes all public keys
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
3. **Config Server**: Configuration management (Port 8888)
4. **SSO Service**: Authentication service (Port 8081)
5. **ACL Service**: Authorization service (Port 8083)
6. **Gateway Service**: API Gateway (Port 8080)
7. **Service1**: Example microservice (Port 8082)

### Build and Run
```bash
# Build all services
mvn clean install

# Run services (in separate terminals)
cd config-server && mvn spring-boot:run
cd sso && mvn spring-boot:run
cd acl && mvn spring-boot:run
cd gateway && mvn spring-boot:run
cd service1 && mvn spring-boot:run
```

## Testing

### Test Config Server Integration
```bash
# Test Config Server
curl http://localhost:8888/service1/default

# Test Service1 config status
curl http://localhost:8082/service1/test/config/status

# Test config refresh
curl http://localhost:8082/service1/test/config/refresh/gateway
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

# Check config cache
redis-cli keys "*config*"
```

### Service Logs
- **Config Server**: Look for configuration requests
- **Gateway**: Look for "GATEWAY CACHE HIT/MISS" messages
- **Service1**: Look for "SIGNED CACHE HIT/MISS" messages
- **SSO**: Look for token validation requests
- **ACL**: Look for permission check requests

## Security Considerations

### Defense in Depth
- **Config Server Level**: Centralized configuration management
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
- **Config Caching**: 1-hour TTL for configuration data
- **Permission Caching**: All permissions cached with tokens
- **Signature Verification**: Local verification in Service1

### Load Distribution
- **Config Server**: Handles all configuration requests
- **Gateway**: Handles all initial authentication/authorization
- **Service1**: Uses cached data for fast responses
- **SSO**: Reduced load through caching
- **ACL**: Reduced load through caching

### Response Times
- **Cache Hit**: ~1ms (Redis lookup + signature verification)
- **Cache Miss**: ~50ms (SSO + ACL calls + caching)
- **Config Server**: ~10ms (configuration retrieval)
- **Service1**: ~0ms (no validation needed)
- **Overall**: 90%+ cache hit rate expected

## Troubleshooting

### Common Issues

#### Config Server Issues
```bash
# Check Config Server health
curl http://localhost:8888/actuator/health

# Check configuration endpoints
curl http://localhost:8888/service1/default

# Check Config Server logs
tail -f config-server/logs/application.log
```

#### Public Key Issues
```bash
# Check public key availability
curl http://localhost:8082/service1/test/config/keys

# Force refresh public keys
curl http://localhost:8082/service1/test/config/refresh/gateway
curl http://localhost:8082/service1/test/config/refresh/sso
```

#### Cache Issues
```bash
# Check Redis connection
redis-cli ping

# Check cached tokens
redis-cli keys "token:*"

# Check cached public keys
redis-cli keys "public_key:*"

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

# Check Config Server health
curl http://localhost:8888/actuator/health
```

### Log Analysis
- **Config Server Logs**: Look for configuration requests
- **Gateway Logs**: Look for authentication/authorization messages
- **Service1 Logs**: Look for signature verification messages
- **SSO Logs**: Look for token validation requests
- **ACL Logs**: Look for permission check requests

## Future Enhancements

### Planned Features
- **Key Rotation**: Automatic RSA key rotation
- **Load Balancing**: Multiple Config Server instances
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