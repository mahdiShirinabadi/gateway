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
        log.info("Starting SSO data initialization...");
        
        // Note: Initial users are now handled by Flyway migrations
        // This component can be used for additional data setup if needed
        
        log.info("SSO data initialization completed - using Flyway migrations for initial data");
    }
} 