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
    
    public TokenInfo(String username, List<String> permissions) {
        this.username = username;
        this.permissions = permissions;
        this.validatedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(30);
        this.source = "gateway";
    }
    
    public TokenInfo(String username, List<String> permissions, String source) {
        this.username = username;
        this.permissions = permissions;
        this.validatedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(30);
        this.source = source;
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
}
