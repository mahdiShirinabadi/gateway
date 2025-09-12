package com.eureka.acl.controller;

import com.eureka.acl.entity.Project;
import com.eureka.acl.entity.ProjectApi;
import com.eureka.acl.service.ProjectRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project-registration")
@RequiredArgsConstructor
@Log4j2
public class ProjectRegistrationController {
    
    private final ProjectRegistrationService projectRegistrationService;
    
    @PostMapping("/register")
    public ResponseEntity<Project> registerProject(@RequestBody ProjectRegistrationRequest request) {
        log.info("Project registration request: {}", request.getName());
        
        Project project = projectRegistrationService.registerProject(
                request.getName(),
                request.getDescription(),
                request.getBaseUrl(),
                request.getVersion()
        );
        
        return ResponseEntity.ok(project);
    }
    
    @PostMapping("/{projectName}/apis")
    public ResponseEntity<String> registerProjectApis(@PathVariable String projectName, 
                                                    @RequestBody List<ProjectRegistrationService.ApiRegistration> apis) {
        log.info("API registration request for project: {} with {} APIs", projectName, apis.size());
        
        projectRegistrationService.registerProjectApis(projectName, apis);
        
        return ResponseEntity.ok("APIs registered successfully for project: " + projectName);
    }
    
    @GetMapping("/{projectName}/apis")
    public ResponseEntity<List<ProjectApi>> getProjectApis(@PathVariable String projectName) {
        log.info("Getting APIs for project: {}", projectName);
        
        List<ProjectApi> apis = projectRegistrationService.getProjectApis(projectName);
        
        return ResponseEntity.ok(apis);
    }
    
    @GetMapping("/projects")
    public ResponseEntity<List<Project>> getAllProjects() {
        log.info("Getting all projects");
        
        List<Project> projects = projectRegistrationService.getAllProjects();
        
        return ResponseEntity.ok(projects);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Project Registration Service is running");
    }
    
    public static class ProjectRegistrationRequest {
        private String name;
        private String description;
        private String baseUrl;
        private String version;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
    }
} 