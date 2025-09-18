package com.eureka.service1.controller;

import com.eureka.service1.config.PublicKeyConfig;
import com.eureka.service1.service.PublicKeyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {
    private final PublicKeyService publicKeyService;
    private final PublicKeyConfig publicKeyConfig;

    @GetMapping("/config/status")
    public ResponseEntity<Map<String, Object>> getConfigStatus(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean gatewayKeyCached = publicKeyService.hasCachedPublicKey("gateway");
            boolean ssoKeyCached = publicKeyService.hasCachedPublicKey("sso");
            boolean service1KeyCached = publicKeyService.hasCachedPublicKey("service1");
            
            PublicKey gatewayKey = publicKeyService.getGatewayPublicKey();
            PublicKey ssoKey = publicKeyService.getSsoPublicKey();
            PublicKey service1Key = publicKeyService.getService1PublicKey();
            
            // Get security info from request
            String authenticatedUser = (String) request.getAttribute("authenticatedUser");
            String validatedToken = (String) request.getAttribute("validatedToken");
            
            response.put("success", true);
            response.put("gatewayKeyCached", gatewayKeyCached);
            response.put("ssoKeyCached", ssoKeyCached);
            response.put("service1KeyCached", service1KeyCached);
            response.put("gatewayKeyAvailable", gatewayKey != null);
            response.put("ssoKeyAvailable", ssoKey != null);
            response.put("service1KeyAvailable", service1Key != null);
            response.put("authenticatedUser", authenticatedUser);
            response.put("tokenValidated", validatedToken != null);
            response.put("securityLevel", "SERVICE1_SECURITY_ENABLED");
            response.put("message", "Config Server status checked with Service1 security");
            
            log.info("Service1: Config status - Gateway: {} (cached: {}), SSO: {} (cached: {}), Service1: {} (cached: {})", 
                    gatewayKey != null, gatewayKeyCached, ssoKey != null, ssoKeyCached, service1Key != null, service1KeyCached);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            log.error("Service1: Error checking config status: {}", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/config/refresh/{serviceName}")
    public ResponseEntity<Map<String, Object>> refreshConfig(@PathVariable String serviceName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Service1: Requesting config refresh for service: {}", serviceName);
            PublicKey newKey = publicKeyService.refreshPublicKey(serviceName);
            
            response.put("success", true);
            response.put("serviceName", serviceName);
            response.put("newKeyAvailable", newKey != null);
            response.put("message", "Config refreshed for " + serviceName);
            
            log.info("Service1: Config successfully refreshed for {}: {}", serviceName, newKey != null);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            log.error("Service1: Error refreshing config for {}: {}", serviceName, e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/config/keys")
    public ResponseEntity<Map<String, Object>> getConfigKeys() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("success", true);
            response.put("gatewayConfig", publicKeyConfig.getGateway());
            response.put("ssoConfig", publicKeyConfig.getSso());
            response.put("service1Config", publicKeyConfig.getService1());
            response.put("message", "Config keys retrieved from Config Server");
            
            log.info("Service1: Config keys retrieved from Config Server");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            log.error("Service1: Error getting config keys: {}", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
} 