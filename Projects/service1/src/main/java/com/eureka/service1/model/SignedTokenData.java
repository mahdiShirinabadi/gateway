package com.eureka.service1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignedTokenData implements Serializable {
    private String token;
    private String username;
    private List<String> permissions;
    private LocalDateTime validatedAt;
    private LocalDateTime expiresAt;
    private String signature;
    
    public SignedTokenData(String token, String username, List<String> permissions) {
        this.token = token;
        this.username = username;
        this.permissions = permissions;
        this.validatedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(30);
        this.signature = generateSignature();
    }
    
    public SignedTokenData(String token, String username, List<String> permissions, LocalDateTime validatedAt, LocalDateTime expiresAt) {
        this.token = token;
        this.username = username;
        this.permissions = permissions;
        this.validatedAt = validatedAt;
        this.expiresAt = expiresAt;
        this.signature = generateSignature();
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }
    
    /**
     * Verify signature using SSO public key
     * This method will be called by Service1 to verify the signature
     */
    public boolean isSignatureValid() {
        String expectedSignature = generateSignature();
        return expectedSignature.equals(signature);
    }
    
    /**
     * Generate signature for the token data
     * This creates a hash of the data that can be verified
     */
    private String generateSignature() {
        try {
            // Create data to sign: token + sorted permissions + username + timestamps
            String sortedPermissions = permissions != null ? 
                permissions.stream().sorted().collect(Collectors.joining(",")) : "";
            
            String dataToSign = token + "|" + 
                              username + "|" + 
                              sortedPermissions + "|" + 
                              validatedAt + "|" + 
                              expiresAt + "|" +
                              "SERVICE1_SECRET_KEY_2024"; // Secret key for signing
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
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
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Get the data that was signed (for debugging/auditing)
     */
    public String getSignedData() {
        String sortedPermissions = permissions != null ? 
            permissions.stream().sorted().collect(Collectors.joining(",")) : "";
        
        return token + "|" + 
               username + "|" + 
               sortedPermissions + "|" + 
               validatedAt + "|" + 
               expiresAt + "|" +
               "SERVICE1_SECRET_KEY_2024";
    }
    
    /**
     * Get sorted permissions as string (for verification)
     */
    public String getSortedPermissionsString() {
        return permissions != null ? 
            permissions.stream().sorted().collect(Collectors.joining(",")) : "";
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
        this.signature = generateSignature(); // Regenerate signature when token changes
    }
    
    public void setUsername(String username) {
        this.username = username;
        this.signature = generateSignature(); // Regenerate signature when username changes
    }
    
    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
        this.signature = generateSignature(); // Regenerate signature when permissions change
    }
    
    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
        this.signature = generateSignature(); // Regenerate signature when timestamp changes
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
        this.signature = generateSignature(); // Regenerate signature when timestamp changes
    }
} 