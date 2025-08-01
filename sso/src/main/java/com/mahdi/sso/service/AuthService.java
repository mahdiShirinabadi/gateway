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

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService {
    
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());
        
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Check if password matches (in real application, password should be encoded)
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                String token = jwtUtil.generateToken(user.getUsername());
                log.info("Login successful for user: {}", user.getUsername());
                return new LoginResponse(token);
            } else {
                log.warn("Invalid password for user: {}", loginRequest.getUsername());
                return new LoginResponse("Invalid password", false);
            }
        } else {
            log.warn("User not found: {}", loginRequest.getUsername());
            return new LoginResponse("User not found", false);
        }
    }
    
    public boolean validateToken(String token, String username) {
        log.debug("Validating token for user: {}", username);
        return jwtUtil.validateToken(token, username);
    }
    
    public String extractUsernameFromToken(String token) {
        log.debug("Extracting username from token");
        return jwtUtil.extractUsername(token);
    }
} 