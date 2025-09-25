package com.eureka.acl.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for permission creation request
 */
@Schema(description = "اطلاعات مجوز جدید")
public record PermissionCreateRequest(
    @Schema(description = "نام مجوز", example = "SERVICE1_API_HELLO_GET")
    String name,
    
    @Schema(description = "نام پروژه", example = "service1")
    String projectName,
    
    @Schema(description = "مسیر API", example = "/api/hello")
    String apiPath,
    
    @Schema(description = "روش HTTP", example = "GET")
    String httpMethod,
    
    @Schema(description = "توضیحات مجوز", example = "Access to hello endpoint")
    String description,
    
    @Schema(description = "نام فارسی مجوز", example = "دسترسی به صفحه خوش آمدید")
    String persianName,
    
    @Schema(description = "آیا مجوز حساس است", example = "false")
    boolean isCritical,
    
    @Schema(description = "آیا مجوز عمومی است", example = "false")
    boolean isPublic
) {}
