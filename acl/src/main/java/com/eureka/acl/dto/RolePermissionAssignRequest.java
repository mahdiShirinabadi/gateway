package com.eureka.acl.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for role permission assignment request
 */
@Schema(description = "اطلاعات اختصاص مجوز به نقش")
public record RolePermissionAssignRequest(
    @Schema(description = "نام نقش", example = "admin")
    String roleName,
    
    @Schema(description = "نام مجوز", example = "SERVICE1_API_HELLO_GET")
    String permissionName
) {}
