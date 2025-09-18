# SignedTokenData vs TokenInfo Comparison

## Overview
Both classes serve similar purposes but have different designs and use cases in the token validation system.

## Key Differences

| Aspect | SignedTokenData | TokenInfo |
|--------|----------------|-----------|
| **Purpose** | Legacy signature-based token storage | Modern JSON-based token storage |
| **Token Field** | ✅ Has `token` field | ❌ No `token` field (token is Redis key) |
| **Source Field** | ❌ No source tracking | ✅ Has `source` field ("gateway", "sso", etc.) |
| **JSON Serialization** | ❌ No JSON annotations | ✅ Has `@JsonFormat` annotations |
| **Signature Format** | Complex: `token\|username\|permissions\|timestamps\|secret` | Simple: `username\|permissions\|token` |
| **Constructor Parameters** | `(token, username, permissions)` | `(username, permissions, token)` |
| **Redis Usage** | Token as separate field in object | Token as Redis key, object as value |
| **Architecture** | Self-contained with token | Separated: token=key, data=value |

## Field Comparison

### SignedTokenData Fields
```java
private String token;           // JWT token stored in object
private String username;
private List<String> permissions;
private LocalDateTime validatedAt;
private LocalDateTime expiresAt;
private String signature;
```

### TokenInfo Fields
```java
private String username;
private List<String> permissions;
private LocalDateTime validatedAt;
private LocalDateTime expiresAt;
private String source;          // NEW: tracks where token came from
private String signature;
// Note: No token field - token is the Redis key
```

## Signature Generation Differences

### SignedTokenData (Complex)
```java
String dataToSign = token + "|" + 
                  username + "|" + 
                  sortedPermissions + "|" + 
                  validatedAt + "|" + 
                  expiresAt + "|" +
                  "SERVICE1_SECRET_KEY_2024";
```

### TokenInfo (Simple)
```java
String dataToSign = String.format("%s|%s|%s",
    username,
    sortedPermissions,
    token
);
```

## Usage Patterns

### SignedTokenData (Legacy)
```java
// Token stored as field in object
SignedTokenData signedData = new SignedTokenData(token, username, permissions);
// Complex signature with timestamps and secret key
```

### TokenInfo (Modern)
```java
// Token used as Redis key, data as value
TokenInfo tokenInfo = new TokenInfo(username, permissions, token);
redisTemplate.opsForValue().set(token, jsonData, ttl);
// Simple signature with just essential data
```

## Redis Storage Comparison

### SignedTokenData Approach
```
Key: "token:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
Value: {
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "john.doe",
  "permissions": ["SERVICE1_ALL_ACCESS"],
  "signature": "abc123..."
}
```

### TokenInfo Approach
```
Key: "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
Value: {
  "username": "john.doe",
  "permissions": ["SERVICE1_ALL_ACCESS"],
  "source": "gateway",
  "signature": "abc123..."
}
```

## Advantages

### SignedTokenData
- ✅ Self-contained (token included in object)
- ✅ Complex signature for maximum security
- ✅ All data in one place

### TokenInfo
- ✅ Cleaner Redis structure (token as key)
- ✅ JSON serialization support
- ✅ Source tracking for audit
- ✅ Simpler signature format
- ✅ Better separation of concerns
- ✅ More efficient storage

## Migration Path

### From SignedTokenData to TokenInfo
1. **Remove token field** from object (use as Redis key)
2. **Add source field** for tracking
3. **Simplify signature** to `username|permissions|token`
4. **Add JSON annotations** for serialization
5. **Update constructors** to match new pattern

### Code Migration Example
```java
// OLD (SignedTokenData)
SignedTokenData signedData = new SignedTokenData(token, username, permissions);
String cacheKey = "token:" + token;
redisTemplate.opsForValue().set(cacheKey, signedData);

// NEW (TokenInfo)
TokenInfo tokenInfo = new TokenInfo(username, permissions, token);
redisTemplate.opsForValue().set(token, objectMapper.writeValueAsString(tokenInfo));
```

## Recommendation

**Use TokenInfo** for new implementations because:
- ✅ Better Redis design (token as key)
- ✅ Cleaner JSON serialization
- ✅ Source tracking for audit trails
- ✅ Simpler signature verification
- ✅ More maintainable code
- ✅ Better performance

**SignedTokenData** should be considered legacy and gradually replaced with TokenInfo.
