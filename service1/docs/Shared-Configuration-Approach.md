# Shared Configuration Approach

## Overview
This document explains the centralized configuration approach for managing public keys and shared settings across multiple services in a microservices architecture.

## Problem Solved
- **Before**: Each service had its own duplicate public key configuration
- **After**: Single shared public key configuration that all services inherit

## Architecture

### 1. Shared Configuration File
**Location**: `config-server/src/main/resources/config/shared.properties`

```properties
# Shared Public Key Configuration
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
shared.signature.separator=|
shared.signature.fields=username,permissions,token
shared.signature.algorithm=SHA-256
shared.signature.encoding=UTF-8
shared.signature.validation.enabled=true
shared.signature.validation.strict=true
shared.signature.debug.enabled=false
shared.signature.debug.log-format=false
```

### 2. Service-Specific Configuration
**Example**: `config-server/src/main/resources/config/service1.properties`

```properties
# Service1 Configuration
server.port=8082
spring.application.name=service1
server.servlet.context-path=/service1

# Service1 inherits from shared.properties:
# - shared.public-key (for RSA signature verification)
# - shared.signature.* (signature configuration)
# - shared.redis.* (Redis configuration)
# - shared.logging.* (logging configuration)

# Service1 specific configuration only
service1.name=Service1
service1.description=Authentication and Authorization Service
service1.version=1.0.0
```

### 3. Service Implementation
**TokenInfoService.java**:
```java
@Value("${shared.public-key}")
private String publicKeyString;
```

**TokenValidationService.java**:
```java
@Value("${shared.public-key}")
private String publicKeyString;
```

**SignatureConfig.java**:
```java
@ConfigurationProperties(prefix = "shared.signature")
public class SignatureConfig {
    // Uses shared.signature.* properties
}
```

## Benefits

### ✅ Single Source of Truth
- **One Public Key**: All services use the same public key
- **Centralized Management**: Change key in one place
- **Consistency**: All services have identical configuration

### ✅ Easy Key Rotation
- **Update Once**: Change key in `shared.properties`
- **Automatic Propagation**: All services get new key
- **No Code Changes**: No need to update individual services

### ✅ Reduced Duplication
- **No Copy-Paste**: No duplicate configuration across services
- **Maintainable**: Single file to manage shared settings
- **DRY Principle**: Don't Repeat Yourself

### ✅ Scalability
- **Add New Services**: Just reference shared configuration
- **Consistent Setup**: New services automatically get shared settings
- **Easy Onboarding**: New developers understand the pattern

## Configuration Flow

```
1. Service starts → Reads bootstrap.properties
2. Connects to config-server (localhost:8888)
3. Requests configuration for "service1"
4. Config-server serves:
   - service1.properties (service-specific)
   - shared.properties (shared configuration)
5. Service loads both configurations
6. @Value("${shared.public-key}") gets shared key
7. All services use the same public key
```

## Example for Multiple Services

### Service1 (Authentication)
```properties
# service1.properties
server.port=8082
spring.application.name=service1
# Inherits: shared.public-key, shared.signature.*, shared.redis.*
```

### Service2 (Business Logic)
```properties
# service2.properties
server.port=8083
spring.application.name=service2
# Inherits: shared.public-key, shared.signature.*, shared.redis.*
```

### Service3 (Data Processing)
```properties
# service3.properties
server.port=8084
spring.application.name=service3
# Inherits: shared.public-key, shared.signature.*, shared.redis.*
```

## Testing Shared Configuration

### Test Endpoint
```bash
GET http://localhost:8082/service1/signature/public-key
```

### Expected Response
```json
{
  "success": true,
  "message": "Public key loaded from shared configuration",
  "source": "shared.properties in config-server",
  "format": "username|permissions|token",
  "algorithm": "SHA-256",
  "note": "All services now use the same public key from shared configuration",
  "benefits": [
    "Single source of truth for public key",
    "Easy key rotation across all services",
    "No duplication of configuration",
    "Centralized key management"
  ]
}
```

## Best Practices

### ✅ Configuration Organization
- **shared.properties**: Common configuration for all services
- **service1.properties**: Service1-specific configuration only
- **service2.properties**: Service2-specific configuration only
- **Clear Separation**: Shared vs service-specific settings

### ✅ Key Management
- **Centralized**: All keys in shared.properties
- **Version Control**: Track key changes in git
- **Security**: Secure key storage and rotation
- **Documentation**: Document key usage and rotation process

### ✅ Service Development
- **Consistent Pattern**: All services use @Value("${shared.public-key}")
- **No Hardcoding**: No hardcoded keys in service code
- **Easy Testing**: Test with shared configuration
- **Clear Dependencies**: Services depend on config-server

## Migration from Duplicate Configuration

### Before (❌ Bad)
```properties
# service1.properties
signature.public-key=-----BEGIN PUBLIC KEY-----...

# service2.properties  
signature.public-key=-----BEGIN PUBLIC KEY-----...

# service3.properties
signature.public-key=-----BEGIN PUBLIC KEY-----...
```

### After (✅ Good)
```properties
# shared.properties
shared.public-key=-----BEGIN PUBLIC KEY-----...

# service1.properties
# Inherits shared.public-key

# service2.properties
# Inherits shared.public-key

# service3.properties
# Inherits shared.public-key
```

## Conclusion

The shared configuration approach provides:
- ✅ **Single source of truth** for public keys
- ✅ **Easy key rotation** across all services
- ✅ **No duplication** of configuration
- ✅ **Centralized management** of shared settings
- ✅ **Scalable architecture** for multiple services

This approach is especially beneficial when you have **10+ services** that all need the same public key and configuration settings.
