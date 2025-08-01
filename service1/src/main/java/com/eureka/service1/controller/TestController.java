package com.eureka.service1.controller;

import com.eureka.service1.service.PublicKeyService;
import com.eureka.service1.service.SignatureVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {
    private final PublicKeyService publicKeyService;
    private final SignatureVerificationService signatureVerificationService;

    @GetMapping("/public-key/status")
    public ResponseEntity<Map<String, Object>> getPublicKeyStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean hasCachedKey = publicKeyService.hasCachedPublicKey();
            PublicKey publicKey = publicKeyService.getGatewayPublicKey();
            boolean isAvailable = signatureVerificationService.isGatewayPublicKeyAvailable();
            
            response.put("success", true);
            response.put("hasCachedKey", hasCachedKey);
            response.put("publicKeyAvailable", publicKey != null);
            response.put("verificationServiceAvailable", isAvailable);
            response.put("message", "وضعیت کلید عمومی بررسی شد");
            
            log.info("Service1: وضعیت کلید عمومی - کش: {}, موجود: {}, سرویس: {}", 
                    hasCachedKey, publicKey != null, isAvailable);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            log.error("Service1: خطا در بررسی وضعیت کلید عمومی: {}", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public-key/refresh")
    public ResponseEntity<Map<String, Object>> refreshPublicKey() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Service1: درخواست به‌روزرسانی کلید عمومی");
            publicKeyService.invalidateCachedPublicKey();
            PublicKey newKey = publicKeyService.getGatewayPublicKey();
            
            response.put("success", true);
            response.put("newKeyAvailable", newKey != null);
            response.put("message", "کلید عمومی به‌روزرسانی شد");
            
            log.info("Service1: کلید عمومی با موفقیت به‌روزرسانی شد: {}", newKey != null);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            log.error("Service1: خطا در به‌روزرسانی کلید عمومی: {}", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
} 