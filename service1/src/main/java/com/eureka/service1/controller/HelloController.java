package com.eureka.service1.controller;

import com.eureka.service1.annotation.RequirePermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple Hello Controller with AOP-based ACL
 * Uses @RequirePermission annotation for automatic permission checking
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Log4j2
public class HelloController {

    @GetMapping("/hello")
    @RequirePermission(resource = "service1", action = "SERVICE1_HELLO_ACCESS")
    public ResponseEntity<Map<String, Object>> hello(
            @RequestHeader(value = "X-Authenticated-User", required = false) String authenticatedUser,
            @RequestHeader(value = "X-Auth-Token", required = false) String authToken,
            @RequestHeader(value = "X-User-Info", required = false) String userInfo) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Hello endpoint called by user: {}", authenticatedUser);
            
            response.put("message", "Hello from Service1!");
            response.put("status", "success");
            response.put("authenticatedUser", authenticatedUser);
            response.put("userInfo", userInfo);
            response.put("tokenPresent", authToken != null);
            
            log.info("Hello endpoint accessed successfully by user: {}", authenticatedUser);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Internal server error");
            response.put("message", e.getMessage());
            log.error("Error in hello endpoint: {}", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}