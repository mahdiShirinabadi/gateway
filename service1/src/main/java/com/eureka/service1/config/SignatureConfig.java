package com.eureka.service1.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "signature")
public class SignatureConfig {
    
    private String format = "username|permissions|token";
    private String separator = "|";
    private List<String> fields = List.of("username", "permissions", "token");
    private String algorithm = "SHA-256";
    private String encoding = "UTF-8";
    
    private Validation validation = new Validation();
    private Debug debug = new Debug();
    
    @Data
    public static class Validation {
        private boolean enabled = true;
        private boolean strict = true;
    }
    
    @Data
    public static class Debug {
        private boolean enabled = false;
        private boolean logFormat = true;
    }
    
    /**
     * Get the signature format for a specific service
     */
    public String getServiceFormat(String serviceName) {
        // For now, all services use the same format
        // In the future, this could be service-specific
        return format;
    }
    
    /**
     * Get the fields that should be included in signature
     */
    public List<String> getSignatureFields() {
        return fields;
    }
    
    /**
     * Check if signature validation is enabled
     */
    public boolean isValidationEnabled() {
        return validation.isEnabled();
    }
    
    /**
     * Check if debug logging is enabled
     */
    public boolean isDebugEnabled() {
        return debug.isEnabled();
    }
}
