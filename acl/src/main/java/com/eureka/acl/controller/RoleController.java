package com.eureka.acl.controller;

import com.eureka.acl.dto.RoleCreateRequest;
import com.eureka.acl.entity.Role;
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
 * Role Management Controller
 * Handles role creation and listing operations
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "Role Management", description = "مدیریت نقش‌ها")
public class RoleController {
    
    private final AclService aclService;
    
    @Operation(
            summary = "ایجاد نقش جدید",
            description = "یک نقش جدید را در سیستم ثبت می‌کند"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "نقش با موفقیت ایجاد شد",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Role.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "اطلاعات ورودی نامعتبر"
            )
    })
    @PostMapping
    public ResponseEntity<Role> createRole(
            @Parameter(description = "اطلاعات نقش جدید", required = true)
            @RequestBody RoleCreateRequest request) {
        
        log.info("Creating new role: {}", request.name());
        
        Role role = aclService.createRole(
                request.name(),
                request.description()
        );
        
        log.info("Role created successfully: {}", role.getName());
        return ResponseEntity.ok(role);
    }
    
    @Operation(
            summary = "لیست تمام نقش‌ها",
            description = "لیست تمام نقش‌های ثبت شده در سیستم را برمی‌گرداند"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "لیست نقش‌ها با موفقیت دریافت شد",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Role.class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        log.info("Getting all roles");
        
        List<Role> roles = aclService.getAllRoles();
        
        log.info("Found {} roles", roles.size());
        return ResponseEntity.ok(roles);
    }
    
    @Operation(
            summary = "دریافت نقش بر اساس نام",
            description = "نقشی را بر اساس نام آن جستجو می‌کند"
    )
    @GetMapping("/{name}")
    public ResponseEntity<Role> getRoleByName(
            @Parameter(description = "نام نقش", required = true)
            @PathVariable String name) {
        
        log.info("Getting role by name: {}", name);
        
        return aclService.getRoleByName(name)
                .map(role -> {
                    log.info("Role found: {}", role.getName());
                    return ResponseEntity.ok(role);
                })
                .orElseGet(() -> {
                    log.warn("Role not found: {}", name);
                    return ResponseEntity.notFound().build();
                });
    }
    
    @Operation(
            summary = "دریافت مجوزهای نقش",
            description = "لیست تمام مجوزهای یک نقش خاص را برمی‌گرداند"
    )
    @GetMapping("/{name}/permissions")
    public ResponseEntity<List<com.eureka.acl.entity.ApiPermission>> getRolePermissions(
            @Parameter(description = "نام نقش", required = true)
            @PathVariable String name) {
        
        log.info("Getting permissions for role: {}", name);
        
        List<com.eureka.acl.entity.ApiPermission> permissions = aclService.getRolePermissions(name);
        
        log.info("Found {} permissions for role: {}", permissions.size(), name);
        return ResponseEntity.ok(permissions);
    }
    
}
