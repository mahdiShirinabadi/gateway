package com.eureka.acl.controller;

import com.eureka.acl.entity.Permission;
import com.eureka.acl.service.AclService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/acl")
@RequiredArgsConstructor
@Log4j2
public class AclController {
    
    private final AclService aclService;
    
    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkPermission(@RequestBody PermissionRequest request) {
        log.info("Permission check request for user: {}, permission: {}", 
                request.getUsername(), request.getPermissionName());
        
        boolean hasPermission = aclService.hasPermission(
                request.getUsername(), 
                request.getPermissionName()
        );
        
        Map<String, Object> response = Map.of(
                "username", request.getUsername(),
                "permission", request.getPermissionName(),
                "allowed", hasPermission
        );
        
        log.info("Permission check result: {}", hasPermission);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/permissions/{username}")
    public ResponseEntity<List<Permission>> getUserPermissions(@PathVariable String username) {
        log.info("Getting permissions for user: {}", username);
        List<Permission> permissions = aclService.getUserPermissions(username);
        return ResponseEntity.ok(permissions);
    }
    
    @PostMapping("/roles")
    public ResponseEntity<String> addRole(@RequestBody RoleRequest request) {
        log.info("Adding role: {}", request.getName());
        aclService.addRole(request.getName());
        return ResponseEntity.ok("Role added successfully");
    }
    
    @PostMapping("/permissions")
    public ResponseEntity<String> addPermission(@RequestBody PermissionCreateRequest request) {
        log.info("Adding permission: {}", request.getName());
        aclService.addPermission(
                request.getName(),
                request.getProjectName(),
                request.isCritical(),
                request.getPersianName()
        );
        return ResponseEntity.ok("Permission added successfully");
    }
    
    @PostMapping("/assign-permission")
    public ResponseEntity<String> assignPermissionToRole(@RequestBody PermissionAssignmentRequest request) {
        log.info("Assigning permission {} to role {}", request.getPermissionName(), request.getRoleName());
        aclService.assignPermissionToRole(request.getRoleName(), request.getPermissionName());
        return ResponseEntity.ok("Permission assigned to role successfully");
    }
    
    @PostMapping("/assign-role")
    public ResponseEntity<String> assignRoleToUser(@RequestBody UserRoleAssignmentRequest request) {
        log.info("Assigning role {} to user {}", request.getRoleName(), request.getUsername());
        aclService.assignRoleToUser(request.getUsername(), request.getRoleName());
        return ResponseEntity.ok("Role assigned to user successfully");
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ACL Service is running");
    }
    
    // Request classes
    public static class PermissionRequest {
        private String username;
        private String permissionName;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPermissionName() { return permissionName; }
        public void setPermissionName(String permissionName) { this.permissionName = permissionName; }
    }
    
    public static class RoleRequest {
        private String name;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
    
    public static class PermissionCreateRequest {
        private String name;
        private String projectName;
        private boolean critical;
        private String persianName;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        
        public boolean isCritical() { return critical; }
        public void setCritical(boolean critical) { this.critical = critical; }
        
        public String getPersianName() { return persianName; }
        public void setPersianName(String persianName) { this.persianName = persianName; }
    }
    
    public static class PermissionAssignmentRequest {
        private String roleName;
        private String permissionName;
        
        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
        
        public String getPermissionName() { return permissionName; }
        public void setPermissionName(String permissionName) { this.permissionName = permissionName; }
    }
    
    public static class UserRoleAssignmentRequest {
        private String username;
        private String roleName;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
    }
} 