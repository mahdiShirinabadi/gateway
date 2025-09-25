package com.eureka.acl.controller;

import com.eureka.acl.dto.UserCreateRequest;
import com.eureka.acl.entity.User;
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
 * User Management Controller
 * Handles user creation and listing operations
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "User Management", description = "مدیریت کاربران")
public class UserController {
    
    private final AclService aclService;
    
    @Operation(
            summary = "ایجاد کاربر جدید",
            description = "یک کاربر جدید را در سیستم ثبت می‌کند"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "کاربر با موفقیت ایجاد شد",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "اطلاعات ورودی نامعتبر"
            )
    })
    @PostMapping
    public ResponseEntity<User> createUser(
            @Parameter(description = "اطلاعات کاربر جدید", required = true)
            @RequestBody UserCreateRequest request) {
        
        log.info("Creating new user: {}", request.username());
        
        User user = aclService.createUser(
                request.username(),
                request.email(),
                request.fullName()
        );
        
        log.info("User created successfully: {}", user.getUsername());
        return ResponseEntity.ok(user);
    }
    
    @Operation(
            summary = "لیست تمام کاربران",
            description = "لیست تمام کاربران ثبت شده در سیستم را برمی‌گرداند"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "لیست کاربران با موفقیت دریافت شد",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Getting all users");
        
        List<User> users = aclService.getAllUsers();
        
        log.info("Found {} users", users.size());
        return ResponseEntity.ok(users);
    }
    
    @Operation(
            summary = "دریافت کاربر بر اساس نام کاربری",
            description = "کاربری را بر اساس نام کاربری آن جستجو می‌کند"
    )
    @GetMapping("/{username}")
    public ResponseEntity<User> getUserByUsername(
            @Parameter(description = "نام کاربری", required = true)
            @PathVariable String username) {
        
        log.info("Getting user by username: {}", username);
        
        return aclService.getUserByUsername(username)
                .map(user -> {
                    log.info("User found: {}", user.getUsername());
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> {
                    log.warn("User not found: {}", username);
                    return ResponseEntity.notFound().build();
                });
    }
    
    @Operation(
            summary = "دریافت مجوزهای کاربر",
            description = "لیست تمام مجوزهای یک کاربر خاص را برمی‌گرداند"
    )
    @GetMapping("/{username}/permissions")
    public ResponseEntity<List<com.eureka.acl.entity.ApiPermission>> getUserPermissions(
            @Parameter(description = "نام کاربری", required = true)
            @PathVariable String username) {
        
        log.info("Getting permissions for user: {}", username);
        
        List<com.eureka.acl.entity.ApiPermission> permissions = aclService.getUserPermissions(username);
        
        log.info("Found {} permissions for user: {}", permissions.size(), username);
        return ResponseEntity.ok(permissions);
    }
    
    @Operation(
            summary = "دریافت نقش‌های کاربر",
            description = "لیست تمام نقش‌های یک کاربر خاص را برمی‌گرداند"
    )
    @GetMapping("/{username}/roles")
    public ResponseEntity<List<com.eureka.acl.entity.Role>> getUserRoles(
            @Parameter(description = "نام کاربری", required = true)
            @PathVariable String username) {
        
        log.info("Getting roles for user: {}", username);
        
        List<com.eureka.acl.entity.Role> roles = aclService.getUserRoles(username);
        
        log.info("Found {} roles for user: {}", roles.size(), username);
        return ResponseEntity.ok(roles);
    }
    
}
