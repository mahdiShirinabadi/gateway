package com.eureka.service1.service;

import com.eureka.service1.model.TokenInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class TokenInfoService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    public TokenInfoService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Store token info in Redis with token as key and JSON data as value
     * @param token The JWT token (used as Redis key)
     * @param tokenInfo The token information to store as JSON
     * @param ttlMinutes Time to live in minutes
     */
    public void storeTokenInfo(String token, TokenInfo tokenInfo, long ttlMinutes) {
        try {
            String jsonData = objectMapper.writeValueAsString(tokenInfo);
            redisTemplate.opsForValue().set(token, jsonData, ttlMinutes, TimeUnit.MINUTES);
            log.info("Stored token info for token: {} with TTL: {} minutes", token, ttlMinutes);
        } catch (JsonProcessingException e) {
            log.error("Error serializing token info to JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to store token info", e);
        }
    }
    
    /**
     * Store token info with default TTL (30 minutes)
     */
    public void storeTokenInfo(String token, TokenInfo tokenInfo) {
        storeTokenInfo(token, tokenInfo, 30);
    }
    
    /**
     * Retrieve token info from Redis
     * @param token The JWT token (Redis key)
     * @return TokenInfo object or null if not found/expired
     */
    public TokenInfo getTokenInfo(String token) {
        try {
            String jsonData = redisTemplate.opsForValue().get(token);

            TokenInfo tokenInfo = objectMapper.readValue(jsonData, TokenInfo.class);
            
            // Check if token is expired
            if (tokenInfo.isExpired()) {
                log.info("Token info expired for token: {}", token);
                // Remove expired token
                redisTemplate.delete(token);
                return null;
            }
            
            log.debug("Retrieved token info for token: {}", token);
            return tokenInfo;
            
        } catch (JsonProcessingException e) {
            log.error("Error deserializing token info from JSON: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if token exists and is valid
     */
    public boolean isTokenValid(String token) {
        TokenInfo tokenInfo = getTokenInfo(token);
        return tokenInfo != null && !tokenInfo.isExpired();
    }
    
    /**
     * Check if token has specific permission
     */
    public boolean hasPermission(String token, String permission) {
        TokenInfo tokenInfo = getTokenInfo(token);
        return tokenInfo != null && tokenInfo.hasPermission(permission);
    }
    
    /**
     * Check if token has any of the required permissions
     */
    public boolean hasAnyPermission(String token, List<String> requiredPermissions) {
        TokenInfo tokenInfo = getTokenInfo(token);
        return tokenInfo != null && tokenInfo.hasAnyPermission(requiredPermissions);
    }
    
    /**
     * Get username from token
     */
    public String getUsername(String token) {
        TokenInfo tokenInfo = getTokenInfo(token);
        return tokenInfo != null ? tokenInfo.getUsername() : null;
    }
    
    /**
     * Get all permissions for token
     */
    public List<String> getPermissions(String token) {
        TokenInfo tokenInfo = getTokenInfo(token);
        return tokenInfo != null ? tokenInfo.getPermissions() : null;
    }
    
    /**
     * Remove token from Redis
     */
    public void removeToken(String token) {
        redisTemplate.delete(token);
        log.info("Removed token from Redis: {}", token);
    }
    
    /**
     * Update token TTL
     */
    public void refreshToken(String token, long ttlMinutes) {
        if (redisTemplate.hasKey(token)) {
            redisTemplate.expire(token, ttlMinutes, TimeUnit.MINUTES);
            log.info("Refreshed TTL for token: {} to {} minutes", token, ttlMinutes);
        }
    }
    
    /**
     * Get remaining TTL for token
     */
    public long getTokenTTL(String token) {
        return redisTemplate.getExpire(token, TimeUnit.SECONDS);
    }
}
