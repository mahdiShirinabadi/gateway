package com.eureka.service1.config;

import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Log4j2
public class JwtTokenValidator {

    private final WebClient webClient;
    
    @Value("${sso.service.url}")
    private String ssoValidationUrl;
    
    @Value("${acl.service.url}")
    private String aclCheckUrl;

    public JwtTokenValidator(WebClient webClient) {
        this.webClient = webClient;
    }

    public boolean validateToken(String token) {
        try {
            log.debug("Validating token with SSO service");
            
            Map<String, Object> response = webClient.post()
                    .uri(ssoValidationUrl)
                    .header("Content-Type", "application/json")
                    .bodyValue(Map.of("token", token))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null && response.containsKey("valid")) {
                boolean isValid = (Boolean) response.get("valid");
                log.debug("Token validation result: {}", isValid);
                return isValid;
            }
            
            log.warn("Invalid response from SSO service");
            return false;
            
        } catch (Exception e) {
            log.error("Error validating token with SSO service: {}", e.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            log.debug("Extracting username from token");
            
            Map<String, Object> response = webClient.post()
                    .uri(ssoValidationUrl)
                    .header("Content-Type", "application/json")
                    .bodyValue(Map.of("token", token))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null && response.containsKey("username")) {
                String username = (String) response.get("username");
                log.debug("Extracted username: {}", username);
                return username;
            }
            
            log.warn("Could not extract username from token");
            return null;
            
        } catch (Exception e) {
            log.error("Error extracting username from token: {}", e.getMessage());
            return null;
        }
    }
    
    public boolean checkAuthorization(String username, String permissionName) {
        try {
            log.debug("Checking authorization for user: {} with permission: {}", username, permissionName);
            
            Map<String, Object> response = webClient.post()
                    .uri(aclCheckUrl)
                    .header("Content-Type", "application/json")
                    .bodyValue(Map.of("username", username, "permissionName", permissionName))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null && response.containsKey("allowed")) {
                boolean isAllowed = (Boolean) response.get("allowed");
                log.debug("Authorization check result: {} for user: {} with permission: {}", isAllowed, username, permissionName);
                return isAllowed;
            }
            
            log.warn("Invalid response from ACL service");
            return false;
            
        } catch (Exception e) {
            log.error("Error checking authorization with ACL service: {}", e.getMessage());
            return false;
        }
    }
    
    public String getPermissionNameForPath(String path, String method) {
        // Map paths to permission names (same logic as Gateway)
        if (path.startsWith("/app1/hello")) {
            return "SERVICE1_HELLO_ACCESS";
        } else if (path.startsWith("/app1/admin")) {
            return "SERVICE1_ADMIN_ACCESS";
        } else if (path.startsWith("/app1/")) {
            return "SERVICE1_ALL_ACCESS";
        }
        return "DEFAULT_PERMISSION";
    }
} 