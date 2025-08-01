package com.mahdi.sso.controller;

import com.mahdi.sso.dto.LoginRequest;
import com.mahdi.sso.dto.LoginResponse;
import com.mahdi.sso.service.AuthService;
import com.mahdi.sso.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Log4j2
public class AuthController {
    
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        log.info("Received login request for user: {}", loginRequest.getUsername());
        
        LoginResponse response = authService.login(loginRequest);
        
        if (response.isSuccess()) {
            log.info("Login successful for user: {}", loginRequest.getUsername());
            return ResponseEntity.ok(response);
        } else {
            log.warn("Login failed for user: {} - {}", loginRequest.getUsername(), response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestParam String token, @RequestParam String username) {
        log.debug("Token validation request for user: {}", username);
        boolean isValid = authService.validateToken(token, username);
        
        Map<String, Object> response = Map.of(
                "valid", isValid,
                "message", isValid ? "Token is valid" : "Token is invalid",
                "username", isValid ? username : null
        );
        
        log.debug("Token validation result for user {}: {}", username, isValid);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/public-key")
    public ResponseEntity<String> getPublicKey() {
        log.debug("Public key request received");
        try {
            PublicKey publicKey = jwtUtil.getPublicKeyForValidation();
            String encodedKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            log.debug("Public key provided successfully");
            return ResponseEntity.ok(encodedKey);
        } catch (Exception e) {
            log.error("Error getting public key", e);
            return ResponseEntity.internalServerError().body("Error getting public key");
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.debug("Health check request received");
        return ResponseEntity.ok("SSO Service is running");
    }
} 