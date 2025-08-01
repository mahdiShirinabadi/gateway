package com.mahdi.sso.config;

import com.mahdi.sso.entity.User;
import com.mahdi.sso.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Create a test user if it doesn't exist
        if (!userRepository.findByUsername("testuser").isPresent()) {
            User testUser = new User();
            testUser.setUsername("testuser");
            testUser.setPassword(passwordEncoder.encode("password123"));
            userRepository.save(testUser);
            System.out.println("Test user created: username=testuser, password=password123");
        }
    }
} 