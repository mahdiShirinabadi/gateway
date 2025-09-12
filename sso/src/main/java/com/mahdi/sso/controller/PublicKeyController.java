package com.mahdi.sso.controller;

import com.mahdi.sso.util.RsaKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PublicKeyController {
    
    private final RsaKeyGenerator rsaKeyGenerator;
    
    /**
     * Expose SSO public key for signature verification
     * This endpoint is called by Service1 to get the public key for verifying signatures
     */
    @GetMapping("/public-key/get")
    public ResponseEntity<Map<String, Object>> getPublicKey() {
        try {
            log.info("Public key requested by Service1");
            
            // Get the public key from RSA key generator
            PublicKey publicKey = rsaKeyGenerator.getPublicKey();
            
            if (publicKey == null) {
                log.error("Public key not available");
                return ResponseEntity.status(500).body(Map.of(
                    "error", "Public key not available",
                    "message", "SSO service is not properly configured"
                ));
            }
            
            // Encode public key to Base64 string
            String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            
            Map<String, Object> response = new HashMap<>();
            response.put("publicKey", publicKeyString);
            response.put("algorithm", publicKey.getAlgorithm());
            response.put("format", publicKey.getFormat());
            response.put("timestamp", System.currentTimeMillis());
            response.put("message", "SSO public key for signature verification");
            
            log.info("Public key provided to Service1 successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error providing public key: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to provide public key",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Health check endpoint for public key availability
     */
    @GetMapping("/public-key/health")
    public ResponseEntity<Map<String, Object>> getPublicKeyHealth() {
        try {
            PublicKey publicKey = rsaKeyGenerator.getPublicKey();
            boolean isAvailable = publicKey != null;
            
            Map<String, Object> response = new HashMap<>();
            response.put("available", isAvailable);
            response.put("algorithm", isAvailable ? publicKey.getAlgorithm() : "N/A");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error checking public key health: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "available", false,
                "error", e.getMessage()
            ));
        }
    }
} 