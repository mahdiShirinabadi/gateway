package com.eureka.acl.config;

import com.eureka.acl.entity.*;
import com.eureka.acl.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class DataInitializer implements CommandLineRunner {
    
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Starting ACL data initialization...");
        
        // Note: Initial data is now handled by Flyway migrations
        // This component can be used for additional data setup if needed
        
        log.info("ACL data initialization completed - using Flyway migrations for initial data");
    }
} 