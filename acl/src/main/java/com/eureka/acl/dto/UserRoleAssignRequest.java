package com.eureka.acl.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for user role assignment request
 */
@Schema(description = "اطلاعات اختصاص نقش به کاربر")
public record UserRoleAssignRequest(
    @Schema(description = "نام کاربری", example = "john_doe")
    String username,
    
    @Schema(description = "نام نقش", example = "admin")
    String roleName,
    
    @Schema(description = "آیا نقش اصلی است", example = "true")
    boolean isPrimary
) {}
