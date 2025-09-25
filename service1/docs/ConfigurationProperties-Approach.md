# @ConfigurationProperties Approach

## Overview
This document explains the modern `@ConfigurationProperties` approach for managing configuration in Spring Boot applications, replacing the old `@Value` annotation approach.

## Problem with @Value Annotation

### ❌ Old Approach (Not Recommended)
```java
@Service
public class TokenInfoService {
    
    @Value("${shared.public-key}")
    private String publicKeyString;
    
    @Value("${shared.signature.format}")
    private String signatureFormat;
    
    @Value("${shared.redis.host}")
    private String redisHost;
    
    // Multiple @Value annotations scattered throughout the code
}
```

### Problems:
- **Scattered Configuration**: Configuration scattered across multiple classes
- **No Type Safety**: String-based configuration without validation
- **No IDE Support**: Limited IDE support for configuration properties
- **Hard to Maintain**: Difficult to track all configuration properties
- **No Default Values**: No easy way to set default values
- **No Validation**: No built-in validation for configuration values

## Solution with @ConfigurationProperties

### ✅ New Approach (Recommended)
```java
@Configuration
@ConfigurationProperties(prefix = "shared")
public class SharedConfigurationProperties {
    
    private String publicKey;
    private String jwtPublicKey;
    private Signature signature = new Signature();
    private Redis redis = new Redis();
    private Logging logging = new Logging();
    
    @Data
    public static class Signature {
        private String format = "username|permissions|token";
        private String separator = "|";
        private List<String> fields = List.of("username", "permissions", "token");
        private String algorithm = "SHA-256";
        private String encoding = "UTF-8";
        private Validation validation = new Validation();
        private Debug debug = new Debug();
        
        @Data
        public static class Validation {
            private boolean enabled = true;
            private boolean strict = true;
        }
        
        @Data
        public static class Debug {
            private boolean enabled = false;
            private boolean logFormat = false;
        }
    }
    
    @Data
    public static class Redis {
        private String host = "localhost";
        private int port = 6379;
        private int database = 0;
        private String timeout = "2000ms";
    }
    
    @Data
    public static class Logging {
        private String levelRoot = "INFO";
        private String levelComEureka = "DEBUG";
        private String patternConsole = "%d{yyyy-MM-dd HH:mm:ss} - %msg%n";
        private String patternFile = "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n";
    }
}
```

## Benefits of @ConfigurationProperties

### ✅ Type Safety
- **Strong Typing**: Configuration properties are strongly typed
- **Compile-time Validation**: IDE can catch type errors at compile time
- **Auto-completion**: Full IDE support with auto-completion
- **Refactoring**: Safe refactoring of configuration properties

### ✅ Centralized Configuration
- **Single Source**: All configuration in one place
- **Easy to Find**: Easy to locate all configuration properties
- **Consistent**: Consistent naming and structure
- **Maintainable**: Easy to maintain and update

### ✅ Default Values
- **Built-in Defaults**: Easy to set default values
- **Fallback Values**: Graceful fallback when properties are missing
- **Environment Specific**: Different defaults for different environments

### ✅ Validation
- **Built-in Validation**: Spring Boot provides validation out of the box
- **Custom Validation**: Easy to add custom validation rules
- **Error Messages**: Clear error messages for invalid configuration

### ✅ IDE Support
- **Auto-completion**: Full IDE support with auto-completion
- **Documentation**: Easy to add documentation for properties
- **Navigation**: Easy navigation between configuration and usage

## Implementation Details

### 1. Configuration Properties Class
```java
@Configuration
@ConfigurationProperties(prefix = "shared")
public class SharedConfigurationProperties {
    
    // Properties with default values
    private String publicKey;
    private Signature signature = new Signature();
    
    // Nested configuration classes
    @Data
    public static class Signature {
        private String format = "username|permissions|token";
        private String algorithm = "SHA-256";
        // ... other properties
    }
}
```

### 2. Service Usage
```java
@Service
public class TokenInfoService {
    
    private final SharedConfigurationProperties sharedConfig;
    
    public TokenInfoService(SharedConfigurationProperties sharedConfig) {
        this.sharedConfig = sharedConfig;
    }
    
    private java.security.PublicKey loadPublicKey() {
        // Get public key from shared configuration
        String publicKeyString = sharedConfig.getPublicKey();
        // ... process public key
    }
}
```

### 3. Controller Usage
```java
@RestController
public class SignatureConfigController {
    
    private final SharedConfigurationProperties sharedConfig;
    
    @GetMapping("/shared-config")
    public ResponseEntity<Map<String, Object>> getSharedConfiguration() {
        // Access configuration properties
        String format = sharedConfig.getSignature().getFormat();
        String algorithm = sharedConfig.getSignature().getAlgorithm();
        // ... return configuration info
    }
}
```

## Configuration Files

### shared.properties (Config-Server)
```properties
# Shared Public Key
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

# Shared Redis Configuration
shared.redis.host=localhost
shared.redis.port=6379
shared.redis.database=0
shared.redis.timeout=2000ms

# Shared Logging Configuration
shared.logging.level.root=INFO
shared.logging.level.com.eureka=DEBUG
shared.logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
shared.logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

### application.properties (Service1)
```properties
# Spring Boot 2.4+ Configuration Import
spring.config.import=optional:configserver:http://localhost:8888
spring.application.name=service1

# Service1 specific configuration
server.port=8082
server.servlet.context-path=/service1

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/service1_db
spring.datasource.username=postgres
spring.datasource.password=password
```

## Testing Configuration

### 1. Test Shared Configuration
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
    "encoding": "UTF-8",
    "validationEnabled": true,
    "validationStrict": true,
    "debugEnabled": false
  },
  "redis": {
    "host": "localhost",
    "port": 6379,
    "database": 0,
    "timeout": "2000ms"
  },
  "logging": {
    "levelRoot": "INFO",
    "levelComEureka": "DEBUG"
  },
  "note": "All configuration is loaded from shared.properties using @ConfigurationProperties"
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
  "format": "username|permissions|token",
  "algorithm": "SHA-256",
  "publicKeyAvailable": true,
  "jwtPublicKeyAvailable": true,
  "note": "All services now use the same public key from shared configuration with @ConfigurationProperties",
  "benefits": [
    "Single source of truth for public key",
    "Easy key rotation across all services",
    "No duplication of configuration",
    "Centralized key management",
    "Type-safe configuration with @ConfigurationProperties",
    "Better IDE support and validation"
  ]
}
```

## Migration from @Value to @ConfigurationProperties

### Before (❌ @Value)
```java
@Service
public class TokenInfoService {
    
    @Value("${shared.public-key}")
    private String publicKeyString;
    
    @Value("${shared.signature.format}")
    private String signatureFormat;
    
    @Value("${shared.redis.host}")
    private String redisHost;
}
```

### After (✅ @ConfigurationProperties)
```java
@Service
public class TokenInfoService {
    
    private final SharedConfigurationProperties sharedConfig;
    
    public TokenInfoService(SharedConfigurationProperties sharedConfig) {
        this.sharedConfig = sharedConfig;
    }
    
    private void someMethod() {
        String publicKey = sharedConfig.getPublicKey();
        String format = sharedConfig.getSignature().getFormat();
        String redisHost = sharedConfig.getRedis().getHost();
    }
}
```

## Best Practices

### ✅ Configuration Organization
- **Single Class**: One configuration class per configuration group
- **Nested Classes**: Use nested classes for related configuration
- **Default Values**: Always provide default values
- **Documentation**: Document configuration properties

### ✅ Type Safety
- **Strong Typing**: Use appropriate types for configuration properties
- **Validation**: Add validation annotations where needed
- **Immutable**: Make configuration classes immutable where possible

### ✅ IDE Support
- **Lombok**: Use Lombok for getters/setters
- **Documentation**: Add JavaDoc for configuration properties
- **Examples**: Provide examples in documentation

## Conclusion

The `@ConfigurationProperties` approach provides:
- ✅ **Type Safety**: Strong typing and compile-time validation
- ✅ **Centralized Configuration**: All configuration in one place
- ✅ **IDE Support**: Full IDE support with auto-completion
- ✅ **Default Values**: Easy to set default values
- ✅ **Validation**: Built-in validation support
- ✅ **Maintainability**: Easy to maintain and update

This approach is **much better** than using `@Value` annotations and is the **recommended way** to handle configuration in Spring Boot applications!
