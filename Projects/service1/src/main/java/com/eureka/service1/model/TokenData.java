package com.eureka.service1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenData implements Serializable {
    private String username;
    private List<String> permissions;
    private LocalDateTime validatedAt;
    private LocalDateTime expiresAt;
    
    public TokenData(String username, List<String> permissions) {
        this.username = username;
        this.permissions = permissions;
        this.validatedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(30);
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }
} 