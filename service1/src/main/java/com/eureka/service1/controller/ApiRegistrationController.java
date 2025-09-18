package com.eureka.service1.controller;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/api/registration")
@RequiredArgsConstructor
@Log4j2
public class ApiRegistrationController {
    
    @Value("${server.port:8082}")
    private String serverPort;

    private final RestTemplate restTemplate;
    
    @EventListener(ApplicationReadyEvent.class)
    public void registerApisOnStartup() {
        log.info("Registering Service1 APIs with ACL service...");
        
        try {
            // Register project first
            registerProject();
            
            // Register APIs
            registerApis();
            
            log.info("Service1 APIs registered successfully with ACL service");
        } catch (Exception e) {
            log.error("Failed to register APIs with ACL service: {}", e.getMessage());
        }
    }
    
    private void registerProject() {
        String aclUrl = "http://localhost:8080/acl/api/project-registration/register";
        
        ProjectRegistrationRequest request = new ProjectRegistrationRequest();
        request.setName("service1");
        request.setDescription("Service1 - Sample Microservice");
        request.setBaseUrl("http://localhost:" + serverPort);
        request.setVersion("1.0.0");
        
        try {
            restTemplate.postForEntity(aclUrl, request, Object.class);
            log.info("Project registration successful");
        } catch (Exception e) {
            log.error("Project registration failed: {}", e.getMessage());
        }
    }
    
    private void registerApis() {
        String aclUrl = "http://localhost:8080/acl/api/project-registration/service1/apis";
        
        List<ApiRegistration> apis = Arrays.asList(
            new ApiRegistration(
                "/app1/hello",
                "GET",
                "SERVICE1_HELLO_ACCESS",
                "Hello endpoint for basic users",
                false,
                false,
                "دسترسی به صفحه سلام"
            ),
            new ApiRegistration(
                "/app1/admin",
                "GET",
                "SERVICE1_ADMIN_ACCESS",
                "Admin endpoint for administrators",
                false,
                true,
                "دسترسی ادمین به سرویس 1"
            )
        );
        
        try {
            restTemplate.postForEntity(aclUrl, apis, Object.class);
            log.info("API registration successful");
        } catch (Exception e) {
            log.error("API registration failed: {}", e.getMessage());
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Service1 API Registration Controller is running");
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProjectRegistrationRequest {
        private String name;
        private String description;
        private String baseUrl;
        private String version;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApiRegistration {
        private String apiPath;
        private String httpMethod;
        private String permissionName;
        private String description;
        private boolean isPublic;
        private boolean isCritical;
        private String persianName;
    }
} 