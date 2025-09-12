package com.eureka.gateway.controller;

import com.eureka.gateway.util.GatewayKeyGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
@Tag(name = "Gateway Test", description = "تست و مدیریت Gateway")
public class TestController {
    private final GatewayKeyGenerator gatewayKeyGenerator;

    public TestController(GatewayKeyGenerator gatewayKeyGenerator) {
        this.gatewayKeyGenerator = gatewayKeyGenerator;
    }

    @Operation(
            summary = "بررسی وضعیت کلیدها",
            description = "وضعیت کلیدهای رمزنگاری Gateway را بررسی می‌کند"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "وضعیت کلیدها با موفقیت بررسی شد"
            )
    })
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

    @Operation(
            summary = "تولید مجدد کلیدها",
            description = "کلیدهای رمزنگاری Gateway را مجدداً تولید می‌کند"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "کلیدها با موفقیت مجدداً تولید شدند"
            )
    })
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