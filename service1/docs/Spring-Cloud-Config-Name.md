# Spring Cloud Config Name Configuration

## Overview
This document explains how to use `spring.cloud.config.name` to specify which configuration files to load from the config-server.

## Configuration Properties

### spring.cloud.config.name
**Purpose**: Specifies which configuration files to load from config-server
**Format**: Comma-separated list of configuration names
**Default**: Uses `spring.application.name`

### Example Configuration
```properties
# Spring Cloud Config Configuration
spring.cloud.config.name=service1,shared
spring.cloud.config.profile=default
spring.cloud.config.label=master
spring.cloud.config.fail-fast=true
```

## How It Works

### 1. Configuration Loading Order
```
1. service1.properties (service-specific configuration)
2. shared.properties (shared configuration)
3. service1-default.properties (if exists)
4. shared-default.properties (if exists)
```

### 2. Config-Server File Structure
```
config-server/src/main/resources/config/
├── service1.properties          # Service1 specific config
├── shared.properties            # Shared config for all services
├── service2.properties          # Service2 specific config
├── service3.properties          # Service3 specific config
└── ...
```

### 3. Configuration Resolution
When `spring.cloud.config.name=service1,shared` is specified:

1. **service1.properties** is loaded first
2. **shared.properties** is loaded second
3. Properties from **shared.properties** can override **service1.properties**
4. Final configuration is merged from both files

## Benefits of Multiple Config Names

### ✅ Shared Configuration
- **Common Settings**: All services can use shared configuration
- **No Duplication**: Avoid duplicating common properties
- **Easy Updates**: Update shared properties in one place

### ✅ Service-Specific Configuration
- **Custom Settings**: Each service can have its own configuration
- **Override Capability**: Service config can override shared config
- **Flexibility**: Mix and match configuration files

### ✅ Environment Management
- **Different Environments**: Different configs for dev, test, prod
- **Profile Support**: Use profiles for environment-specific configs
- **Label Support**: Use labels for version-specific configs

## Configuration Examples

### service1.properties (Service-Specific)
```properties
# Service1 Configuration
server.port=8082
spring.application.name=service1
server.servlet.context-path=/service1

# Database Configuration (Service1 specific)
spring.datasource.url=jdbc:postgresql://localhost:5432/service1_db
spring.datasource.username=service1
spring.datasource.password=service1

# Service1 specific endpoints
service1.endpoints.hello=/service1/hello
service1.endpoints.admin=/service1/admin
```

### shared.properties (Shared Configuration)
```properties
# Shared Public Key (used by all services)
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
shared.signature.algorithm=SHA-256
shared.signature.encoding=UTF-8

# Shared Redis Configuration
shared.redis.host=localhost
shared.redis.port=6379
shared.redis.database=0
shared.redis.timeout=2000ms

# Shared Logging Configuration
shared.logging.level.root=INFO
shared.logging.level.com.eureka=DEBUG
```

## Configuration Loading Process

### 1. Service Startup
```
1. Service1 starts
2. Reads application.properties
3. spring.config.import connects to config-server
4. spring.cloud.config.name=service1,shared specifies which files to load
5. Config-server serves both service1.properties and shared.properties
6. Properties are merged (shared can override service1)
7. @ConfigurationProperties binds the merged configuration
```

### 2. Property Resolution Order
```
1. service1.properties (loaded first)
2. shared.properties (loaded second, can override service1)
3. Environment variables
4. Command line arguments
```

### 3. Final Configuration
```properties
# From service1.properties
server.port=8082
spring.application.name=service1
spring.datasource.url=jdbc:postgresql://localhost:5432/service1_db

# From shared.properties
shared.public-key=-----BEGIN PUBLIC KEY-----...
shared.signature.format=username|permissions|token
shared.redis.host=localhost
shared.redis.port=6379
```

## Advanced Configuration

### Multiple Services Example
```properties
# Service1
spring.cloud.config.name=service1,shared

# Service2
spring.cloud.config.name=service2,shared

# Service3
spring.cloud.config.name=service3,shared
```

### Environment-Specific Configuration
```properties
# Development
spring.cloud.config.name=service1,shared
spring.cloud.config.profile=dev

# Production
spring.cloud.config.name=service1,shared
spring.cloud.config.profile=prod
```

### Version-Specific Configuration
```properties
# Specific version
spring.cloud.config.name=service1,shared
spring.cloud.config.label=v1.0.0

# Latest version
spring.cloud.config.name=service1,shared
spring.cloud.config.label=master
```

## Testing Configuration

### 1. Test Configuration Loading
```bash
GET http://localhost:8082/service1/signature/shared-config
```

### Expected Response
```json
{
  "success": true,
  "message": "Shared configuration loaded using @ConfigurationProperties",
  "configurationMethod": "@ConfigurationProperties",
  "publicKey": {
    "available": true,
    "jwtAvailable": true
  },
  "signature": {
    "format": "username|permissions|token",
    "separator": "|",
    "algorithm": "SHA-256",
    "encoding": "UTF-8"
  },
  "redis": {
    "host": "localhost",
    "port": 6379,
    "database": 0,
    "timeout": "2000ms"
  },
  "note": "Configuration loaded from service1.properties and shared.properties"
}
```

### 2. Test Public Key Loading
```bash
GET http://localhost:8082/service1/signature/public-key
```

### Expected Response
```json
{
  "success": true,
  "message": "Public key loaded from shared configuration using @ConfigurationProperties",
  "source": "shared.properties in config-server",
  "configurationMethod": "@ConfigurationProperties",
  "configFiles": ["service1.properties", "shared.properties"],
  "publicKeyAvailable": true,
  "note": "Public key loaded from shared.properties via spring.cloud.config.name=service1,shared"
}
```

## Best Practices

### ✅ Configuration Organization
- **Service-Specific**: Put service-specific config in service1.properties
- **Shared Config**: Put common config in shared.properties
- **Clear Separation**: Keep service and shared configs separate
- **Documentation**: Document which configs are shared vs service-specific

### ✅ Naming Convention
- **Service Config**: Use service name (service1.properties, service2.properties)
- **Shared Config**: Use descriptive name (shared.properties, common.properties)
- **Environment Config**: Use environment suffix (service1-dev.properties, service1-prod.properties)

### ✅ Property Override
- **Service Override**: Service config can override shared config
- **Environment Override**: Environment config can override service config
- **Clear Hierarchy**: Document the override hierarchy

## Conclusion

Using `spring.cloud.config.name=service1,shared` provides:
- ✅ **Shared Configuration**: Common config shared across services
- ✅ **Service-Specific Config**: Custom config for each service
- ✅ **Override Capability**: Service config can override shared config
- ✅ **Flexibility**: Mix and match configuration files
- ✅ **Maintainability**: Easy to maintain and update configuration

This approach is **perfect** for microservices architecture where you need both shared and service-specific configuration!
