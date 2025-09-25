package com.eureka.acl.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for group-role assignment request
 */
@Schema(description = "اطلاعات اختصاص نقش به گروه")
public record GroupRoleAssignRequest(
    @Schema(description = "نام گروه", example = "admin_group")
    String groupName,
    
    @Schema(description = "نام نقش", example = "admin")
    String roleName
) {}
