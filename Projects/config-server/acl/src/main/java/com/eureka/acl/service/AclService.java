package com.eureka.acl.service;

import com.eureka.acl.entity.*;
import com.eureka.acl.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class AclService {
    
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    
    public boolean hasPermission(String username, String permissionName, String projectName) {
        log.info("Checking permission for user: {}, permission: {}", username, permissionName);
        
        // Find user
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            log.warn("User not found: {}", username);
            return false;
        }
        
        User user = userOpt.get();
        Role userRole = user.getRole();
        
        // Find permission
        Optional<Permission> permissionOpt = permissionRepository.findByNameAndProjectName(permissionName, projectName);
        if (permissionOpt.isEmpty()) {
            log.warn("Permission not found: {}", permissionName);
            return false;
        }
        
        Permission permission = permissionOpt.get();
        
        // Check if role has this permission
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleIdAndPermissionId(
                userRole.getId(), permission.getId());
        
        boolean hasAccess = !rolePermissions.isEmpty();
        
        log.info("Permission result for user {} on {} - {}", username, permissionName, hasAccess);
        
        return hasAccess;
    }
    
    public List<Permission> getUserPermissions(String username) {
        log.info("Getting permissions for user: {}", username);
        return permissionRepository.findPermissionsByUsername(username);
    }
    
    public void addRole(String roleName) {
        log.info("Adding role: {}", roleName);
        Role role = new Role();
        role.setName(roleName);
        roleRepository.save(role);
        log.info("Role added successfully: {}", roleName);
    }
    
    public void addPermission(String name, String projectName, boolean isCritical, String persianName) {
        log.info("Adding permission: {}", name);
        Permission permission = new Permission();
        permission.setName(name);
        permission.setProjectName(projectName);
        permission.setCritical(isCritical);
        permission.setPersianName(persianName);
        permissionRepository.save(permission);
        log.info("Permission added successfully: {}", name);
    }
    
    public void assignPermissionToRole(String roleName, String permissionName) {
        log.info("Assigning permission {} to role {}", permissionName, roleName);
        
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        Optional<Permission> permissionOpt = permissionRepository.findByName(permissionName);
        
        if (roleOpt.isEmpty() || permissionOpt.isEmpty()) {
            log.error("Role or permission not found");
            return;
        }
        
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(roleOpt.get());
        rolePermission.setPermission(permissionOpt.get());
        rolePermissionRepository.save(rolePermission);
        
        log.info("Permission {} assigned to role {} successfully", permissionName, roleName);
    }
    
    public void assignRoleToUser(String username, String roleName) {
        log.info("Assigning role {} to user {}", roleName, username);
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        
        if (userOpt.isEmpty() || roleOpt.isEmpty()) {
            log.error("User or role not found");
            return;
        }
        
        User user = userOpt.get();
        user.setRole(roleOpt.get());
        userRepository.save(user);
        
        log.info("Role {} assigned to user {} successfully", roleName, username);
    }
} 