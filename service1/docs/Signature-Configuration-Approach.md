# Signature Configuration Approach

## Problem
All services need to know the signature format to generate and verify signatures consistently. The format `username|permissions|token` must be shared across:
- Gateway (generates signatures)
- Service1 (verifies signatures)
- SSO (generates signatures)
- ACL (generates signatures)

## Solution: Config-Server Based Signature Configuration

### 1. **Centralized Configuration**
All signature configuration is stored in `config-server/src/main/resources/config/signature.properties`:

```properties
# Signature Format Configuration
signature.format=username|permissions|token
signature.separator=|
signature.fields=username,permissions,token

# Signature Algorithm Configuration
signature.algorithm=SHA-256
signature.encoding=UTF-8

# Signature Validation Configuration
signature.validation.enabled=true
signature.validation.strict=true
```

### 2. **SignatureConfig Class**
Each service has a `SignatureConfig` class that reads from config-server:

```java
@ConfigurationProperties(prefix = "signature")
public class SignatureConfig {
    private String format = "username|permissions|token";
    private String separator = "|";
    private List<String> fields = List.of("username", "permissions", "token");
    private String algorithm = "SHA-256";
    // ... other configuration
}
```

### 3. **SignatureService Class**
Each service has a `SignatureService` that uses the configuration:

```java
@Service
public class SignatureService {
    private final SignatureConfig signatureConfig;
    
    public String generateSignature(String username, List<String> permissions, String token) {
        String dataToSign = buildSignatureData(username, permissions, token);
        // Uses configured algorithm and format
    }
    
    public boolean verifySignature(String username, List<String> permissions, String token, String expectedSignature) {
        // Uses same configuration for verification
    }
}
```

## How It Works

### **Gateway (Token Generation)**
```java
// Gateway uses SignatureService to generate signatures
SignatureService signatureService = new SignatureService(signatureConfig);
String signature = signatureService.generateSignature(username, permissions, token);

// Create TokenInfo with pre-generated signature
TokenInfo tokenInfo = new TokenInfo(username, permissions, "gateway", token, signature);
```

### **Service1 (Token Verification)**
```java
// Service1 uses SignatureService to verify signatures
SignatureService signatureService = new SignatureService(signatureConfig);
boolean isValid = signatureService.verifySignature(username, permissions, token, expectedSignature);
```

### **SSO/ACL (Token Generation)**
```java
// SSO/ACL services use same SignatureService
SignatureService signatureService = new SignatureService(signatureConfig);
String signature = signatureService.generateSignature(username, permissions, token);
```

## Benefits

### **1. Centralized Configuration**
- ✅ Single source of truth in config-server
- ✅ Easy to change signature format across all services
- ✅ Consistent signature generation/verification

### **2. Service Independence**
- ✅ Each service has its own SignatureService
- ✅ No direct dependencies between services
- ✅ Services can be deployed independently

### **3. Flexibility**
- ✅ Can change signature format without code changes
- ✅ Can enable/disable signature validation
- ✅ Can add debug logging
- ✅ Can use different algorithms

### **4. Security**
- ✅ All services use same signature format
- ✅ No hardcoded signature logic
- ✅ Easy to rotate signature algorithms

## Configuration Examples

### **Change Signature Format**
```properties
# In config-server/signature.properties
signature.format=token|username|permissions
signature.separator=|
signature.fields=token,username,permissions
```

### **Change Algorithm**
```properties
# In config-server/signature.properties
signature.algorithm=SHA-512
```

### **Enable Debug Logging**
```properties
# In config-server/signature.properties
signature.debug.enabled=true
signature.debug.log-format=true
```

### **Disable Signature Validation**
```properties
# In config-server/signature.properties
signature.validation.enabled=false
```

## Implementation Steps

### **1. Add to Config-Server**
- Create `signature.properties` in config-server
- Add signature configuration properties

### **2. Add to Each Service**
- Create `SignatureConfig` class
- Create `SignatureService` class
- Update token generation/verification to use SignatureService

### **3. Update TokenInfo**
- Add constructor that accepts pre-generated signature
- Remove internal signature generation (use SignatureService)

### **4. Test Configuration**
- Verify all services use same signature format
- Test signature generation/verification
- Test configuration changes

## Alternative Approaches

### **1. Hardcoded Format (Current)**
```java
// Hardcoded in each service
String dataToSign = username + "|" + permissions + "|" + token;
```
❌ **Problems**: Inconsistent, hard to change, error-prone

### **2. Shared Library**
```java
// Shared JAR with signature logic
SignatureUtils.generateSignature(username, permissions, token);
```
❌ **Problems**: Version conflicts, deployment complexity

### **3. Database Configuration**
```sql
-- Store signature config in database
SELECT format FROM signature_config WHERE service = 'gateway';
```
❌ **Problems**: Database dependency, performance overhead

### **4. Config-Server (Recommended)**
```java
// Configuration-driven approach
@ConfigurationProperties(prefix = "signature")
public class SignatureConfig { ... }
```
✅ **Benefits**: Centralized, flexible, no dependencies, easy to change

## Conclusion

The **Config-Server approach** is the best solution because it:
- ✅ **Centralizes** signature configuration
- ✅ **Ensures consistency** across all services
- ✅ **Enables easy changes** without code modifications
- ✅ **Maintains service independence**
- ✅ **Provides flexibility** for future requirements

All services will use the same signature format: `username|permissions|token` as configured in the config-server!
