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
  "source": "gateway",
  "signature": "a1b2c3d4e5f6..."
}
```

### Benefits

1. **Simplified Architecture**: Clean JSON-based storage with signature verification
2. **Better Performance**: Direct token lookup in Redis
3. **Easier Debugging**: JSON data is human-readable
4. **Flexible**: Easy to add new fields to TokenInfo
5. **TTL Support**: Automatic expiration using Redis TTL
6. **Integrity Verification**: SHA-256 signature ensures data hasn't been tampered with

### How It Works

#### 1. Gateway Stores Token Info
When a user authenticates through the Gateway:
```java
TokenInfo tokenInfo = new TokenInfo(username, permissions, jwtToken, "gateway");
// Gateway stores in Redis with TTL
redisTemplate.opsForValue().set(jwtToken, jsonData, 30, TimeUnit.MINUTES);
```

#### 2. Service1 Validates Token (Read-Only)
When Service1 receives a request:
```java
TokenInfo tokenInfo = tokenInfoService.getTokenInfo(token);
// TokenInfoService automatically:
// 1. Verifies signature using username|permissions|token format
// 2. Checks expiration
// 3. Deletes expired/invalid tokens
if (tokenInfo != null) {
    // Token is valid and signature verified, check permissions
    if (tokenInfo.hasPermission("SERVICE1_HELLO_ACCESS")) {
        // Allow access
    }
}
```

#### 3. Cache Miss Handling
If token is not in Redis (cache miss):
- Service1 returns 401 Unauthorized
- User must re-authenticate through Gateway
- Gateway will store new token info in Redis

### Redis Operations

#### Store Token
```bash
SET "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..." '{"username":"john.doe","permissions":["SERVICE1_ALL_ACCESS"],"validatedAt":"2024-01-15T10:30:00","expiresAt":"2024-01-15T11:00:00","source":"gateway","signature":"a1b2c3d4e5f6..."}' EX 1800
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
2. **JWT Validation**: Gateway validates JWT before storing in Redis
3. **Read-Only Service1**: Service1 only reads and deletes from Redis
4. **Cleanup**: Expired tokens are automatically removed by Service1
5. **Signature Verification**: SHA-256 signature using `username|permissions|token` format
6. **Tamper Detection**: Invalid signatures cause immediate token removal
7. **Separation of Concerns**: Gateway handles storage, Service1 handles validation

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
