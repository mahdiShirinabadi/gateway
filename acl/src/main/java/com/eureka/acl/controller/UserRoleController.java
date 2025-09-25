package com.eureka.acl.controller;

import com.eureka.acl.dto.UserRoleAssignRequest;
import com.eureka.acl.entity.UserRole;
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
 * User Role Management Controller
 * Handles user-role assignment and listing operations
 */
@RestController
@RequestMapping("/api/user-roles")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "User Role Management", description = "مدیریت نقش‌های کاربران")
public class UserRoleController {
    
    private final AclService aclService;
    
    @Operation(
            summary = "اختصاص نقش به کاربر",
            description = "یک نقش را به یک کاربر اختصاص می‌دهد"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "نقش با موفقیت به کاربر اختصاص داده شد",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserRole.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "اطلاعات ورودی نامعتبر"
            )
    })
    @PostMapping
    public ResponseEntity<UserRole> assignRoleToUser(
            @Parameter(description = "اطلاعات اختصاص نقش به کاربر", required = true)
            @RequestBody UserRoleAssignRequest request) {
        
        log.info("Assigning role {} to user {}", request.roleName(), request.username());
        
        UserRole userRole = aclService.assignRoleToUser(
                request.username(),
                request.roleName(),
                request.isPrimary()
        );
        
        log.info("Role assigned successfully to user");
        return ResponseEntity.ok(userRole);
    }
    
    @Operation(summary = "به‌روزرسانی نقش‌های کاربر", description = "تمام نقش‌های موجود کاربر را حذف کرده و نقش‌های جدید را ایجاد می‌کند")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "نقش‌های کاربر با موفقیت به‌روزرسانی شد",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserRole.class))),
            @ApiResponse(responseCode = "400", description = "درخواست نامعتبر", content = @Content),
            @ApiResponse(responseCode = "404", description = "کاربر یافت نشد", content = @Content),
            @ApiResponse(responseCode = "500", description = "خطای داخلی سرور", content = @Content)
    })
    @PutMapping("/user/{username}/roles")
    public ResponseEntity<List<UserRole>> updateUserRoles(
            @Parameter(description = "نام کاربری", required = true) @PathVariable String username,
            @Parameter(description = "لیست نام‌های نقش", required = true) @RequestBody List<String> roleNames) {
        
        log.info("Updating roles for user: {} with {} roles", username, roleNames.size());
        List<UserRole> userRoles = aclService.updateUserRoles(username, roleNames);
        log.info("Updated {} roles for user: {}", userRoles.size(), username);
        return ResponseEntity.ok(userRoles);
    }
    
    @Operation(
            summary = "لیست تمام اختصاصات کاربر-نقش",
            description = "لیست تمام اختصاصات کاربران و نقش‌ها را برمی‌گرداند"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "لیست اختصاصات با موفقیت دریافت شد",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserRole.class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<UserRole>> getAllUserRoles() {
        log.info("Getting all user roles");
        
        List<UserRole> userRoles = aclService.getAllUserRoles();
        
        log.info("Found {} user roles", userRoles.size());
        return ResponseEntity.ok(userRoles);
    }
    
    @Operation(
            summary = "لیست نقش‌های یک کاربر",
            description = "لیست تمام نقش‌های یک کاربر خاص را برمی‌گرداند"
    )
    @GetMapping("/user/{username}")
    public ResponseEntity<List<UserRole>> getUserRoles(
            @Parameter(description = "نام کاربری", required = true)
            @PathVariable String username) {
        
        log.info("Getting roles for user: {}", username);
        
        List<UserRole> userRoles = aclService.getUserRolesByUser(username);
        
        log.info("Found {} roles for user: {}", userRoles.size(), username);
        return ResponseEntity.ok(userRoles);
    }
    
    @Operation(
            summary = "لیست کاربران یک نقش",
            description = "لیست تمام کاربرانی که یک نقش خاص دارند را برمی‌گرداند"
    )
    @GetMapping("/role/{roleName}")
    public ResponseEntity<List<UserRole>> getRoleUsers(
            @Parameter(description = "نام نقش", required = true)
            @PathVariable String roleName) {
        
        log.info("Getting users for role: {}", roleName);
        
        List<UserRole> userRoles = aclService.getUserRolesByRole(roleName);
        
        log.info("Found {} users for role: {}", userRoles.size(), roleName);
        return ResponseEntity.ok(userRoles);
    }
    
    @Operation(
            summary = "حذف نقش از کاربر",
            description = "یک نقش را از یک کاربر حذف می‌کند"
    )
    @DeleteMapping("/user/{username}/role/{roleName}")
    public ResponseEntity<String> removeRoleFromUser(
            @Parameter(description = "نام کاربری", required = true)
            @PathVariable String username,
            @Parameter(description = "نام نقش", required = true)
            @PathVariable String roleName) {
        
        log.info("Removing role {} from user {}", roleName, username);
        
        boolean success = aclService.removeRoleFromUser(username, roleName);
        
        if (success) {
            log.info("Role removed successfully from user");
            return ResponseEntity.ok("Role removed successfully from user");
        } else {
            log.warn("Failed to remove role from user");
            return ResponseEntity.badRequest().body("Failed to remove role from user");
        }
    }
    
    @Operation(
            summary = "بررسی وجود نقش در کاربر",
            description = "بررسی می‌کند که آیا کاربر نقش خاصی دارد یا خیر"
    )
    @GetMapping("/user/{username}/has-role/{roleName}")
    public ResponseEntity<Boolean> userHasRole(
            @Parameter(description = "نام کاربری", required = true)
            @PathVariable String username,
            @Parameter(description = "نام نقش", required = true)
            @PathVariable String roleName) {
        
        log.info("Checking if user {} has role {}", username, roleName);
        
        boolean hasRole = aclService.userHasRole(username, roleName);
        
        log.info("User {} has role {}: {}", username, roleName, hasRole);
        return ResponseEntity.ok(hasRole);
    }
    
}
