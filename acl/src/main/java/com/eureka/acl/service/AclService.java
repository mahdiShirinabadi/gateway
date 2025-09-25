package com.eureka.acl.service;

import com.eureka.acl.entity.*;
import com.eureka.acl.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Log4j2
public class AclService {
    
    private final ApiPermissionRepository apiPermissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    

    /**
     * Get all permissions for a user (from all their roles)
     */
    public List<ApiPermission> getUserPermissions(String username) {
        log.info("Getting permissions for user: {}", username);
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            log.warn("User not found: {}", username);
            return List.of();
        }
        
        User user = userOpt.get();
        Set<Role> userRoles = user.getRoles();
        
        if (userRoles == null || userRoles.isEmpty()) {
            log.warn("User {} has no roles assigned", username);
            return List.of();
        }
        
        // Get all permissions from all user roles
        return userRoles.stream()
                .flatMap(role -> rolePermissionRepository.findByRoleId(role.getId()).stream())
                .map(RolePermission::getPermission)
                .distinct()  // Remove duplicates
                .toList();
    }
    
    /**
     * Check if user has specific role
     */
    public boolean userHasRole(String username, String roleName) {
        return userRoleRepository.userHasRole(username, roleName);
    }
    
    /**
     * Get all roles for a user
     */
    public List<Role> getUserRoles(String username) {
        return userRoleRepository.findRolesByUsername(username);
    }
    
    /**
     * Get primary role for a user
     */
    public Optional<Role> getUserPrimaryRole(String username) {
        return userRoleRepository.findPrimaryRoleByUsername(username);
    }
    
    /**
     * Assign role to user
     */
    public boolean assignRoleToUser(String username, String roleName, boolean isPrimary) {
        try {
            log.info("Assigning role {} to user {} (primary: {})", roleName, username, isPrimary);
            
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                log.error("User not found: {}", username);
                return false;
            }
            
            Optional<Role> roleOpt = roleRepository.findByName(roleName);
            if (roleOpt.isEmpty()) {
                log.error("Role not found: {}", roleName);
                return false;
            }
            
            // Check if already assigned
            if (userRoleRepository.userHasRole(username, roleName)) {
                log.info("User {} already has role {}", username, roleName);
                return true;
            }
            
            // If this is primary role, unset other primary roles
            if (isPrimary) {
                userRoleRepository.findByUsername(username).forEach(ur -> {
                    if (ur.isPrimary()) {
                        ur.setPrimary(false);
                        userRoleRepository.save(ur);
                    }
                });
            }
            
            // Create new user-role relationship
            UserRole userRole = new UserRole();
            userRole.setUser(userOpt.get());
            userRole.setRole(roleOpt.get());
            userRole.setPrimary(isPrimary);
            userRole.setCreateBy("system");
            userRole.setCreateTime(java.time.LocalDateTime.now());
            
            userRoleRepository.save(userRole);
            log.info("Role {} assigned to user {} successfully", roleName, username);
            return true;
            
        } catch (Exception e) {
            log.error("Error assigning role to user: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Remove role from user
     */
    public boolean removeRoleFromUser(String username, String roleName) {
        try {
            log.info("Removing role {} from user {}", roleName, username);
            
            Optional<UserRole> userRoleOpt = userRoleRepository.findByUserAndRole(username, roleName);
            if (userRoleOpt.isEmpty()) {
                log.warn("User {} does not have role {}", username, roleName);
                return false;
            }
            
            userRoleRepository.delete(userRoleOpt.get());
            log.info("Role {} removed from user {} successfully", roleName, username);
            return true;
            
        } catch (Exception e) {
            log.error("Error removing role from user: {}", e.getMessage());
            return false;
        }
    }
}