# Redis Token Storage Design

## New Approach: Token as Key, JSON Data as Value

### Overview
Instead of using complex signature-based validation, we now use a simpler approach where:
- **Token** (JWT) acts as the **Redis key**
- **Token information** is stored as **JSON** in the Redis value

### Redis Structure

#### Key Format
```
{token}  // The actual JWT token string
```

#### Value Format (JSON)
```json
{
  "username": "john.doe",
  "permissions": [
    "SERVICE1_ALL_ACCESS",
    "SERVICE1_HELLO_ACCESS", 
    "SERVICE1_ADMIN_ACCESS"
  ],
  "validatedAt": "2024-01-15T10:30:00",
  "expiresAt": "2024-01-15T11:00:00",
  "source": "gateway"
}
```

### Benefits

1. **Simplified Architecture**: No complex signature verification needed
2. **Better Performance**: Direct token lookup in Redis
3. **Easier Debugging**: JSON data is human-readable
4. **Flexible**: Easy to add new fields to TokenInfo
5. **TTL Support**: Automatic expiration using Redis TTL

### How It Works

#### 1. Gateway Stores Token Info
When a user authenticates through the Gateway:
```java
TokenInfo tokenInfo = new TokenInfo(username, permissions, "gateway");
tokenInfoService.storeTokenInfo(jwtToken, tokenInfo, 30); // 30 minutes TTL
```

#### 2. Service1 Validates Token
When Service1 receives a request:
```java
TokenInfo tokenInfo = tokenInfoService.getTokenInfo(token);
if (tokenInfo != null && !tokenInfo.isExpired()) {
    // Token is valid, check permissions
    if (tokenInfo.hasPermission("SERVICE1_HELLO_ACCESS")) {
        // Allow access
    }
}
```

#### 3. Cache Miss Handling
If token is not in Redis (cache miss):
1. Validate JWT with SSO service
2. Get user permissions from ACL service
3. Store in Redis for future requests
4. Allow/deny based on permissions

### Redis Operations

#### Store Token
```bash
SET "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..." '{"username":"john.doe","permissions":["SERVICE1_ALL_ACCESS"],"validatedAt":"2024-01-15T10:30:00","expiresAt":"2024-01-15T11:00:00","source":"gateway"}' EX 1800
```

#### Retrieve Token
```bash
GET "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### Check TTL
```bash
TTL "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Security Considerations

1. **TTL**: Tokens automatically expire
2. **JWT Validation**: Still validate JWT signature when cache miss occurs
3. **Permission Caching**: Only cache after successful validation
4. **Cleanup**: Expired tokens are automatically removed

### Migration from Old System

The old signature-based system is replaced with this simpler JSON-based approach:
- Remove `SignedTokenData` class
- Remove signature verification logic
- Use `TokenInfo` class for all token data
- Use `TokenInfoService` for Redis operations

### Example Usage

```java
// Store token info
TokenInfo tokenInfo = new TokenInfo("john.doe", Arrays.asList("SERVICE1_ALL_ACCESS"));
tokenInfoService.storeTokenInfo(jwtToken, tokenInfo);

// Check if token is valid
boolean isValid = tokenInfoService.isTokenValid(jwtToken);

// Check permissions
boolean hasPermission = tokenInfoService.hasPermission(jwtToken, "SERVICE1_HELLO_ACCESS");

// Get username
String username = tokenInfoService.getUsername(jwtToken);
```
