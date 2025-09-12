package com.mahdi.sso.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String message;
    private boolean success;
    
    // Constructor for success
    public LoginResponse(String token) {
        this.token = token;
        this.message = "Login successful";
        this.success = true;
    }
    
    // Constructor for error
    public LoginResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }
} 