package com.mahdi.sso.controller;

import com.mahdi.sso.dto.LoginRequest;
import com.mahdi.sso.dto.LoginResponse;
import com.mahdi.sso.service.AuthService;
import com.mahdi.sso.service.TokenSigningService;
import com.mahdi.sso.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Log4j2
public class AuthController {
    
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final TokenSigningService tokenSigningService;
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        log.info("Login request received for user: {}", loginRequest.getUsername());
        
        LoginResponse response = authService.login(loginRequest);
        
        if (response.isSuccess()) {
            log.info("Login successful for user: {}", loginRequest.getUsername());
            
            // Sign and cache the token
            tokenSigningService.signAndCacheToken(response.getToken(), loginRequest.getUsername());
            
            return ResponseEntity.ok(response);
        } else {
            log.warn("Login failed for user: {}", loginRequest.getUsername());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        log.info("Token validation request received");
        
        if (token == null || token.isEmpty()) {
            log.warn("Token validation failed - No token provided");
            Map<String, Object> errorResponse = Map.of(
                "valid", false,
                "message", "No token provided",
                "username", null
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        Map<String, Object> response = authService.validateToken(token);
        
        if (Boolean.TRUE.equals(response.get("valid"))) {
            log.info("Token validation successful");
            return ResponseEntity.ok(response);
        } else {
            log.warn("Token validation failed");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        log.info("Public key request received");
        
        String publicKey = jwtUtil.getPublicKeyForValidation();
        
        if (publicKey != null) {
            log.info("Public key provided successfully");
            return ResponseEntity.ok(Map.of("publicKey", publicKey));
        } else {
            log.error("Failed to provide public key");
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to get public key"));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.info("Health check request received");
        return ResponseEntity.ok("SSO Service is running");
    }
} 