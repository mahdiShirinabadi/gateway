package com.eureka.service1.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfo implements Serializable {
    private String username;
    private List<String> permissions;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime validatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;
    
    private String source; // "gateway", "sso", etc.
    private String signature; // Cryptographic signature for integrity verification
    
    public TokenInfo(String username, List<String> permissions, String token) {
        this.username = username;
        this.permissions = permissions;
        this.validatedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(30);
        this.source = "gateway";
        this.signature = generateSignature(token);
    }
    
    public TokenInfo(String username, List<String> permissions, String source, String token) {
        this.username = username;
        this.permissions = permissions;
        this.validatedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(30);
        this.source = source;
        this.signature = generateSignature(token);
    }
    
    // Constructor for use with SignatureService (used by Gateway)
    public TokenInfo(String username, List<String> permissions, String source, String token, String signature) {
        this.username = username;
        this.permissions = permissions;
        this.validatedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(30);
        this.source = source;
        this.signature = signature;
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }
    
    public boolean hasAnyPermission(List<String> requiredPermissions) {
        if (permissions == null || requiredPermissions == null) {
            return false;
        }
        return requiredPermissions.stream().anyMatch(permissions::contains);
    }
    
    /**
     * Generate signature for integrity verification
     * This creates a hash of the data that can be verified
     */
    private String generateSignature(String token) {
        try {
            // Create data to sign: username + sorted permissions + token
            String sortedPermissions = permissions != null ? 
                permissions.stream().sorted().collect(java.util.stream.Collectors.joining(",")) : "";

            String dataToSign = String.format("%s|%s|%s",
                    username,
                    sortedPermissions,
                    token
            );

            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataToSign.getBytes());
            
            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Verify signature for integrity check
     */
    public boolean isSignatureValid(String token) {
        String expectedSignature = generateSignature(token);
        return expectedSignature.equals(signature);
    }
    
    /**
     * Get the data that was signed (for debugging/auditing)
     */
    public String getSignedData(String token) {
        String sortedPermissions = permissions != null ? 
            permissions.stream().sorted().collect(java.util.stream.Collectors.joining(",")) : "";
        
        return String.format("%s|%s|%s", username, sortedPermissions, token);
    }
}
