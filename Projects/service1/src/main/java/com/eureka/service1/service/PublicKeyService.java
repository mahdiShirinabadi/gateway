package com.eureka.service1.service;

import com.eureka.service1.config.PublicKeyConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicKeyService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final PublicKeyConfig publicKeyConfig;
    
    private static final String PUBLIC_KEY_CACHE_PREFIX = "public_key:";
    private static final Duration PUBLIC_KEY_CACHE_TTL = Duration.ofHours(24);

    public PublicKey getGatewayPublicKey() {
        try {
            // First, check Redis cache
            PublicKey cachedKey = getCachedPublicKey("gateway");
            if (cachedKey != null) {
                log.info("Service1: Gateway public key retrieved from Redis cache");
                return cachedKey;
            }

            // Get from Config Server
            log.info("Service1: Getting Gateway public key from Config Server");
            String publicKeyString = publicKeyConfig.getGateway().getPublicKey();
            if (publicKeyString != null && !publicKeyString.isEmpty()) {
                PublicKey publicKey = decodePublicKey(publicKeyString);
                if (publicKey != null) {
                    cachePublicKey("gateway", publicKey);
                    log.info("Service1: Gateway public key cached in Redis");
                }
                return publicKey;
            } else {
                log.error("Service1: Gateway public key not available in Config Server");
            }
            
        } catch (Exception e) {
            log.error("Service1: Error getting Gateway public key: {}", e.getMessage());
        }
        
        return null;
    }

    public PublicKey getSsoPublicKey() {
        try {
            // First, check Redis cache
            PublicKey cachedKey = getCachedPublicKey("sso");
            if (cachedKey != null) {
                log.info("Service1: SSO public key retrieved from Redis cache");
                return cachedKey;
            }

            // Get from Config Server
            log.info("Service1: Getting SSO public key from Config Server");
            String publicKeyString = publicKeyConfig.getSso().getPublicKey();
            if (publicKeyString != null && !publicKeyString.isEmpty()) {
                PublicKey publicKey = decodePublicKey(publicKeyString);
                if (publicKey != null) {
                    cachePublicKey("sso", publicKey);
                    log.info("Service1: SSO public key cached in Redis");
                }
                return publicKey;
            } else {
                log.error("Service1: SSO public key not available in Config Server");
            }
            
        } catch (Exception e) {
            log.error("Service1: Error getting SSO public key: {}", e.getMessage());
        }
        
        return null;
    }

    public PublicKey getService1PublicKey() {
        try {
            // First, check Redis cache
            PublicKey cachedKey = getCachedPublicKey("service1");
            if (cachedKey != null) {
                log.info("Service1: Service1 public key retrieved from Redis cache");
                return cachedKey;
            }

            // Get from Config Server
            log.info("Service1: Getting Service1 public key from Config Server");
            String publicKeyString = publicKeyConfig.getService1().getPublicKey();
            if (publicKeyString != null && !publicKeyString.isEmpty()) {
                PublicKey publicKey = decodePublicKey(publicKeyString);
                if (publicKey != null) {
                    cachePublicKey("service1", publicKey);
                    log.info("Service1: Service1 public key cached in Redis");
                }
                return publicKey;
            } else {
                log.error("Service1: Service1 public key not available in Config Server");
            }
            
        } catch (Exception e) {
            log.error("Service1: Error getting Service1 public key: {}", e.getMessage());
        }
        
        return null;
    }

    private PublicKey decodePublicKey(String publicKeyString) {
        try {
            // Remove PEM headers if present
            String cleanKey = publicKeyString
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(cleanKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
            
        } catch (Exception e) {
            log.error("Service1: Error decoding public key: {}", e.getMessage());
            return null;
        }
    }

    private PublicKey getCachedPublicKey(String serviceName) {
        try {
            String cacheKey = PUBLIC_KEY_CACHE_PREFIX + serviceName;
            Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedValue != null) {
                String publicKeyString = (String) cachedValue;
                return decodePublicKey(publicKeyString);
            }
            
        } catch (Exception e) {
            log.error("Service1: Error getting cached public key for {}: {}", serviceName, e.getMessage());
        }
        
        return null;
    }

    private void cachePublicKey(String serviceName, PublicKey publicKey) {
        try {
            String cacheKey = PUBLIC_KEY_CACHE_PREFIX + serviceName;
            String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            
            redisTemplate.opsForValue().set(cacheKey, publicKeyString, PUBLIC_KEY_CACHE_TTL);
            log.info("Service1: {} public key cached in Redis with TTL: {}", serviceName, PUBLIC_KEY_CACHE_TTL);
            
        } catch (Exception e) {
            log.error("Service1: Error caching {} public key: {}", serviceName, e.getMessage());
        }
    }

    public void invalidateCachedPublicKey(String serviceName) {
        try {
            String cacheKey = PUBLIC_KEY_CACHE_PREFIX + serviceName;
            redisTemplate.delete(cacheKey);
            log.info("Service1: {} public key removed from Redis cache", serviceName);
            
        } catch (Exception e) {
            log.error("Service1: Error invalidating cached {} public key: {}", serviceName, e.getMessage());
        }
    }

    public boolean hasCachedPublicKey(String serviceName) {
        try {
            String cacheKey = PUBLIC_KEY_CACHE_PREFIX + serviceName;
            return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
            
        } catch (Exception e) {
            log.error("Service1: Error checking cached {} public key: {}", serviceName, e.getMessage());
            return false;
        }
    }

    public PublicKey refreshPublicKey(String serviceName) {
        log.info("Service1: Refreshing {} public key from Config Server", serviceName);
        invalidateCachedPublicKey(serviceName);
        
        switch (serviceName.toLowerCase()) {
            case "gateway":
                return getGatewayPublicKey();
            case "sso":
                return getSsoPublicKey();
            case "service1":
                return getService1PublicKey();
            default:
                log.error("Service1: Unknown service name for public key refresh: {}", serviceName);
                return null;
        }
    }
} 