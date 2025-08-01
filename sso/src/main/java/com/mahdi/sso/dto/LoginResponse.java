package com.mahdi.sso.dto;

public class LoginResponse {
    private String token;
    private String message;
    private boolean success;
    
    // Default constructor
    public LoginResponse() {}
    
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
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
} 