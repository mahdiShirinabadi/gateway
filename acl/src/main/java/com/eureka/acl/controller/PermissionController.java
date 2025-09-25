package com.eureka.acl.controller;

import com.eureka.acl.dto.PermissionCreateRequest;
import com.eureka.acl.entity.ApiPermission;
import com.eureka.acl.service.UnifiedAclService;
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
 * Permission Management Controller
 * Handles API permission creation and listing operations
 */
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "Permission Management", description = "مدیریت مجوزها")
public class PermissionController {
    
    private final UnifiedAclService unifiedAclService;
    
    @Operation(
            summary = "ایجاد مجوز جدید",
            description = "یک مجوز API جدید را در سیستم ثبت می‌کند"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "مجوز با موفقیت ایجاد شد",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiPermission.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "اطلاعات ورودی نامعتبر"
            )
    })
    @PostMapping
    public ResponseEntity<ApiPermission> createPermission(
            @Parameter(description = "اطلاعات مجوز جدید", required = true)
            @RequestBody PermissionCreateRequest request) {
        
        log.info("Creating new permission: {} for project: {}", request.name(), request.projectName());
        
        ApiPermission permission = unifiedAclService.registerApiPermission(
                request.name(),
                request.projectName(),
                request.apiPath(),
                request.httpMethod(),
                request.description(),
                request.persianName(),
                request.isCritical(),
                request.isPublic()
        );
        
        log.info("Permission created successfully: {}", permission.getName());
        return ResponseEntity.ok(permission);
    }
    
    @Operation(
            summary = "لیست تمام مجوزها",
            description = "لیست تمام مجوزهای ثبت شده در سیستم را برمی‌گرداند"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "لیست مجوزها با موفقیت دریافت شد",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiPermission.class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<ApiPermission>> getAllPermissions() {
        log.info("Getting all permissions");
        
        List<ApiPermission> permissions = unifiedAclService.getAllApiPermissions();
        
        log.info("Found {} permissions", permissions.size());
        return ResponseEntity.ok(permissions);
    }
    
    @Operation(
            summary = "لیست مجوزهای پروژه",
            description = "لیست مجوزهای یک پروژه خاص را برمی‌گرداند"
    )
    @GetMapping("/project/{projectName}")
    public ResponseEntity<List<ApiPermission>> getProjectPermissions(
            @Parameter(description = "نام پروژه", required = true)
            @PathVariable String projectName) {
        
        log.info("Getting permissions for project: {}", projectName);
        
        List<ApiPermission> permissions = unifiedAclService.getProjectApiPermissions(projectName);
        
        log.info("Found {} permissions for project: {}", permissions.size(), projectName);
        return ResponseEntity.ok(permissions);
    }
    
    @Operation(
            summary = "لیست مجوزهای عمومی",
            description = "لیست مجوزهای عمومی (بدون نیاز به احراز هویت) را برمی‌گرداند"
    )
    @GetMapping("/public")
    public ResponseEntity<List<ApiPermission>> getPublicPermissions() {
        log.info("Getting public permissions");
        
        List<ApiPermission> permissions = unifiedAclService.getPublicApis();
        
        log.info("Found {} public permissions", permissions.size());
        return ResponseEntity.ok(permissions);
    }
    
    @Operation(
            summary = "لیست مجوزهای حساس",
            description = "لیست مجوزهای حساس (Critical) را برمی‌گرداند"
    )
    @GetMapping("/critical")
    public ResponseEntity<List<ApiPermission>> getCriticalPermissions() {
        log.info("Getting critical permissions");
        
        List<ApiPermission> permissions = unifiedAclService.getCriticalPermissions();
        
        log.info("Found {} critical permissions", permissions.size());
        return ResponseEntity.ok(permissions);
    }
    
}
