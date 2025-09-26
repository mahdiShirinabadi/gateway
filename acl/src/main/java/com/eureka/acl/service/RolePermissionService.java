package com.eureka.acl.service;

import com.eureka.acl.entity.ApiPermission;
import com.eureka.acl.entity.Role;
import com.eureka.acl.entity.RolePermission;
import com.eureka.acl.repository.ApiPermissionRepository;
import com.eureka.acl.repository.RolePermissionRepository;
import com.eureka.acl.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing role-permission relationships
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class RolePermissionService {
    
    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final ApiPermissionRepository apiPermissionRepository;
    
    /**
     * Assign permission to role
     */
    @Transactional
    public boolean assignPermissionToRole(Long roleId, Long permissionId) {
        try {
            log.info("Assigning permission {} to role {}", permissionId, roleId);
            
            Optional<Role> roleOpt = roleRepository.findById(roleId);
            if (roleOpt.isEmpty()) {
                log.warn("Role {} not found", roleId);
                return false;
            }
            
            Optional<ApiPermission> permissionOpt = apiPermissionRepository.findById(permissionId);
            if (permissionOpt.isEmpty()) {
                log.warn("Permission {} not found", permissionId);
                return false;
            }
            
            Role role = roleOpt.get();
            ApiPermission permission = permissionOpt.get();
            
            // Check if already exists
            if (rolePermissionRepository.existsByRoleAndPermission(role, permission)) {
                log.info("Permission {} already assigned to role {}", permissionId, roleId);
                return true;
            }
            
            // Create new role-permission relationship
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            rolePermission.setCreatedBy("system");
            rolePermission.setCreatedAt(java.time.LocalDateTime.now());
            
            rolePermissionRepository.save(rolePermission);
            log.info("Permission {} successfully assigned to role {}", permissionId, roleId);
            return true;
            
        } catch (Exception e) {
            log.error("Error assigning permission to role: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Remove permission from role
     */
    @Transactional
    public boolean removePermissionFromRole(Long roleId, Long permissionId) {
        try {
            log.info("Removing permission {} from role {}", permissionId, roleId);
            
            Optional<Role> roleOpt = roleRepository.findById(roleId);
            if (roleOpt.isEmpty()) {
                log.warn("Role {} not found", roleId);
                return false;
            }
            
            Optional<ApiPermission> permissionOpt = apiPermissionRepository.findById(permissionId);
            if (permissionOpt.isEmpty()) {
                log.warn("Permission {} not found", permissionId);
                return false;
            }
            
            Role role = roleOpt.get();
            ApiPermission permission = permissionOpt.get();
            
            Optional<RolePermission> rolePermissionOpt = rolePermissionRepository.findByRoleAndPermission(role, permission);
            if (rolePermissionOpt.isEmpty()) {
                log.warn("Permission {} not assigned to role {}", permissionId, roleId);
                return false;
            }
            
            rolePermissionRepository.delete(rolePermissionOpt.get());
            log.info("Permission {} successfully removed from role {}", permissionId, roleId);
            return true;
            
        } catch (Exception e) {
            log.error("Error removing permission from role: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all permissions for a role
     */
    public List<ApiPermission> getRolePermissions(Long roleId) {
        log.info("Getting permissions for role {}", roleId);
        return rolePermissionRepository.findByRoleId(roleId)
                .stream()
                .map(RolePermission::getPermission)
                .toList();
    }
    
    /**
     * Check if role has specific permission
     */
    public boolean roleHasPermission(Long roleId, Long permissionId) {
        return rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId);
    }
    
    /**
     * Get all roles that have a specific permission
     */
    public List<Role> getRolesWithPermission(Long permissionId) {
        log.info("Getting roles with permission {}", permissionId);
        return rolePermissionRepository.findByPermissionId(permissionId)
                .stream()
                .map(RolePermission::getRole)
                .toList();
    }
}
