package com.eureka.acl.controller;

import com.eureka.acl.entity.ApiPermission;
import com.eureka.acl.entity.Role;
import com.eureka.acl.service.AclService;
import com.eureka.acl.service.UnifiedAclService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unified ACL Controller
 * Handles both permission checking and API registration with multiple roles support
 */
@RestController
@RequestMapping("/api/acl")
@RequiredArgsConstructor
@Log4j2
public class UnifiedAclController {
    
    private final UnifiedAclService unifiedAclService;
    private final AclService aclService;
    
    /**
     * Check permission for user
     */
    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkPermission(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String projectName = request.get("projectName");
        String apiPath = request.get("apiPath");
        String httpMethod = request.get("httpMethod");
        String permissionName = request.get("resource");

        log.info("Permission check request: user={}, project={}, api={}, method={}", 
                username, projectName, apiPath, httpMethod);
        
        Map<String, Object> response = new HashMap<>();
        
        if (username == null || projectName == null || apiPath == null || httpMethod == null) {
            response.put("hasPermission", false);
            response.put("message", "Missing required parameters");
            return ResponseEntity.badRequest().body(response);
        }
        
        boolean hasPermission = unifiedAclService.hasPermission(username, projectName, apiPath, httpMethod, permissionName);
        response.put("hasPermission", hasPermission);
        response.put("message", hasPermission ? "Access granted" : "Access denied");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Register new API permission
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerApiPermission(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String projectName = (String) request.get("projectName");
        String apiPath = (String) request.get("apiPath");
        String httpMethod = (String) request.get("httpMethod");
        String description = (String) request.get("description");
        String persianName = (String) request.get("persianName");
        Boolean isCritical = (Boolean) request.getOrDefault("isCritical", false);
        Boolean isPublic = (Boolean) request.getOrDefault("isPublic", false);
        
        log.info("API permission registration request: name={}, project={}, api={}, method={}", 
                name, projectName, apiPath, httpMethod);
        
        Map<String, Object> response = new HashMap<>();
        
        if (name == null || projectName == null || apiPath == null || httpMethod == null) {
            response.put("success", false);
            response.put("message", "Missing required parameters");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            ApiPermission apiPermission = unifiedAclService.registerApiPermission(
                    name, projectName, apiPath, httpMethod, 
                    description != null ? description : "", 
                    persianName != null ? persianName : name,
                    isCritical, isPublic);
            
            response.put("success", true);
            response.put("message", "API permission registered successfully");
            response.put("apiPermission", apiPermission);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error registering API permission: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to register API permission: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get user permissions (from all roles)
     */
    @GetMapping("/user/{username}/permissions")
    public ResponseEntity<List<ApiPermission>> getUserPermissions(@PathVariable String username) {
        log.info("Getting permissions for user: {}", username);
        List<ApiPermission> permissions = aclService.getUserPermissions(username);
        return ResponseEntity.ok(permissions);
    }
    
    /**
     * Get user roles
     */
    @GetMapping("/user/{username}/roles")
    public ResponseEntity<List<Role>> getUserRoles(@PathVariable String username) {
        log.info("Getting roles for user: {}", username);
        List<Role> roles = aclService.getUserRoles(username);
        return ResponseEntity.ok(roles);
    }
    
    /**
     * Check if user has specific role
     */
    @GetMapping("/user/{username}/has-role/{roleName}")
    public ResponseEntity<Map<String, Object>> userHasRole(@PathVariable String username, @PathVariable String roleName) {
        log.info("Checking if user {} has role {}", username, roleName);
        boolean hasRole = aclService.userHasRole(username, roleName);
        
        Map<String, Object> response = new HashMap<>();
        response.put("hasRole", hasRole);
        response.put("message", hasRole ? "User has role" : "User does not have role");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Assign role to user
     */
    @PostMapping("/user/{username}/assign-role/{roleName}")
    public ResponseEntity<Map<String, Object>> assignRoleToUser(
            @PathVariable String username, 
            @PathVariable String roleName,
            @RequestParam(defaultValue = "false") boolean isPrimary) {
        
        log.info("Assigning role {} to user {} (primary: {})", roleName, username, isPrimary);
        
        Map<String, Object> response = new HashMap<>();
        boolean success = aclService.assignRoleToUser(username, roleName, isPrimary);
        
        response.put("success", success);
        response.put("message", success ? "Role assigned successfully" : "Failed to assign role");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Remove role from user
     */
    @DeleteMapping("/user/{username}/remove-role/{roleName}")
    public ResponseEntity<Map<String, Object>> removeRoleFromUser(
            @PathVariable String username, 
            @PathVariable String roleName) {
        
        log.info("Removing role {} from user {}", roleName, username);
        
        Map<String, Object> response = new HashMap<>();
        boolean success = aclService.removeRoleFromUser(username, roleName);
        
        response.put("success", success);
        response.put("message", success ? "Role removed successfully" : "Failed to remove role");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get project API permissions
     */
    @GetMapping("/project/{projectName}/apis")
    public ResponseEntity<List<ApiPermission>> getProjectApiPermissions(@PathVariable String projectName) {
        log.info("Getting API permissions for project: {}", projectName);
        List<ApiPermission> apiPermissions = unifiedAclService.getProjectApiPermissions(projectName);
        return ResponseEntity.ok(apiPermissions);
    }
    
    /**
     * Get public APIs
     */
    @GetMapping("/public-apis")
    public ResponseEntity<List<ApiPermission>> getPublicApis() {
        log.info("Getting all public APIs");
        List<ApiPermission> publicApis = unifiedAclService.getPublicApis();
        return ResponseEntity.ok(publicApis);
    }
    
    /**
     * Get critical permissions
     */
    @GetMapping("/critical-permissions")
    public ResponseEntity<List<ApiPermission>> getCriticalPermissions() {
        log.info("Getting all critical permissions");
        List<ApiPermission> criticalPermissions = unifiedAclService.getCriticalPermissions();
        return ResponseEntity.ok(criticalPermissions);
    }
}