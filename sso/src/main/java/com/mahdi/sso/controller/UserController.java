package com.mahdi.sso.controller;

import com.mahdi.sso.dto.CreateUserRequest;
import com.mahdi.sso.dto.CreateUserResponse;
import com.mahdi.sso.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for user management operations
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for user management operations")
public class UserController {
    
    private final UserService userService;
    
    /**
     * Create a new user
     */
    @PostMapping("/create")
    @Operation(summary = "Create a new user", description = "Creates a new user with username and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or user already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CreateUserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Received request to create user: {}", request.getUsername());
        
        CreateUserResponse response = userService.createUser(request);
        
        if (response.isSuccess()) {
            log.info("User created successfully: {}", response.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            log.warn("Failed to create user: {}", response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Check if user exists
     */
    @GetMapping("/exists/{username}")
    @Operation(summary = "Check if user exists", description = "Checks if a user with the given username exists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User existence checked successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Boolean> userExists(@PathVariable String username) {
        log.info("Checking if user exists: {}", username);
        
        boolean exists = userService.userExists(username);
        log.info("User '{}' exists: {}", username, exists);
        
        return ResponseEntity.ok(exists);
    }
}
