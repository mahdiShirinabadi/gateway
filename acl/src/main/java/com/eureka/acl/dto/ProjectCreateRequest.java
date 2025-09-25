package com.eureka.acl.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for project creation request
 */
@Schema(description = "اطلاعات پروژه جدید")
public record ProjectCreateRequest(
    @Schema(description = "نام پروژه", example = "service1")
    String name,
    
    @Schema(description = "توضیحات پروژه", example = "Service1 Microservice")
    String description,
    
    @Schema(description = "آدرس پایه پروژه", example = "http://localhost:8082")
    String baseUrl,
    
    @Schema(description = "نسخه پروژه", example = "1.0.0")
    String version
) {}
