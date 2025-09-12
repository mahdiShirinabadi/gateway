package com.eureka.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class FallbackController {

    @GetMapping("/fallback")
    public Mono<ResponseEntity<Map<String, Object>>> fallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Service is temporarily unavailable. Please try again later.");
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("timestamp", System.currentTimeMillis());
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response));
    }

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "api-gateway");
        response.put("timestamp", System.currentTimeMillis());
        
        return Mono.just(ResponseEntity.ok(response));
    }
} 