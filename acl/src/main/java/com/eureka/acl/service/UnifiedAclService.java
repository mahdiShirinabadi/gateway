package com.eureka.acl.service;

import com.eureka.acl.entity.ApiPermission;
import com.eureka.acl.entity.Group;
import com.eureka.acl.entity.Project;
import com.eureka.acl.entity.Role;
import com.eureka.acl.entity.User;
import com.eureka.acl.repository.ApiPermissionRepository;
import com.eureka.acl.repository.ProjectRepository;
import com.eureka.acl.repository.RolePermissionRepository;
import com.eureka.acl.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Unified ACL Service
 * Handles both permission checking and API registration with multiple roles support
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class UnifiedAclService {
    
    private final ApiPermissionRepository apiPermissionRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;
    
    /**
     * Check if user has permission for specific API
     * Supports multiple roles per user
     */
    public boolean hasPermission(String username, String projectName, String apiPath, String httpMethod, String permissionName) {
        log.info("=== UnifiedAclService.hasPermission() START ===");
        log.info("Parameters: username={}, projectName={}, apiPath={}, httpMethod={}, permissionName={}",
                username, projectName, apiPath, httpMethod, permissionName);
        
        try {
            // Find user
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                log.warn("User not found: username={}", username);
                log.info("=== UnifiedAclService.hasPermission() END - USER NOT FOUND ===");
                return false;
            }
            
            User user = userOpt.get();
            Set<Group> userGroups = user.getGroups();
            
            if (userGroups == null || userGroups.isEmpty()) {
                log.warn("User {} has no groups assigned", username);
                log.info("=== UnifiedAclService.hasPermission() END - NO GROUPS ===");
                return false;
            }
            
            log.info("User {} belongs to {} groups: {}", username, userGroups.size(),
                    userGroups.stream().map(Group::getName).collect(java.util.stream.Collectors.toList()));
            
            // Get all roles from all user's groups
            List<Role> userRoles = userGroups.stream()
                    .flatMap(group -> group.getRoles().stream())
                    .distinct()
                    .toList();
            
            if (userRoles.isEmpty()) {
                log.warn("User {} has no roles through groups", username);
                log.info("=== UnifiedAclService.hasPermission() END - NO ROLES ===");
                return false;
            }
            
            log.info("User {} has {} roles: {}", username, userRoles.size(),
                    userRoles.stream().map(Role::getName).collect(java.util.stream.Collectors.toList()));

            Long projectId = projectRepository.findByName(projectName).get().getId();
            // Find API permission
            Optional<ApiPermission> apiPermissionOpt = apiPermissionRepository
                    .findByProjectAndApi(projectId, apiPath, httpMethod, permissionName);
            
            if (apiPermissionOpt.isEmpty()) {
                log.warn("API permission not found for project: {} api: {} method: {}", 
                        projectName, apiPath, httpMethod);
                log.info("=== UnifiedAclService.hasPermission() END - PERMISSION NOT FOUND ===");
                return false;
            }
            
            ApiPermission apiPermission = apiPermissionOpt.get();
            log.info("Found API permission: id={}, name={}, isPublic={}, isCritical={}", 
                    apiPermission.getId(), apiPermission.getName(), apiPermission.isPublic(), apiPermission.isCritical());
            
            // Check if API is public
            if (apiPermission.isPublic()) {
                log.info("API {} is public, access granted", apiPermission.getName());
                log.info("=== UnifiedAclService.hasPermission() END - PUBLIC ACCESS ===");
                return true;
            }
            
            // Check if any of user's roles has this permission
            for (Role role : userRoles) {
                boolean hasPermission = rolePermissionRepository.existsByRoleAndPermission(role, apiPermission);
                if (hasPermission) {
                    log.info("Permission found for user {} with role {}", username, role.getName());
                    log.info("=== UnifiedAclService.hasPermission() END - PERMISSION GRANTED ===");
                    return true;
                }
            }
            
            log.warn("No permission found for user {} on any role for API {}", username, apiPermission.getName());
            log.info("=== UnifiedAclService.hasPermission() END - PERMISSION DENIED ===");
            return false;
            
        } catch (Exception e) {
            log.error("=== UnifiedAclService.hasPermission() END - ERROR ===");
            log.error("Error checking permission: username={}, projectName={}, apiPath={}, httpMethod={}, permissionName={}, error={}", 
                    username, projectName, apiPath, httpMethod, permissionName, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Check if user has permission by permission name only
     */
    public boolean hasPermissionByName(String username, String permissionName) {
        log.info("=== UnifiedAclService.hasPermissionByName() START ===");
        log.info("Parameters: username={}, permissionName={}", username, permissionName);
        
        try {
            // Find user
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                log.warn("User not found: username={}", username);
                log.info("=== UnifiedAclService.hasPermissionByName() END - USER NOT FOUND ===");
                return false;
            }
            
            User user = userOpt.get();
            Set<Group> userGroups = user.getGroups();
            
            if (userGroups == null || userGroups.isEmpty()) {
                log.warn("User {} has no groups assigned", username);
                log.info("=== UnifiedAclService.hasPermissionByName() END - NO GROUPS ===");
                return false;
            }
            
            log.info("User {} belongs to {} groups: {}", username, userGroups.size(),
                    userGroups.stream().map(Group::getName).collect(java.util.stream.Collectors.toList()));
            
            // Get all roles from all user's groups
            List<Role> userRoles = userGroups.stream()
                    .flatMap(group -> group.getRoles().stream())
                    .distinct()
                    .toList();
            
            if (userRoles.isEmpty()) {
                log.warn("User {} has no roles through groups", username);
                log.info("=== UnifiedAclService.hasPermissionByName() END - NO ROLES ===");
                return false;
            }
            
            log.info("User {} has {} roles: {}", username, userRoles.size(),
                    userRoles.stream().map(Role::getName).collect(java.util.stream.Collectors.toList()));
            
            // Find API permission by name
            Optional<ApiPermission> apiPermissionOpt = apiPermissionRepository.findSingleByName(permissionName);
            
            if (apiPermissionOpt.isEmpty()) {
                log.warn("API permission not found: permissionName={}", permissionName);
                log.info("=== UnifiedAclService.hasPermissionByName() END - PERMISSION NOT FOUND ===");
                return false;
            }
            
            ApiPermission apiPermission = apiPermissionOpt.get();
            log.info("Found API permission: id={}, name={}, isPublic={}, isCritical={}", 
                    apiPermission.getId(), apiPermission.getName(), apiPermission.isPublic(), apiPermission.isCritical());
            
            // Check if API is public
            if (apiPermission.isPublic()) {
                log.info("API {} is public, access granted", apiPermission.getName());
                log.info("=== UnifiedAclService.hasPermissionByName() END - PUBLIC ACCESS ===");
                return true;
            }
            
            // Check if any of user's roles has this permission
            for (Role role : userRoles) {
                boolean hasPermission = rolePermissionRepository.existsByRoleAndPermission(role, apiPermission);
                if (hasPermission) {
                    log.info("Permission found for user {} with role {}", username, role.getName());
                    log.info("=== UnifiedAclService.hasPermissionByName() END - PERMISSION GRANTED ===");
                    return true;
                }
            }
            
            log.warn("No permission found for user {} on any role for API {}", username, apiPermission.getName());
            log.info("=== UnifiedAclService.hasPermissionByName() END - PERMISSION DENIED ===");
            return false;
            
        } catch (Exception e) {
            log.error("=== UnifiedAclService.hasPermissionByName() END - ERROR ===");
            log.error("Error checking permission by name: username={}, permissionName={}, error={}", 
                    username, permissionName, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Register new API permission
     */
    public ApiPermission registerApiPermission(String name, String projectName, String apiPath, 
                                             String httpMethod, String description, String persianName, 
                                             boolean isCritical, boolean isPublic) {
        try {
            log.info("Registering API permission: {} for project: {}", name, projectName);

            Long projectId = projectRepository.findByName(projectName).get().getId();
            // Check if already exists
            Optional<ApiPermission> existing = apiPermissionRepository
                    .findByProjectAndApi(projectId, apiPath, httpMethod, name);
            
            if (existing.isPresent()) {
                log.info("API permission already exists, updating...");
                ApiPermission apiPermission = existing.get();
                apiPermission.setName(name);
                apiPermission.setDescription(description);
                apiPermission.setPersianName(persianName);
                apiPermission.setCritical(isCritical);
                apiPermission.setPublic(isPublic);
                return apiPermissionRepository.save(apiPermission);
            }
            
            // Find project by name
            Optional<Project> projectOpt = projectRepository.findByName(projectName);
            if (projectOpt.isEmpty()) {
                log.error("Project not found: {}", projectName);
                throw new RuntimeException("Project not found: " + projectName);
            }
            
            // Create new API permission
            ApiPermission apiPermission = new ApiPermission();
            apiPermission.setName(name);
            apiPermission.setProject(projectOpt.get());
            apiPermission.setApiPath(apiPath);
            apiPermission.setHttpMethod(httpMethod);
            apiPermission.setDescription(description);
            apiPermission.setPersianName(persianName);
            apiPermission.setCritical(isCritical);
            apiPermission.setPublic(isPublic);
            
            ApiPermission saved = apiPermissionRepository.save(apiPermission);
            log.info("API permission registered successfully: {}", saved.getName());
            return saved;
            
        } catch (Exception e) {
            log.error("Error registering API permission: {}", e.getMessage());
            throw new RuntimeException("Failed to register API permission", e);
        }
    }
    
    /**
     * Get all API permissions for a project
     */
    public List<ApiPermission> getProjectApiPermissions(String projectName) {
        log.info("Getting API permissions for project: {}", projectName);
        return apiPermissionRepository.findByProjectName(projectName);
    }
    
    /**
     * Get all public APIs
     */
    public List<ApiPermission> getPublicApis() {
        log.info("Getting all public APIs");
        return apiPermissionRepository.findPublicApis();
    }
    
    /**
     * Get all critical permissions
     */
    public List<ApiPermission> getCriticalPermissions() {
        log.info("Getting all critical permissions");
        return apiPermissionRepository.findCriticalPermissions();
    }
    
    /**
     * Get all API permissions
     */
    public List<ApiPermission> getAllApiPermissions() {
        log.info("Getting all API permissions");
        return apiPermissionRepository.findAll();
    }
}