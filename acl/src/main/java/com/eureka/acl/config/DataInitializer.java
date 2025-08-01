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
        
        // Create roles
        createRoleIfNotExists("USER");
        createRoleIfNotExists("ADMIN");
        createRoleIfNotExists("SUPER_ADMIN");
        
        // Create permissions
        createPermissionIfNotExists("SERVICE1_HELLO_ACCESS", "service1", false, "دسترسی به صفحه سلام");
        createPermissionIfNotExists("SERVICE1_ADMIN_ACCESS", "service1", true, "دسترسی ادمین به سرویس 1");
        createPermissionIfNotExists("SERVICE1_ALL_ACCESS", "service1", true, "دسترسی کامل به سرویس 1");
        
        // Assign permissions to roles
        assignPermissionToRoleIfNotExists("USER", "SERVICE1_HELLO_ACCESS");
        assignPermissionToRoleIfNotExists("ADMIN", "SERVICE1_HELLO_ACCESS");
        assignPermissionToRoleIfNotExists("ADMIN", "SERVICE1_ADMIN_ACCESS");
        assignPermissionToRoleIfNotExists("SUPER_ADMIN", "SERVICE1_ALL_ACCESS");
        
        // Create users and assign roles
        createUserIfNotExists("testuser", "USER");
        createUserIfNotExists("admin", "ADMIN");
        createUserIfNotExists("superadmin", "SUPER_ADMIN");
        
        log.info("ACL data initialization completed");
    }
    
    private void createRoleIfNotExists(String roleName) {
        if (!roleRepository.findByName(roleName).isPresent()) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
            log.info("Created role: {}", roleName);
        }
    }
    
    private void createPermissionIfNotExists(String name, String projectName, boolean isCritical, String persianName) {
        if (!permissionRepository.findByName(name).isPresent()) {
            Permission permission = new Permission();
            permission.setName(name);
            permission.setProjectName(projectName);
            permission.setCritical(isCritical);
            permission.setPersianName(persianName);
            permissionRepository.save(permission);
            log.info("Created permission: {}", name);
        }
    }
    
    private void assignPermissionToRoleIfNotExists(String roleName, String permissionName) {
        Role role = roleRepository.findByName(roleName).orElse(null);
        Permission permission = permissionRepository.findByName(permissionName).orElse(null);
        
        if (role != null && permission != null) {
            if (rolePermissionRepository.findByRoleIdAndPermissionId(role.getId(), permission.getId()).isEmpty()) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRole(role);
                rolePermission.setPermission(permission);
                rolePermissionRepository.save(rolePermission);
                log.info("Assigned permission {} to role {}", permissionName, roleName);
            }
        }
    }
    
    private void createUserIfNotExists(String username, String roleName) {
        if (!userRepository.findByUsername(username).isPresent()) {
            Role role = roleRepository.findByName(roleName).orElse(null);
            if (role != null) {
                User user = new User();
                user.setUsername(username);
                user.setRole(role);
                userRepository.save(user);
                log.info("Created user {} with role {}", username, roleName);
            }
        }
    }
} 