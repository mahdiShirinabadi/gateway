package com.eureka.service1.controller;

import com.eureka.service1.config.SignatureConfig;
import com.eureka.service1.service.SignatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/signature")
@RequiredArgsConstructor
@Log4j2
public class SignatureConfigController {
    
    private final SignatureConfig signatureConfig;
    private final SignatureService signatureService;
    
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getSignatureConfig() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("success", true);
            response.put("format", signatureConfig.getFormat());
            response.put("separator", signatureConfig.getSeparator());
            response.put("fields", signatureConfig.getFields());
            response.put("algorithm", signatureConfig.getAlgorithm());
            response.put("encoding", signatureConfig.getEncoding());
            response.put("validationEnabled", signatureConfig.isValidationEnabled());
            response.put("debugEnabled", signatureConfig.isDebugEnabled());
            
            log.info("Signature configuration loaded from config-server: {}", signatureConfig.getFormat());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            log.error("Error reading signature configuration: {}", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testSignatureGeneration() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test signature generation
            String username = "testuser";
            List<String> permissions = List.of("SERVICE1_ALL_ACCESS", "SERVICE1_HELLO_ACCESS");
            String token = "test-token-123";
            
            String signature = signatureService.generateSignature(username, permissions, token);
            boolean isValid = signatureService.verifySignature(username, permissions, token, signature);
            
            response.put("success", true);
            response.put("username", username);
            response.put("permissions", permissions);
            response.put("token", token);
            response.put("signature", signature);
            response.put("isValid", isValid);
            response.put("format", signatureConfig.getFormat());
            
            log.info("Signature test successful: {} -> {}", signatureConfig.getFormat(), signature);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            log.error("Error testing signature generation: {}", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/format")
    public ResponseEntity<Map<String, Object>> getSignatureFormat() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("success", true);
            response.put("format", signatureConfig.getFormat());
            response.put("separator", signatureConfig.getSeparator());
            response.put("fields", signatureConfig.getFields());
            response.put("example", "username|permissions|token");
            
            log.info("Signature format: {}", signatureConfig.getFormat());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            log.error("Error getting signature format: {}", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
