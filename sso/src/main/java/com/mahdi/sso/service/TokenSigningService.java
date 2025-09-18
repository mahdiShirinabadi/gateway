package com.mahdi.sso.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class TokenSigningService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PrivateKey privateKey;

    @Value("${jwt.private-key}")
    private String privateKeyString;

    public TokenSigningService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.privateKey = loadPrivateKey();
    }

    private PrivateKey loadPrivateKey() {
        try {
            // Remove headers and decode
            String privateKeyPEM = privateKeyString
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            log.error("Error loading private key: {}", e.getMessage());
            throw new RuntimeException("Failed to load private key", e);
        }
    }

    public void signAndCacheToken(String token, String userId) {
        try {
            log.info("Signing token for user: {}", userId);
            
            // Create token data: token + userId
            String tokenData = token + ":" + userId;
            
            // Sign with private key
            String signature = signWithPrivateKey(tokenData);
            
            // Cache signed token in Redis
            String cacheKey = "token:" + token;
            redisTemplate.opsForValue().set(cacheKey, signature, 30, TimeUnit.MINUTES);
            
            log.info("Token signed and cached for user: {}", userId);
            
        } catch (Exception e) {
            log.error("Error signing token: {}", e.getMessage());
            throw new RuntimeException("Failed to sign token", e);
        }
    }

    private String signWithPrivateKey(String data) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(data.getBytes());
            byte[] signatureBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            log.error("Error signing data: {}", e.getMessage());
            throw new RuntimeException("Failed to sign data", e);
        }
    }

    public void revokeToken(String token) {
        try {
            String cacheKey = "token:" + token;
            redisTemplate.delete(cacheKey);
            log.info("Token revoked: {}", token);
        } catch (Exception e) {
            log.error("Error revoking token: {}", e.getMessage());
        }
    }
}

