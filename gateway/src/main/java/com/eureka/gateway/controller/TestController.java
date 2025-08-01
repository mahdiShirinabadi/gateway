package com.eureka.gateway.controller;

import com.eureka.gateway.util.GatewayKeyGenerator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {
    private final GatewayKeyGenerator gatewayKeyGenerator;

    public TestController(GatewayKeyGenerator gatewayKeyGenerator) {
        this.gatewayKeyGenerator = gatewayKeyGenerator;
    }

    @GetMapping("/keys/status")
    public ResponseEntity<Map<String, Object>> getKeysStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean keysAvailable = gatewayKeyGenerator.areKeysAvailable();
            PrivateKey privateKey = gatewayKeyGenerator.getPrivateKey();
            PublicKey publicKey = gatewayKeyGenerator.getPublicKey();
            String publicKeyString = gatewayKeyGenerator.getPublicKeyAsString();
            
            response.put("success", true);
            response.put("keysAvailable", keysAvailable);
            response.put("privateKeyAvailable", privateKey != null);
            response.put("publicKeyAvailable", publicKey != null);
            response.put("publicKeyLength", publicKeyString != null ? publicKeyString.length() : 0);
            response.put("message", "وضعیت کلیدهای Gateway بررسی شد");
            
            System.out.println("Gateway: وضعیت کلیدها - موجود: " + keysAvailable + 
                             ", خصوصی: " + (privateKey != null) + 
                             ", عمومی: " + (publicKey != null));
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            System.err.println("Gateway: خطا در بررسی وضعیت کلیدها: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/keys/regenerate")
    public ResponseEntity<Map<String, Object>> regenerateKeys() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("Gateway: درخواست تولید مجدد کلیدها");
            gatewayKeyGenerator.regenerateKeys();
            
            boolean keysAvailable = gatewayKeyGenerator.areKeysAvailable();
            String publicKeyString = gatewayKeyGenerator.getPublicKeyAsString();
            
            response.put("success", true);
            response.put("keysRegenerated", true);
            response.put("keysAvailable", keysAvailable);
            response.put("publicKeyLength", publicKeyString != null ? publicKeyString.length() : 0);
            response.put("message", "کلیدهای Gateway مجدداً تولید شدند");
            
            System.out.println("Gateway: کلیدها با موفقیت مجدداً تولید شدند");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            System.err.println("Gateway: خطا در تولید مجدد کلیدها: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
} 