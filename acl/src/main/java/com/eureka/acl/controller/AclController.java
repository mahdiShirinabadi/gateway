package com.eureka.acl.controller;

import com.eureka.acl.entity.Permission;
import com.eureka.acl.service.AclService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/acl")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "ACL Management", description = "مدیریت دسترسی‌ها و نقش‌ها")
public class AclController {
    
    private final AclService aclService;

    @Operation(
            summary = "بررسی دسترسی کاربر",
            description = "بررسی می‌کند که آیا کاربر مشخص شده دسترسی مورد نظر را دارد یا خیر"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "نتیجه بررسی دسترسی",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = "{\"username\": \"john_doe\", \"permission\": \"READ_USERS\", \"allowed\": true}"
                            )
                    )
            )
    })
    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkPermission(
            @Parameter(description = "اطلاعات درخواست بررسی دسترسی", required = true)
            @RequestBody PermissionRequest request) {
        log.info("=== ACL PERMISSION CHECK ===");
        log.info("User: {}", request.getUsername());
        log.info("Permission: {}", request.getPermissionName());
        log.info("Project: {}", request.getProjectName());
        log.info("Error Source: ACL SERVICE");
        log.info("=============================");
        
        boolean hasPermission = aclService.hasPermission(
                request.getUsername(), 
                request.getPermissionName(),
                request.getProjectName()
        );
        
        Map<String, Object> response = Map.of(
                "username", request.getUsername(),
                "permission", request.getPermissionName(),
                "allowed", hasPermission
        );
        
        if (hasPermission) {
            log.info("=== ACL PERMISSION GRANTED ===");
            log.info("User: {}", request.getUsername());
            log.info("Permission: {}", request.getPermissionName());
            log.info("=============================");
        } else {
            log.warn("=== ACL PERMISSION DENIED ===");
            log.warn("User: {}", request.getUsername());
            log.warn("Permission: {}", request.getPermissionName());
            log.warn("Project: {}", request.getProjectName());
            log.warn("Error Source: ACL SERVICE");
            log.warn("Status: 403 FORBIDDEN");
            log.warn("=============================");
        }
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "دریافت دسترسی‌های کاربر",
            description = "لیست تمام دسترسی‌های کاربر مشخص شده را برمی‌گرداند"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "لیست دسترسی‌های کاربر",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Permission.class)
                    )
            )
    })
    @GetMapping("/permissions/{username}")
    public ResponseEntity<List<Permission>> getUserPermissions(
            @Parameter(description = "نام کاربری", required = true, example = "john_doe")
            @PathVariable String username) {
        log.info("Getting permissions for user: {}", username);
        List<Permission> permissions = aclService.getUserPermissions(username);
        return ResponseEntity.ok(permissions);
    }
    
    @Operation(
            summary = "دریافت دسترسی‌های کاربر به صورت رشته",
            description = "لیست دسترسی‌های کاربر را به صورت آرایه‌ای از رشته‌ها برمی‌گرداند"
    )
    @GetMapping("/user-permissions")
    public ResponseEntity<Map<String, Object>> getUserPermissionsAsStrings(
            @Parameter(description = "نام کاربری", required = true, example = "john_doe")
            @RequestParam String username) {
        log.info("Getting permissions as strings for user: {}", username);
        List<Permission> permissions = aclService.getUserPermissions(username);
        
        // Convert Permission objects to permission names
        List<String> permissionNames = permissions.stream()
                .map(Permission::getName)
                .collect(Collectors.toList());
        
        Map<String, Object> response = Map.of(
                "username", username,
                "permissions", permissionNames
        );
        
        log.info("Retrieved {} permissions for user: {}", permissionNames.size(), username);
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "افزودن نقش جدید",
            description = "یک نقش جدید به سیستم اضافه می‌کند"
    )
    @PostMapping("/roles")
    public ResponseEntity<String> addRole(
            @Parameter(description = "اطلاعات نقش جدید", required = true)
            @RequestBody RoleRequest request) {
        log.info("Adding role: {}", request.getName());
        aclService.addRole(request.getName());
        return ResponseEntity.ok("Role added successfully");
    }
    
    @Operation(
            summary = "افزودن دسترسی جدید",
            description = "یک دسترسی جدید به سیستم اضافه می‌کند"
    )
    @PostMapping("/permissions")
    public ResponseEntity<String> addPermission(
            @Parameter(description = "اطلاعات دسترسی جدید", required = true)
            @RequestBody PermissionCreateRequest request) {
        log.info("Adding permission: {}", request.getName());
        aclService.addPermission(
                request.getName(),
                request.getProjectName(),
                request.isCritical(),
                request.getPersianName()
        );
        return ResponseEntity.ok("Permission added successfully");
    }
    
    @Operation(
            summary = "اختصاص دسترسی به نقش",
            description = "یک دسترسی مشخص را به یک نقش اختصاص می‌دهد"
    )
    @PostMapping("/assign-permission")
    public ResponseEntity<String> assignPermissionToRole(
            @Parameter(description = "اطلاعات اختصاص دسترسی", required = true)
            @RequestBody PermissionAssignmentRequest request) {
        log.info("Assigning permission {} to role {}", request.getPermissionName(), request.getRoleName());
        aclService.assignPermissionToRole(request.getRoleName(), request.getPermissionName());
        return ResponseEntity.ok("Permission assigned to role successfully");
    }
    
    @Operation(
            summary = "اختصاص نقش به کاربر",
            description = "یک نقش مشخص را به یک کاربر اختصاص می‌دهد"
    )
    @PostMapping("/assign-role")
    public ResponseEntity<String> assignRoleToUser(
            @Parameter(description = "اطلاعات اختصاص نقش", required = true)
            @RequestBody UserRoleAssignmentRequest request) {
        log.info("Assigning role {} to user {}", request.getRoleName(), request.getUsername());
        aclService.assignRoleToUser(request.getUsername(), request.getRoleName());
        return ResponseEntity.ok("Role assigned to user successfully");
    }
    
    @Operation(
            summary = "بررسی وضعیت سرویس",
            description = "بررسی می‌کند که سرویس ACL در حال اجرا است یا خیر"
    )
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ACL Service is running");
    }

    @PostMapping("/permission-changed")
    public ResponseEntity<Map<String, Object>> permissionChanged(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        
        log.info("=== ACL PERMISSION CHANGE NOTIFICATION ===");
        log.info("User: {}", username);
        log.info("=============================");


        Map<String, Object> response = Map.of(
                "success", true,
                "message", "Permission change notification sent",
                "username", username != null ? username : "all users"
        );

        return ResponseEntity.ok(response);
    }
    
    // Request classes
    @Getter
    @Setter
    public static class PermissionRequest {
        private String username;
        private String projectName;
        private String permissionName;
    }

    @Getter
    @Setter
    public static class RoleRequest {
        private String name;
    }

    @Setter
    @Getter
    public static class PermissionCreateRequest {
        private String name;
        private String projectName;
        private boolean critical;
        private String persianName;
    }

    @Getter
    @Setter
    public static class PermissionAssignmentRequest {
        private String roleName;
        private String permissionName;
    }

    @Getter
    @Setter
    public static class UserRoleAssignmentRequest {
        private String username;
        private String roleName;
    }
} 