package com.mahdi.sso.controller;

import com.mahdi.sso.dto.LoginRequest;
import com.mahdi.sso.dto.LoginResponse;
import com.mahdi.sso.service.AuthService;
import com.mahdi.sso.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.PublicKey;
import java.util.Base64;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token, @RequestParam String username) {
        boolean isValid = authService.validateToken(token, username);
        return ResponseEntity.ok(isValid);
    }
    
    @GetMapping("/public-key")
    public ResponseEntity<String> getPublicKey() {
        try {
            PublicKey publicKey = jwtUtil.getPublicKeyForValidation();
            String encodedKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            return ResponseEntity.ok(encodedKey);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error getting public key");
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("SSO Service is running");
    }
} 