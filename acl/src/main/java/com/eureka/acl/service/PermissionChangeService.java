package com.eureka.acl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Log4j2
public class PermissionChangeService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String GATEWAY_TOKEN_PREFIX = "gateway_token:";
    private static final String ACL_TOKEN_PREFIX = "token:";

    public void notifyPermissionChange(String username) {
        try {
            log.info("Permission change detected for user: {}", username);
            
            // Remove all tokens for this user from Gateway cache
            removeUserTokensFromGateway(username);
            
            // Remove all tokens for this user from ACL cache
            removeUserTokensFromACL(username);
            
            log.info("All tokens invalidated for user: {}", username);
            
        } catch (Exception e) {
            log.error("Error notifying permission change for user {}: {}", username, e.getMessage());
        }
    }

    private void removeUserTokensFromGateway(String username) {
        try {
            Set<String> keys = redisTemplate.keys(GATEWAY_TOKEN_PREFIX + "*");
            if (keys != null) {
                for (String key : keys) {
                    Object value = redisTemplate.opsForValue().get(key);
                    if (value != null) {
                        // Check if this token belongs to the user
                        // This would need to be adapted based on your TokenData structure
                        redisTemplate.delete(key);
                        log.info("Gateway token removed for user: {}", username);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error removing Gateway tokens for user {}: {}", username, e.getMessage());
        }
    }

    private void removeUserTokensFromACL(String username) {
        try {
            Set<String> keys = redisTemplate.keys(ACL_TOKEN_PREFIX + "*");
            if (keys != null) {
                for (String key : keys) {
                    Object value = redisTemplate.opsForValue().get(key);
                    if (value != null) {
                        // Check if this token belongs to the user
                        redisTemplate.delete(key);
                        log.info("ACL token removed for user: {}", username);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error removing ACL tokens for user {}: {}", username, e.getMessage());
        }
    }

    public void notifyAllUsersPermissionChange() {
        try {
            log.info("Global permission change detected - invalidating all tokens");
            
            // Remove all Gateway tokens
            Set<String> gatewayKeys = redisTemplate.keys(GATEWAY_TOKEN_PREFIX + "*");
            if (gatewayKeys != null && !gatewayKeys.isEmpty()) {
                redisTemplate.delete(gatewayKeys);
                log.info("All Gateway tokens removed: {} tokens", gatewayKeys.size());
            }
            
            // Remove all ACL tokens
            Set<String> aclKeys = redisTemplate.keys(ACL_TOKEN_PREFIX + "*");
            if (aclKeys != null && !aclKeys.isEmpty()) {
                redisTemplate.delete(aclKeys);
                log.info("All ACL tokens removed: {} tokens", aclKeys.size());
            }
            
        } catch (Exception e) {
            log.error("Error notifying global permission change: {}", e.getMessage());
        }
    }
}

