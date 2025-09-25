package com.eureka.acl.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for user-group assignment request
 */
@Schema(description = "اطلاعات اختصاص کاربر به گروه")
public record UserGroupAssignRequest(
    @Schema(description = "نام کاربری", example = "john_doe")
    String username,
    
    @Schema(description = "نام گروه", example = "admin_group")
    String groupName,
    
    @Schema(description = "آیا گروه اصلی است", example = "true")
    boolean isPrimary
) {}
