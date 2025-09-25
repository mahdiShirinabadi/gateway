package com.eureka.acl.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for user creation request
 */
@Schema(description = "اطلاعات کاربر جدید")
public record UserCreateRequest(
    @Schema(description = "نام کاربری", example = "john_doe")
    String username,
    
    @Schema(description = "ایمیل کاربر", example = "john.doe@example.com")
    String email,
    
    @Schema(description = "نام کامل کاربر", example = "John Doe")
    String fullName
) {}
