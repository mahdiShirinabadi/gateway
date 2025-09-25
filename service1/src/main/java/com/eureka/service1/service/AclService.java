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
 * ACL Service for @PreAuthorize
 * Checks permissions with ACL service using HttpClient
 */
@Service("aclService")
@RequiredArgsConstructor
@Log4j2
public class AclService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    /**
     * Check if user has permission to access the requested resource
     * This method is called by @PreAuthorize
     */
    public boolean hasPermission(String username, String resource, String action) {
        try {
            log.info("Checking ACL permission for user: {} resource: {} action: {}", username, resource, action);

            Map<String, String> request = new HashMap<>();
            request.put("username", username);
            request.put("projectName", resource);
            request.put("permissionName", action);
            
            String requestBody = objectMapper.writeValueAsString(request);
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8083/acl/api/acl/check"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(10))
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
                Boolean hasPermission = (Boolean) responseBody.get("allowed");
                
                log.info("ACL check result: {} for user: {} resource: {} action: {}", 
                        hasPermission, username, resource, action);
                
                return hasPermission != null && hasPermission;
            } else {
                log.warn("ACL service returned status: {}", response.statusCode());
                return false;
            }

        } catch (Exception e) {
            log.error("Error checking ACL permission: {}", e.getMessage());
            return false;
        }
    }
}
