package com.eureka.acl.service;

import com.eureka.acl.entity.ApiPermission;
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
        try {
            log.info("Checking permission for user: {} project: {} api: {} method: {}, permission {}",
                    username, projectName, apiPath, httpMethod, permissionName);
            
            // Find user
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                log.warn("User {} not found", username);
                return false;
            }
            
            User user = userOpt.get();
            Set<Role> userRoles = user.getRoles();
            
            if (userRoles == null || userRoles.isEmpty()) {
                log.warn("User {} has no roles assigned", username);
                return false;
            }

            Long projectId = projectRepository.findByName(projectName).get().getId();
            // Find API permission
            Optional<ApiPermission> apiPermissionOpt = apiPermissionRepository
                    .findByProjectAndApi(projectId, apiPath, httpMethod, permissionName);
            
            if (apiPermissionOpt.isEmpty()) {
                log.warn("API permission not found for project: {} api: {} method: {}", 
                        projectName, apiPath, httpMethod);
                return false;
            }
            
            ApiPermission apiPermission = apiPermissionOpt.get();
            
            // Check if API is public
            if (apiPermission.isPublic()) {
                log.info("API {} is public, access granted", apiPermission.getName());
                return true;
            }
            
            // Check if any of user's roles has this permission
            for (Role role : userRoles) {
                boolean hasPermission = rolePermissionRepository.existsByRoleAndPermission(role, apiPermission);
                if (hasPermission) {
                    log.info("Permission found for user {} with role {}", username, role.getName());
                    return true;
                }
            }
            
            log.warn("No permission found for user {} on any role for API {}", username, apiPermission.getName());
            return false;
            
        } catch (Exception e) {
            log.error("Error checking permission: {}", e.getMessage());
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
}