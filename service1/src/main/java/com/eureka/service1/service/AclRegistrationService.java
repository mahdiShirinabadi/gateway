package com.eureka.service1.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple ACL Registration Service
 * Registers Service1 methods with ACL service using HttpClient
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class AclRegistrationService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    /**
     * Register Service1 methods with ACL service
     */
    public boolean registerService1Methods() {
        try {
            log.info("Registering Service1 methods with ACL service");
            
            // Register hello method
            boolean result = registerMethod("service1", "hello", "GET", "Hello endpoint access");
            
            if (result) {
                log.info("Service1 methods registered successfully");
            } else {
                log.warn("Failed to register Service1 methods");
            }
            
            return result;

        } catch (Exception e) {
            log.error("Error registering Service1 methods: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Register a method with ACL service
     */
    private boolean registerMethod(String service, String method, String httpMethod, String description) {
        try {
            String permissionName = service.toUpperCase() + "_" + method.toUpperCase() + "_ACCESS";
            String apiPath = "/" + method;
            String persianName = "دسترسی به " + method;
            
            Map<String, Object> request = new HashMap<>();
            request.put("name", permissionName);
            request.put("projectName", service);
            request.put("apiPath", apiPath);
            request.put("httpMethod", httpMethod);
            request.put("description", description);
            request.put("persianName", persianName);
            request.put("isCritical", false);
            request.put("isPublic", false);
            
            String requestBody = objectMapper.writeValueAsString(request);
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8083/api/acl/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(10))
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
                Boolean success = (Boolean) responseBody.get("success");
                
                log.debug("Method registered: {} - {} at {}", service, method, apiPath);
                return success != null && success;
            } else {
                log.warn("ACL registration returned status: {}", response.statusCode());
                return false;
            }
            
        } catch (Exception e) {
            log.error("Failed to register method {}: {}", method, e.getMessage());
            return false;
        }
    }
}
