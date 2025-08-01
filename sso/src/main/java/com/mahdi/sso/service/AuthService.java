package com.mahdi.sso.service;

import com.mahdi.sso.dto.LoginRequest;
import com.mahdi.sso.dto.LoginResponse;
import com.mahdi.sso.entity.User;
import com.mahdi.sso.repository.UserRepository;
import com.mahdi.sso.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());
        
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                String token = jwtUtil.generateToken(user.getUsername());
                log.info("Login successful for user: {}", loginRequest.getUsername());
                return new LoginResponse(token);
            } else {
                log.warn("Login failed for user: {} - Invalid password", loginRequest.getUsername());
                return new LoginResponse("Invalid credentials", false);
            }
        } else {
            log.warn("Login failed for user: {} - User not found", loginRequest.getUsername());
            return new LoginResponse("Invalid credentials", false);
        }
    }
    
    public Map<String, Object> validateToken(String token) {
        log.info("Validating token");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                log.info("Token validation successful for user: {}", username);
                
                response.put("valid", true);
                response.put("message", "Token is valid");
                response.put("username", username);
            } else {
                log.warn("Token validation failed - Invalid token");
                response.put("valid", false);
                response.put("message", "Invalid token");
                response.put("username", null);
            }
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            response.put("valid", false);
            response.put("message", "Error validating token: " + e.getMessage());
            response.put("username", null);
        }
        
        return response;
    }
} 