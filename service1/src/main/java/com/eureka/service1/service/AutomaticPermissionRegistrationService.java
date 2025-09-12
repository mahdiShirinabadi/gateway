package com.eureka.service1.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Method;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutomaticPermissionRegistrationService {
    
    private final WebClient webClient;
    private final ApplicationContext applicationContext;
    
    @Value("${acl.service.url}")
    private String aclServiceUrl;
    
    @Value("${spring.application.name:service1}")
    private String projectName;
    
    @Value("${server.port:8082}")
    private String serverPort;
    
    @Value("${server.servlet.context-path:}")
    private String contextPath;
    
    @EventListener(ApplicationReadyEvent.class)
    public void registerPermissionsOnStartup() {
        log.info("Service1 starting - automatically registering all API endpoints with ACL service...");
        
        try {
            // Register project first
            registerProject();
            
            // Discover and register all API endpoints
            registerAllDiscoveredEndpoints();
            
            log.info("Service1 automatic permission registration completed successfully!");
            
        } catch (Exception e) {
            log.error("Error registering permissions with ACL service: {}", e.getMessage(), e);
        }
    }
    
    private void registerProject() {
        try {
            log.info("Registering project: {} with ACL service", projectName);
            
            Map<String, Object> projectData = Map.of(
                "projectName", projectName,
                "description", "Service1 Microservice - Auto-discovered APIs",
                "baseUrl", "http://localhost:" + serverPort + contextPath,
                "version", "1.0.0",
                "autoDiscovery", true
            );
            
            webClient.post()
                    .uri(aclServiceUrl.replace("/check", "/project-registration/register"))
                    .header("Content-Type", "application/json")
                    .bodyValue(projectData)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            log.info("Project {} registered successfully", projectName);
            
        } catch (Exception e) {
            log.error("Error registering project {}: {}", projectName, e.getMessage());
        }
    }
    
    private void registerAllDiscoveredEndpoints() {
        try {
            log.info("Discovering all API endpoints in Service1...");
            
            List<EndpointInfo> discoveredEndpoints = discoverAllEndpoints();
            
            log.info("Discovered {} endpoints, registering with ACL service...", discoveredEndpoints.size());
            
            for (EndpointInfo endpoint : discoveredEndpoints) {
                registerEndpoint(endpoint);
            }
            
            log.info("All {} endpoints registered successfully with ACL service", discoveredEndpoints.size());
            
        } catch (Exception e) {
            log.error("Error registering discovered endpoints: {}", e.getMessage());
        }
    }
    
    private List<EndpointInfo> discoverAllEndpoints() {
        List<EndpointInfo> endpoints = new ArrayList<>();
        
        // Get all beans that are controllers
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RestController.class);
        
        for (Object controller : controllers.values()) {
            Class<?> controllerClass = controller.getClass();
            
            // Get class-level RequestMapping
            RequestMapping classMapping = controllerClass.getAnnotation(RequestMapping.class);
            String classPath = classMapping != null ? classMapping.value()[0] : "";
            
            // Discover all methods in the controller
            Method[] methods = controllerClass.getMethods();
            
            for (Method method : methods) {
                EndpointInfo endpoint = discoverEndpoint(method, classPath);
                if (endpoint != null) {
                    endpoints.add(endpoint);
                }
            }
        }
        
        return endpoints;
    }
    
    private EndpointInfo discoverEndpoint(Method method, String classPath) {
        // Check for all possible HTTP method annotations
        String httpMethod = null;
        String path = null;
        String permissionName = null;
        boolean isCritical = false;
        String persianName = null;
        
        // Check for @GetMapping
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        if (getMapping != null) {
            httpMethod = "GET";
            path = getMapping.value().length > 0 ? getMapping.value()[0] : "";
        }
        
        // Check for @PostMapping
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        if (postMapping != null) {
            httpMethod = "POST";
            path = postMapping.value().length > 0 ? postMapping.value()[0] : "";
        }
        
        // Check for @PutMapping
        PutMapping putMapping = method.getAnnotation(PutMapping.class);
        if (putMapping != null) {
            httpMethod = "PUT";
            path = putMapping.value().length > 0 ? putMapping.value()[0] : "";
        }
        
        // Check for @DeleteMapping
        DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
        if (deleteMapping != null) {
            httpMethod = "DELETE";
            path = deleteMapping.value().length > 0 ? deleteMapping.value()[0] : "";
        }
        
        // Check for @PatchMapping
        PatchMapping patchMapping = method.getAnnotation(PatchMapping.class);
        if (patchMapping != null) {
            httpMethod = "PATCH";
            path = patchMapping.value().length > 0 ? patchMapping.value()[0] : "";
        }
        
        // Check for @RequestMapping
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        if (requestMapping != null && httpMethod == null) {
            httpMethod = requestMapping.method().length > 0 ? requestMapping.method()[0].name() : "GET";
            path = requestMapping.value().length > 0 ? requestMapping.value()[0] : "";
        }
        
        // If no HTTP method found, skip this method
        if (httpMethod == null) {
            return null;
        }
        
        // Build complete path
        String completePath = classPath + path;
        if (!completePath.startsWith("/")) {
            completePath = "/" + completePath;
        }
        
        // Generate permission name based on path and method
        permissionName = generatePermissionName(completePath, httpMethod);
        
        // Determine if endpoint is critical based on path
        isCritical = isCriticalEndpoint(completePath);
        
        // Generate Persian name
        persianName = generatePersianName(completePath, httpMethod);
        
        // Build complete URL
        String completeUrl = "http://localhost:" + serverPort + contextPath + completePath;
        
        // Create extra data
        Map<String, Object> extraData = createExtraData(completePath, httpMethod, method);
        
        return new EndpointInfo(permissionName, completeUrl, httpMethod, isCritical, persianName, extraData);
    }
    
    private String generatePermissionName(String path, String method) {
        // Convert path to permission name
        String permission = path
                .replaceAll("/", "_")
                .replaceAll("-", "_")
                .toUpperCase();
        
        // Remove leading underscore if exists
        if (permission.startsWith("_")) {
            permission = permission.substring(1);
        }
        
        // Add method prefix
        return "SERVICE1_" + method + "_" + permission;
    }
    
    private boolean isCriticalEndpoint(String path) {
        // Define critical endpoints (admin, security, etc.)
        String lowerPath = path.toLowerCase();
        return lowerPath.contains("/admin") || 
               lowerPath.contains("/super") || 
               lowerPath.contains("/security") ||
               lowerPath.contains("/user") ||
               lowerPath.contains("/authorization");
    }
    
    private String generatePersianName(String path, String method) {
        // Generate Persian name based on path
        String lowerPath = path.toLowerCase();
        
        if (lowerPath.contains("/hello")) {
            return "دسترسی به صفحه خوش‌آمدگویی";
        } else if (lowerPath.contains("/admin")) {
            return "دسترسی به پنل مدیریت";
        } else if (lowerPath.contains("/super")) {
            return "دسترسی به پنل سوپر ادمین";
        } else if (lowerPath.contains("/user")) {
            return "دسترسی به اطلاعات کاربر";
        } else if (lowerPath.contains("/security")) {
            return "دسترسی به تست امنیت";
        } else if (lowerPath.contains("/authorization")) {
            return "دسترسی به تست مجوزدهی";
        } else if (lowerPath.contains("/public")) {
            return "دسترسی عمومی";
        } else {
            return "دسترسی به " + path;
        }
    }
    
    private Map<String, Object> createExtraData(String path, String method, Method javaMethod) {
        Map<String, Object> extraData = new HashMap<>();
        
        extraData.put("category", determineCategory(path));
        extraData.put("description", "Auto-discovered endpoint: " + method + " " + path);
        extraData.put("javaMethod", javaMethod.getName());
        extraData.put("javaClass", javaMethod.getDeclaringClass().getSimpleName());
        extraData.put("autoDiscovered", true);
        extraData.put("discoveryTime", new Date().toString());
        
        // Add method-specific data
        if (path.toLowerCase().contains("/admin")) {
            extraData.put("requiresRole", "ADMIN");
        } else if (path.toLowerCase().contains("/super")) {
            extraData.put("requiresRole", "SUPER_ADMIN");
        } else if (path.toLowerCase().contains("/public")) {
            extraData.put("noAuthRequired", true);
        }
        
        return extraData;
    }
    
    private String determineCategory(String path) {
        String lowerPath = path.toLowerCase();
        
        if (lowerPath.contains("/admin") || lowerPath.contains("/super")) {
            return "admin";
        } else if (lowerPath.contains("/security") || lowerPath.contains("/authorization")) {
            return "security";
        } else if (lowerPath.contains("/user")) {
            return "user";
        } else if (lowerPath.contains("/public")) {
            return "public";
        } else {
            return "general";
        }
    }
    
    private void registerEndpoint(EndpointInfo endpoint) {
        try {
            log.debug("Registering endpoint: {} {} with ACL service", endpoint.getMethod(), endpoint.getCompleteUrl());
            
            Map<String, Object> endpointData = Map.of(
                "name", endpoint.getPermissionName(),
                "projectName", projectName,
                "completeUrl", endpoint.getCompleteUrl(),
                "method", endpoint.getMethod(),
                "isCritical", endpoint.isCritical(),
                "persianName", endpoint.getPersianName(),
                "extraData", endpoint.getExtraData()
            );
            
            webClient.post()
                    .uri(aclServiceUrl.replace("/check", "/permissions"))
                    .header("Content-Type", "application/json")
                    .bodyValue(endpointData)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            log.debug("Endpoint {} registered successfully", endpoint.getPermissionName());
            
        } catch (Exception e) {
            log.error("Error registering endpoint {}: {}", endpoint.getPermissionName(), e.getMessage());
        }
    }
    
    // Inner class to hold endpoint information
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class EndpointInfo {
        private final String permissionName;
        private final String completeUrl;
        private final String method;
        private final boolean isCritical;
        private final String persianName;
        private final Map<String, Object> extraData;
    }
} 