package com.eureka.gateway.controller;

import com.eureka.gateway.util.GatewayKeyGenerator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/gateway")
public class PublicKeyController {
    private final WebClient webClient;
    private final GatewayKeyGenerator gatewayKeyGenerator;

    public PublicKeyController(WebClient webClient, GatewayKeyGenerator gatewayKeyGenerator) {
        this.webClient = webClient;
        this.gatewayKeyGenerator = gatewayKeyGenerator;
    }

    @GetMapping("/public-key")
    public ResponseEntity<Map<String, Object>> getGatewayPublicKey() {
        System.out.println("Gateway: درخواست کلید عمومی Gateway");
        
        try {
            if (gatewayKeyGenerator.areKeysAvailable()) {
                String publicKeyString = gatewayKeyGenerator.getPublicKeyAsString();
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "کلید عمومی Gateway ارائه شد");
                result.put("publicKey", publicKeyString);
                result.put("source", "gateway");
                result.put("timestamp", System.currentTimeMillis());
                result.put("keyType", "RSA");
                result.put("keySize", "2048");
                
                System.out.println("Gateway: کلید عمومی Gateway با موفقیت ارائه شد");
                return ResponseEntity.ok(result);
                
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "کلیدهای Gateway در دسترس نیستند");
                errorResponse.put("error", "Gateway keys not available");
                return ResponseEntity.status(500).body(errorResponse);
            }
            
        } catch (Exception e) {
            System.out.println("Gateway: خطا در ارائه کلید عمومی: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "خطا در ارائه کلید عمومی Gateway");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/public-key/health")
    public ResponseEntity<Map<String, Object>> getGatewayPublicKeyHealth() {
        System.out.println("Gateway: بررسی وضعیت کلید عمومی Gateway");
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "وضعیت کلید عمومی Gateway بررسی شد");
        result.put("gatewayKeysAvailable", gatewayKeyGenerator.areKeysAvailable());
        result.put("gatewayStatus", "healthy");
        result.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/sso/public-key")
    public Mono<ResponseEntity<Map<String, Object>>> getSsoPublicKey() {
        System.out.println("Gateway: دریافت درخواست کلید عمومی از SSO");
        
        return webClient.get()
                .uri("http://localhost:8081/api/auth/public-key")
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("message", "کلید عمومی از SSO دریافت شد");
                    result.put("publicKey", response.get("publicKey"));
                    result.put("source", "sso");
                    result.put("timestamp", System.currentTimeMillis());
                    System.out.println("Gateway: کلید عمومی با موفقیت از SSO دریافت شد");
                    return ResponseEntity.ok(result);
                })
                .onErrorResume(error -> {
                    System.out.println("Gateway: خطا در دریافت کلید عمومی از SSO: " + error.getMessage());
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "خطا در دریافت کلید عمومی از SSO");
                    errorResponse.put("error", error.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(errorResponse));
                });
    }
} 