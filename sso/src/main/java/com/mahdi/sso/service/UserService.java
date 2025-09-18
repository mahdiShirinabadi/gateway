package com.mahdi.sso.service;

import com.mahdi.sso.dto.CreateUserRequest;
import com.mahdi.sso.dto.CreateUserResponse;
import com.mahdi.sso.entity.User;
import com.mahdi.sso.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for user management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Create a new user
     */
    public CreateUserResponse createUser(CreateUserRequest request) {
        try {
            log.info("Creating user with username: {}", request.getUsername());
            
            // Check if user already exists
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                log.warn("User with username '{}' already exists", request.getUsername());
                return CreateUserResponse.error("Username already exists");
            }
            
            // Create new user
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            
            User savedUser = userRepository.save(user);
            log.info("User created successfully with ID: {}", savedUser.getId());
            
            return CreateUserResponse.success(savedUser.getId(), savedUser.getUsername());
            
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            return CreateUserResponse.error("Failed to create user: " + e.getMessage());
        }
    }
    
    /**
     * Check if user exists by username
     */
    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}

