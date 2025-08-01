package com.mahdi.sso.service;

import com.mahdi.sso.dto.LoginRequest;
import com.mahdi.sso.dto.LoginResponse;
import com.mahdi.sso.entity.User;
import com.mahdi.sso.repository.UserRepository;
import com.mahdi.sso.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public LoginResponse login(LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Check if password matches (in real application, password should be encoded)
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                String token = jwtUtil.generateToken(user.getUsername());
                return new LoginResponse(token);
            } else {
                return new LoginResponse("Invalid password", false);
            }
        } else {
            return new LoginResponse("User not found", false);
        }
    }
    
    public boolean validateToken(String token, String username) {
        return jwtUtil.validateToken(token, username);
    }
    
    public String extractUsernameFromToken(String token) {
        return jwtUtil.extractUsername(token);
    }
} 