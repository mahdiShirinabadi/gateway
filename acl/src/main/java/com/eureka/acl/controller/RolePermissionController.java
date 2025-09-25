package com.eureka.acl.controller;

import com.eureka.acl.dto.RolePermissionAssignRequest;
import com.eureka.acl.entity.RolePermission;
import com.eureka.acl.service.AclService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Role Permission Management Controller
 * Handles role-permission assignment and listing operations
 */
@RestController
@RequestMapping("/api/role-permissions")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "Role Permission Management", description = "مدیریت مجوزهای نقش‌ها")
public class RolePermissionController {
    
    private final AclService aclService;
    
    @Operation(
            summary = "اختصاص مجوز به نقش",
            description = "یک مجوز را به یک نقش اختصاص می‌دهد"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "مجوز با موفقیت به نقش اختصاص داده شد",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RolePermission.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "اطلاعات ورودی نامعتبر"
            )
    })
    @PostMapping
    public ResponseEntity<RolePermission> assignPermissionToRole(
            @Parameter(description = "اطلاعات اختصاص مجوز به نقش", required = true)
            @RequestBody RolePermissionAssignRequest request) {
        
        log.info("Assigning permission {} to role {}", request.permissionName(), request.roleName());
        
        RolePermission rolePermission = aclService.assignPermissionToRole(
                request.roleName(),
                request.permissionName()
        );
        
        log.info("Permission assigned successfully to role");
        return ResponseEntity.ok(rolePermission);
    }
    
    @Operation(summary = "به‌روزرسانی دسترسی‌های نقش", description = "تمام دسترسی‌های موجود نقش را حذف کرده و دسترسی‌های جدید را ایجاد می‌کند")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "دسترسی‌های نقش با موفقیت به‌روزرسانی شد",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RolePermission.class))),
            @ApiResponse(responseCode = "400", description = "درخواست نامعتبر", content = @Content),
            @ApiResponse(responseCode = "404", description = "نقش یافت نشد", content = @Content),
            @ApiResponse(responseCode = "500", description = "خطای داخلی سرور", content = @Content)
    })
    @PutMapping("/role/{roleName}/permissions")
    public ResponseEntity<List<RolePermission>> updateRolePermissions(
            @Parameter(description = "نام نقش", required = true) @PathVariable String roleName,
            @Parameter(description = "لیست نام‌های دسترسی", required = true) @RequestBody List<String> permissionNames) {
        
        log.info("Updating permissions for role: {} with {} permissions", roleName, permissionNames.size());
        List<RolePermission> rolePermissions = aclService.updateRolePermissions(roleName, permissionNames);
        log.info("Updated {} permissions for role: {}", rolePermissions.size(), roleName);
        return ResponseEntity.ok(rolePermissions);
    }
    
    @Operation(
            summary = "لیست تمام اختصاصات نقش-مجوز",
            description = "لیست تمام اختصاصات نقش‌ها و مجوزها را برمی‌گرداند"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "لیست اختصاصات با موفقیت دریافت شد",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RolePermission.class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<RolePermission>> getAllRolePermissions() {
        log.info("Getting all role permissions");
        
        List<RolePermission> rolePermissions = aclService.getAllRolePermissions();
        
        log.info("Found {} role permissions", rolePermissions.size());
        return ResponseEntity.ok(rolePermissions);
    }
    
    @Operation(
            summary = "لیست مجوزهای یک نقش",
            description = "لیست تمام مجوزهای یک نقش خاص را برمی‌گرداند"
    )
    @GetMapping("/role/{roleName}")
    public ResponseEntity<List<RolePermission>> getRolePermissions(
            @Parameter(description = "نام نقش", required = true)
            @PathVariable String roleName) {
        
        log.info("Getting permissions for role: {}", roleName);
        
        List<RolePermission> rolePermissions = aclService.getRolePermissionsByRole(roleName);
        
        log.info("Found {} permissions for role: {}", rolePermissions.size(), roleName);
        return ResponseEntity.ok(rolePermissions);
    }
    
    @Operation(
            summary = "لیست نقش‌های یک مجوز",
            description = "لیست تمام نقش‌هایی که یک مجوز خاص دارند را برمی‌گرداند"
    )
    @GetMapping("/permission/{permissionName}")
    public ResponseEntity<List<RolePermission>> getPermissionRoles(
            @Parameter(description = "نام مجوز", required = true)
            @PathVariable String permissionName) {
        
        log.info("Getting roles for permission: {}", permissionName);
        
        List<RolePermission> rolePermissions = aclService.getRolePermissionsByPermission(permissionName);
        
        log.info("Found {} roles for permission: {}", rolePermissions.size(), permissionName);
        return ResponseEntity.ok(rolePermissions);
    }
    
    @Operation(
            summary = "حذف مجوز از نقش",
            description = "یک مجوز را از یک نقش حذف می‌کند"
    )
    @DeleteMapping("/role/{roleName}/permission/{permissionName}")
    public ResponseEntity<String> removePermissionFromRole(
            @Parameter(description = "نام نقش", required = true)
            @PathVariable String roleName,
            @Parameter(description = "نام مجوز", required = true)
            @PathVariable String permissionName) {
        
        log.info("Removing permission {} from role {}", permissionName, roleName);
        
        boolean success = aclService.removePermissionFromRole(roleName, permissionName);
        
        if (success) {
            log.info("Permission removed successfully from role");
            return ResponseEntity.ok("Permission removed successfully from role");
        } else {
            log.warn("Failed to remove permission from role");
            return ResponseEntity.badRequest().body("Failed to remove permission from role");
        }
    }
    
}
