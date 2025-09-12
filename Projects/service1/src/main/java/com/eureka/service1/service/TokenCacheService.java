package com.eureka.service1.service;

import com.eureka.service1.model.SignedTokenData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final SignatureVerificationService signatureVerificationService;
    
    @Value("${token.cache.prefix:token:}")
    private String cachePrefix;
    
    @Value("${token.cache.ttl.minutes:30}")
    private int cacheTtlMinutes;
    
    public void cacheToken(String token, String username, List<String> permissions) {
        try {
            String cacheKey = cachePrefix + token;
            SignedTokenData signedTokenData = new SignedTokenData(token, username, permissions);
            
            redisTemplate.opsForValue().set(cacheKey, signedTokenData, Duration.ofMinutes(cacheTtlMinutes));
            log.debug("Signed token cached successfully for user: {} with {} permissions", username, permissions.size());
            
        } catch (Exception e) {
            log.error("Error caching signed token for user {}: {}", username, e.getMessage());
        }
    }
    
    public SignedTokenData getCachedToken(String token) {
        try {
            String cacheKey = cachePrefix + token;
            Object cachedData = redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedData instanceof SignedTokenData) {
                SignedTokenData signedTokenData = (SignedTokenData) cachedData;
                
                // Verify signature using comprehensive verification
                if (!signatureVerificationService.verifySignatureComprehensive(signedTokenData)) {
                    log.warn("Invalid signature detected for token: {} - possible tampering", token);
                    invalidateToken(token);
                    return null;
                }
                
                // Check expiration
                if (signedTokenData.isExpired()) {
                    log.debug("Cached signed token expired for user: {}", signedTokenData.getUsername());
                    invalidateToken(token);
                    return null;
                }
                
                log.debug("Cache hit for signed token: {} with valid signature", token);
                return signedTokenData;
            }
            
            log.debug("Cache miss for token: {}", token);
            return null;
            
        } catch (Exception e) {
            log.error("Error retrieving cached signed token: {}", e.getMessage());
            return null;
        }
    }
    
    public void invalidateToken(String token) {
        try {
            String cacheKey = cachePrefix + token;
            redisTemplate.delete(cacheKey);
            log.debug("Signed token invalidated: {}", token);
            
        } catch (Exception e) {
            log.error("Error invalidating signed token {}: {}", token, e.getMessage());
        }
    }
    
    public boolean hasCachedToken(String token) {
        try {
            String cacheKey = cachePrefix + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
            
        } catch (Exception e) {
            log.error("Error checking cached signed token: {}", e.getMessage());
            return false;
        }
    }
    
    public void clearAllTokens() {
        try {
            Set<String> keys = redisTemplate.keys(cachePrefix + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Cleared {} cached signed tokens", keys.size());
            }
            
        } catch (Exception e) {
            log.error("Error clearing cached signed tokens: {}", e.getMessage());
        }
    }
    
    // Method to verify token integrity with detailed logging
    public boolean verifyTokenIntegrity(String token) {
        try {
            SignedTokenData signedTokenData = getCachedToken(token);
            if (signedTokenData != null) {
                boolean isValid = !signedTokenData.isExpired();
                log.debug("Token integrity check for {}: {}", token, isValid ? "VALID" : "INVALID");
                return isValid;
            }
            return false;
        } catch (Exception e) {
            log.error("Error verifying token integrity for {}: {}", token, e.getMessage());
            return false;
        }
    }
    
    // Method to get token data with integrity verification
    public SignedTokenData getVerifiedTokenData(String token) {
        SignedTokenData signedTokenData = getCachedToken(token);
        if (signedTokenData != null && !signedTokenData.isExpired()) {
            return signedTokenData;
        }
        return null;
    }
    
    /**
     * بررسی دسترسی کلید عمومی Gateway
     */
    public boolean isGatewayPublicKeyAvailable() {
        return signatureVerificationService.isGatewayPublicKeyAvailable();
    }

    /**
     * به‌روزرسانی کلید عمومی Gateway
     */
    public void refreshGatewayPublicKey() {
        signatureVerificationService.refreshGatewayPublicKey();
    }
} 