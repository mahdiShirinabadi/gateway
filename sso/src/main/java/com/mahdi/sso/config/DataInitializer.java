package com.mahdi.sso.config;

import com.mahdi.sso.entity.User;
import com.mahdi.sso.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");
        
        // Create a test user if it doesn't exist
        if (!userRepository.findByUsername("testuser").isPresent()) {
            User testUser = new User();
            testUser.setUsername("testuser");
            testUser.setPassword(passwordEncoder.encode("password123"));
            userRepository.save(testUser);
            log.info("Test user created: username=testuser, password=password123");
        } else {
            log.info("Test user already exists, skipping creation");
        }
        
        log.info("Data initialization completed");
    }
} 