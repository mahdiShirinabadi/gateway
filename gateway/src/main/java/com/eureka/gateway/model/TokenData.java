package com.eureka.gateway.model;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class TokenData implements Serializable {
    private String token;
    private String username;
    private List<String> permissions;
    private LocalDateTime validatedAt;
    private LocalDateTime expiresAt;
    private String signature;

    public TokenData(String token, String username, List<String> permissions) {
        this.token = token;
        this.username = username;
        this.permissions = permissions;
        this.validatedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(30);
        this.signature = generateSignature();
    }

    public TokenData(String token, String username, List<String> permissions, LocalDateTime validatedAt, LocalDateTime expiresAt) {
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

    public boolean isSignatureValid(PrivateKey privateKey) {
        try {
            String dataToVerify = getSignedData();
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(privateKey);
            sig.update(dataToVerify.getBytes());
            byte[] signatureBytes = sig.sign();
            String expectedSignature = Base64.getEncoder().encodeToString(signatureBytes);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            System.err.println("Gateway: خطا در تأیید امضا: " + e.getMessage());
            return false;
        }
    }

    private String generateSignature() {
        try {
            String dataToSign = getSignedData();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataToSign.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Gateway: خطا در تولید امضا: " + e.getMessage());
            return null;
        }
    }

    public String getSignedData() {
        String sortedPermissions = permissions != null ? 
            permissions.stream().sorted().collect(Collectors.joining(",")) : "";
        
        return String.format("%s|%s|%s|%s|%s|%s",
            token,
            username,
            sortedPermissions,
            validatedAt.toString(),
            expiresAt.toString(),
            "GATEWAY_SECRET_KEY_2024"
        );
    }

    public String getSortedPermissionsString() {
        return permissions != null ? 
            permissions.stream().sorted().collect(Collectors.joining(",")) : "";
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
        this.signature = generateSignature();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        this.signature = generateSignature();
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
        this.signature = generateSignature();
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
        this.signature = generateSignature();
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
        this.signature = generateSignature();
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        return "TokenData{" +
                "token='" + token + '\'' +
                ", username='" + username + '\'' +
                ", permissions=" + permissions +
                ", validatedAt=" + validatedAt +
                ", expiresAt=" + expiresAt +
                ", signature='" + signature + '\'' +
                '}';
    }
} 