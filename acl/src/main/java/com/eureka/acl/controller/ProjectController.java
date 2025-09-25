package com.eureka.acl.controller;

import com.eureka.acl.dto.ProjectCreateRequest;
import com.eureka.acl.entity.Project;
import com.eureka.acl.service.ProjectRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Project Management Controller
 * Handles project creation and listing operations
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "Project Management", description = "مدیریت پروژه‌ها")
public class ProjectController {
    
    private final ProjectRegistrationService projectRegistrationService;
    
    @Operation(
            summary = "ایجاد پروژه جدید",
            description = "یک پروژه جدید را در سیستم ثبت می‌کند"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "پروژه با موفقیت ایجاد شد",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Project.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "اطلاعات ورودی نامعتبر"
            )
    })
    @PostMapping
    public ResponseEntity<Project> createProject(
            @Parameter(description = "اطلاعات پروژه جدید", required = true)
            @RequestBody ProjectCreateRequest request) {
        
        log.info("Creating new project: {}", request.name());
        
        Project project = projectRegistrationService.registerProject(
                request.name(),
                request.description(),
                request.baseUrl(),
                request.version()
        );
        
        log.info("Project created successfully: {}", project.getName());
        return ResponseEntity.ok(project);
    }
    
    @Operation(
            summary = "لیست تمام پروژه‌ها",
            description = "لیست تمام پروژه‌های ثبت شده در سیستم را برمی‌گرداند"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "لیست پروژه‌ها با موفقیت دریافت شد",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Project.class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        log.info("Getting all projects");
        
        List<Project> projects = projectRegistrationService.getAllProjects();
        
        log.info("Found {} projects", projects.size());
        return ResponseEntity.ok(projects);
    }
    
    @Operation(
            summary = "دریافت پروژه بر اساس نام",
            description = "پروژه‌ای را بر اساس نام آن جستجو می‌کند"
    )
    @GetMapping("/{name}")
    public ResponseEntity<Project> getProjectByName(
            @Parameter(description = "نام پروژه", required = true)
            @PathVariable String name) {
        
        log.info("Getting project by name: {}", name);
        
        return projectRegistrationService.getProjectByName(name)
                .map(project -> {
                    log.info("Project found: {}", project.getName());
                    return ResponseEntity.ok(project);
                })
                .orElseGet(() -> {
                    log.warn("Project not found: {}", name);
                    return ResponseEntity.notFound().build();
                });
    }
    
}
