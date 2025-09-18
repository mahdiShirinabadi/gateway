package com.eureka.gateway.service;

import com.eureka.gateway.model.TokenData;
import com.eureka.gateway.util.GatewayKeyGenerator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class TokenCacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final GatewayKeyGenerator gatewayKeyGenerator;
    
    private static final String TOKEN_CACHE_PREFIX = "gateway_token:";
    private static final Duration TOKEN_CACHE_TTL = Duration.ofMinutes(30);

    public TokenCacheService(RedisTemplate<String, Object> redisTemplate, GatewayKeyGenerator gatewayKeyGenerator) {
        this.redisTemplate = redisTemplate;
        this.gatewayKeyGenerator = gatewayKeyGenerator;
    }

    public void cacheToken(String token, String username, List<String> permissions) {
        try {
            String cacheKey = TOKEN_CACHE_PREFIX + token;
            TokenData tokenData = new TokenData(token, username, permissions);
            
            // Verify signature with Gateway's private key
            if (gatewayKeyGenerator.areKeysAvailable() && tokenData.isSignatureValid(gatewayKeyGenerator.getPrivateKey())) {
                redisTemplate.opsForValue().set(cacheKey, tokenData, TOKEN_CACHE_TTL);
                System.out.println("Gateway: توکن با امضای Gateway در کش ذخیره شد برای کاربر: " + username + " با " + permissions.size() + " مجوز");
            } else {
                System.err.println("Gateway: خطا در تأیید امضای توکن برای کاربر: " + username);
            }
            
        } catch (Exception e) {
            System.err.println("Gateway: خطا در کش کردن توکن: " + e.getMessage());
        }
    }

    public TokenData getCachedToken(String token) {
        try {
            String cacheKey = TOKEN_CACHE_PREFIX + token;
            Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedValue instanceof TokenData) {
                TokenData tokenData = (TokenData) cachedValue;
                
                // Check if token is expired
                if (tokenData.isExpired()) {
                    System.out.println("Gateway: توکن منقضی شده است: " + token);
                    redisTemplate.delete(cacheKey);
                    return null;
                }
                
                // Verify signature with Gateway's private key
                if (gatewayKeyGenerator.areKeysAvailable() && tokenData.isSignatureValid(gatewayKeyGenerator.getPrivateKey())) {
                    System.out.println("Gateway: توکن از کش با موفقیت بازیابی شد برای کاربر: " + tokenData.getUsername());
                    return tokenData;
                } else {
                    System.err.println("Gateway: امضای توکن تأیید نشد: " + token);
                    redisTemplate.delete(cacheKey);
                    return null;
                }
            }
            
        } catch (Exception e) {
            System.err.println("Gateway: خطا در بازیابی توکن از کش: " + e.getMessage());
        }
        
        return null;
    }

    public void invalidateToken(String token) {
        try {
            String cacheKey = TOKEN_CACHE_PREFIX + token;
            redisTemplate.delete(cacheKey);
            System.out.println("Gateway: توکن از کش حذف شد: " + token);
            
        } catch (Exception e) {
            System.err.println("Gateway: خطا در حذف توکن از کش: " + e.getMessage());
        }
    }

    public boolean hasCachedToken(String token) {
        try {
            String cacheKey = TOKEN_CACHE_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
            
        } catch (Exception e) {
            System.err.println("Gateway: خطا در بررسی وجود توکن در کش: " + e.getMessage());
            return false;
        }
    }

    public void clearAllTokens() {
        try {
            redisTemplate.delete(redisTemplate.keys(TOKEN_CACHE_PREFIX + "*"));
            System.out.println("Gateway: همه توکن‌ها از کش حذف شدند");
            
        } catch (Exception e) {
            System.err.println("Gateway: خطا در حذف همه توکن‌ها: " + e.getMessage());
        }
    }

    public void removeToken(String token) {
        try {
            String cacheKey = TOKEN_CACHE_PREFIX + token;
            redisTemplate.delete(cacheKey);
            System.out.println("Gateway: توکن منقضی از کش حذف شد: " + token);
            
        } catch (Exception e) {
            System.err.println("Gateway: خطا در حذف توکن منقضی: " + e.getMessage());
        }
    }

    public void removeUserTokens(String username) {
        try {
            // Find all tokens for a specific user
            var keys = redisTemplate.keys(TOKEN_CACHE_PREFIX + "*");
            if (keys != null) {
                for (String key : keys) {
                    Object value = redisTemplate.opsForValue().get(key);
                    if (value instanceof TokenData) {
                        TokenData tokenData = (TokenData) value;
                        if (username.equals(tokenData.getUsername())) {
                            redisTemplate.delete(key);
                            System.out.println("Gateway: توکن کاربر حذف شد: " + username);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Gateway: خطا در حذف توکن‌های کاربر: " + e.getMessage());
        }
    }
} 