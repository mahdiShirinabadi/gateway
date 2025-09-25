package com.eureka.acl.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for role creation request
 */
@Schema(description = "اطلاعات نقش جدید")
public record RoleCreateRequest(
    @Schema(description = "نام نقش", example = "admin")
    String name,
    
    @Schema(description = "توضیحات نقش", example = "Administrator role with full access")
    String description
) {}
