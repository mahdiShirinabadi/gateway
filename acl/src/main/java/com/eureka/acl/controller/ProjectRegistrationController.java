package com.eureka.acl.controller;

import com.eureka.acl.entity.Project;
import com.eureka.acl.model.ProjectRegistrationRequest;
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

@RestController
@RequestMapping("/api/project-registration")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "Project Registration", description = "ثبت و مدیریت پروژه‌ها و API های آنها")
public class ProjectRegistrationController {
    
    private final ProjectRegistrationService projectRegistrationService;
    
    @Operation(
            summary = "ثبت پروژه جدید",
            description = "یک پروژه جدید را در سیستم ثبت می‌کند"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "پروژه با موفقیت ثبت شد",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Project.class)
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<Project> registerProject(
            @Parameter(description = "اطلاعات پروژه جدید", required = true)
            @RequestBody ProjectRegistrationRequest request) {
        log.info("Project registration request: {}", request.getName());
        
        Project project = projectRegistrationService.registerProject(
                request.getName(),
                request.getDescription(),
                request.getBaseUrl(),
                request.getVersion()
        );
        
        return ResponseEntity.ok(project);
    }
    


    
    @Operation(
            summary = "دریافت تمام پروژه‌ها",
            description = "لیست تمام پروژه‌های ثبت شده در سیستم را برمی‌گرداند"
    )
    @GetMapping("/projects")
    public ResponseEntity<List<Project>> getAllProjects() {
        log.info("Getting all projects");
        
        List<Project> projects = projectRegistrationService.getAllProjects();
        
        return ResponseEntity.ok(projects);
    }
    
    @Operation(
            summary = "بررسی وضعیت سرویس",
            description = "بررسی می‌کند که سرویس ثبت پروژه در حال اجرا است یا خیر"
    )
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Project Registration Service is running");
    }

} 