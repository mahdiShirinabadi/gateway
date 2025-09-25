package com.eureka.acl.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for group creation request
 */
@Schema(description = "اطلاعات گروه جدید")
public record GroupCreateRequest(
    @Schema(description = "نام گروه", example = "admin_group")
    String name,
    
    @Schema(description = "توضیحات گروه", example = "Administrator group with full access")
    String description,
    
    @Schema(description = "آیا گروه فعال است", example = "true")
    boolean isActive
) {}
