package com.eureka.acl.controller;

import com.eureka.acl.service.SimpleAclService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple ACL Controller
 * Handles permission checking requests
 */
@RestController
@RequestMapping("/api/acl2")
@RequiredArgsConstructor
@Log4j2
public class SimpleAclController {

    private final SimpleAclService aclService;

    @PostMapping("/check1")
    public ResponseEntity<Map<String, Object>> checkPermission(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = request.get("username");
            String resource = request.get("resource");
            String action = request.get("action");
            
            if (username == null || resource == null || action == null) {
                response.put("error", "Missing required fields");
                response.put("message", "username, resource, and action are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean hasPermission = aclService.hasPermission(username, resource, action);
            
            response.put("hasPermission", hasPermission);
            response.put("username", username);
            response.put("resource", resource);
            response.put("action", action);
            response.put("message", hasPermission ? "Permission granted" : "Permission denied");
            
            log.info("ACL check completed for user: {} result: {}", username, hasPermission);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Internal server error");
            response.put("message", e.getMessage());
            log.error("Error in ACL check: {}", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "ACL");
        response.put("message", "ACL service is healthy");
        
        log.info("Health check endpoint accessed");
        return ResponseEntity.ok(response);
    }
}
