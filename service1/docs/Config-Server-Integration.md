# Service1 Config-Server Integration

## Overview
Service1 reads signature configuration from the config-server to ensure consistent signature generation and verification across all services.

## How Service1 Reads from Config-Server

### 1. **Bootstrap Configuration**
Service1 is configured to read from config-server via `bootstrap.properties`:

```properties
# Bootstrap Configuration for Config Server
spring.application.name=service1
spring.cloud.config.uri=http://localhost:8888
spring.cloud.config.enabled=true
spring.cloud.config.fail-fast=true
spring.cloud.config.retry.initial-interval=1000
spring.cloud.config.retry.max-interval=2000
spring.cloud.config.retry.max-attempts=6
spring.cloud.config.retry.multiplier=1.1
```

### 2. **Config-Server Configuration**
The signature configuration is stored in `config-server/src/main/resources/config/service1.properties`:

```properties
# Signature Configuration
signature.format=username|permissions|token
signature.separator=|
signature.fields=username,permissions,token
signature.algorithm=SHA-256
signature.encoding=UTF-8
signature.validation.enabled=true
signature.validation.strict=true
signature.debug.enabled=false
signature.debug.log-format=true
```

### 3. **Service1 Configuration Classes**

#### **SignatureConfig Class**
```java
@ConfigurationProperties(prefix = "signature")
@Component
public class SignatureConfig {
    private String format = "username|permissions|token";
    private String separator = "|";
    private List<String> fields = List.of("username", "permissions", "token");
    private String algorithm = "SHA-256";
    private String encoding = "UTF-8";
    // ... other configuration
}
```

#### **SignatureService Class**
```java
@Service
public class SignatureService {
    private final SignatureConfig signatureConfig;
    
    public String generateSignature(String username, List<String> permissions, String token) {
        // Uses configuration from config-server
        String dataToSign = buildSignatureData(username, permissions, token);
        // ... signature generation logic
    }
}
```

## Configuration Flow

### **1. Service1 Startup**
1. Service1 starts and reads `bootstrap.properties`
2. Connects to config-server at `http://localhost:8888`
3. Requests configuration for `spring.application.name=service1`
4. Config-server serves `service1.properties` with signature configuration

### **2. Configuration Loading**
1. Spring Boot loads `SignatureConfig` class
2. `@ConfigurationProperties(prefix = "signature")` binds properties
3. Configuration is available throughout the application

### **3. Signature Generation/Verification**
1. `SignatureService` uses `SignatureConfig` for format
2. Generates signatures using `username|permissions|token` format
3. Verifies signatures using same format

## Testing Configuration

### **Test Endpoints**
Service1 provides test endpoints to verify configuration:

#### **Get Signature Configuration**
```bash
GET http://localhost:8082/service1/signature/config
```

Response:
```json
{
  "success": true,
  "format": "username|permissions|token",
  "separator": "|",
  "fields": ["username", "permissions", "token"],
  "algorithm": "SHA-256",
  "encoding": "UTF-8",
  "validationEnabled": true,
  "debugEnabled": false
}
```

#### **Test Signature Generation**
```bash
GET http://localhost:8082/service1/signature/test
```

Response:
```json
{
  "success": true,
  "username": "testuser",
  "permissions": ["SERVICE1_ALL_ACCESS", "SERVICE1_HELLO_ACCESS"],
  "token": "test-token-123",
  "signature": "a1b2c3d4e5f6...",
  "isValid": true,
  "format": "username|permissions|token"
}
```

#### **Get Signature Format**
```bash
GET http://localhost:8082/service1/signature/format
```

Response:
```json
{
  "success": true,
  "format": "username|permissions|token",
  "separator": "|",
  "fields": ["username", "permissions", "token"],
  "example": "username|permissions|token"
}
```

## Configuration Changes

### **Change Signature Format**
To change the signature format across all services:

1. **Update Config-Server**
```properties
# In config-server/src/main/resources/config/service1.properties
signature.format=token|username|permissions
signature.fields=token,username,permissions
```

2. **Restart Service1**
```bash
# Service1 will automatically pick up new configuration
curl http://localhost:8082/service1/signature/config
```

3. **Verify Change**
```json
{
  "format": "token|username|permissions",
  "fields": ["token", "username", "permissions"]
}
```

### **Enable Debug Logging**
```properties
# In config-server/src/main/resources/config/service1.properties
signature.debug.enabled=true
signature.debug.log-format=true
```

### **Change Algorithm**
```properties
# In config-server/src/main/resources/config/service1.properties
signature.algorithm=SHA-512
```

## Benefits

### **1. Centralized Configuration**
- ✅ Single source of truth in config-server
- ✅ Easy to change signature format
- ✅ Consistent across all services

### **2. Dynamic Updates**
- ✅ Change configuration without code changes
- ✅ Restart services to pick up new configuration
- ✅ No deployment required for configuration changes

### **3. Service Independence**
- ✅ Each service reads its own configuration
- ✅ Services can have different configurations if needed
- ✅ No direct dependencies between services

### **4. Testing and Debugging**
- ✅ Test endpoints to verify configuration
- ✅ Debug logging for signature generation
- ✅ Easy to troubleshoot signature issues

## Troubleshooting

### **Configuration Not Loading**
1. Check config-server is running on port 8888
2. Verify `bootstrap.properties` configuration
3. Check logs for connection errors

### **Signature Mismatch**
1. Verify all services use same configuration
2. Check signature format in config-server
3. Use test endpoints to verify signature generation

### **Connection Issues**
1. Check network connectivity to config-server
2. Verify config-server is accessible
3. Check firewall settings

## Conclusion

Service1 successfully reads signature configuration from config-server, ensuring:
- ✅ **Consistent signature format** across all services
- ✅ **Centralized configuration management**
- ✅ **Easy configuration changes** without code modifications
- ✅ **Proper testing and debugging** capabilities

The signature format `username|permissions|token` is now centrally managed and consistently used across all services!
