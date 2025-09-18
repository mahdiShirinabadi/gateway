package com.eureka.service1.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

@Service
@Log4j2
public class TokenValidationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PublicKey publicKey;

    @Value("${jwt.public-key}")
    private String publicKeyString;

    public TokenValidationService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.publicKey = loadPublicKey();
    }

    private PublicKey loadPublicKey() {
        try {
            // Remove headers and decode
            String publicKeyPEM = publicKeyString
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            log.error("Error loading public key: {}", e.getMessage());
            throw new RuntimeException("Failed to load public key", e);
        }
    }

    public boolean validateToken(String token, String userId) {
        try {
            log.info("Validating token in Service1 for user: {}", userId);
            
            // Check Redis for signed token
            String cacheKey = "token:" + token;
            String signature = (String) redisTemplate.opsForValue().get(cacheKey);
            
            if (signature == null) {
                log.warn("Token not found in Redis cache");
                return false;
            }

            // Create token data: token + userId
            String tokenData = token + ":" + userId;
            
            // Verify signature with public key
            boolean isValid = verifySignature(tokenData, signature);
            
            if (isValid) {
                log.info("Token signature verified successfully for user: {}", userId);
                return true;
            } else {
                log.warn("Token signature verification failed for user: {}", userId);
                return false;
            }

        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean verifySignature(String data, String signature) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(data.getBytes());
            byte[] signatureBytes = Base64.getDecoder().decode(signature);
            return sig.verify(signatureBytes);
        } catch (Exception e) {
            log.error("Error verifying signature: {}", e.getMessage());
            return false;
        }
    }

    private String generateTokenHash(String token) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Error generating token hash: {}", e.getMessage());
            return token; // Fallback to original token
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            JwtParser parser = Jwts.parser()
                    .verifyWith(publicKey)
                    .build();
            
            Claims claims = parser.parseSignedClaims(token).getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Error extracting username from token: {}", e.getMessage());
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            JwtParser parser = Jwts.parser()
                    .verifyWith(publicKey)
                    .build();
            
            Claims claims = parser.parseSignedClaims(token).getPayload();

            if (claims == null) {
                return true; // Invalid token is considered expired
            }

            // Check JWT expiration
            return claims.getExpiration() != null && 
                   claims.getExpiration().before(new java.util.Date());

        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true; // If we can't parse, consider it expired
        }
    }

    public boolean hasPermission(String token, String requiredPermission) {
        try {
            // First check if token is expired
            if (isTokenExpired(token)) {
                log.warn("Token expired for permission check");
                return false;
            }

            // First validate token with JWT
            if (!validateToken(token, getUsernameFromToken(token))) {
                log.warn("Invalid token for permission check");
                return false;
            }

            String tokenHash = generateTokenHash(token);
            String cacheKey = "permissions:" + tokenHash;
            @SuppressWarnings("unchecked")
            List<String> permissions = (List<String>) redisTemplate.opsForValue().get(cacheKey);

            boolean hasPermission = permissions.contains(requiredPermission);
            log.info("Permission check from cache: {} for permission: {}", hasPermission, requiredPermission);
            return hasPermission;

            // If not in cache, return false (should be validated by Gateway)
        } catch (Exception e) {
            log.error("Error checking permissions: {}", e.getMessage());
            return false;
        }
    }
}
