package com.eureka.gateway;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/test-auth")
    public Mono<ResponseEntity<Map<String, Object>>> testAuthentication(
            @RequestHeader(value = "X-Validated-Token", required = false) String validatedToken,
            @RequestHeader(value = "X-Authenticated-User", required = false) String authenticatedUser) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Authentication test successful");
        response.put("validatedToken", validatedToken != null ? "present" : "missing");
        response.put("authenticatedUser", authenticatedUser);
        response.put("timestamp", System.currentTimeMillis());
        
        return Mono.just(ResponseEntity.ok(response));
    }

    @GetMapping("/test-no-auth")
    public Mono<ResponseEntity<Map<String, Object>>> testNoAuthentication() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This endpoint doesn't require authentication");
        response.put("timestamp", System.currentTimeMillis());
        
        return Mono.just(ResponseEntity.ok(response));
    }
} 