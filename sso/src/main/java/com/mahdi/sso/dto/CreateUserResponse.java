package com.mahdi.sso.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user creation response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserResponse {
    
    private Long id;
    private String username;
    private String message;
    private boolean success;
    
    public static CreateUserResponse success(Long id, String username) {
        return new CreateUserResponse(id, username, "User created successfully", true);
    }
    
    public static CreateUserResponse error(String message) {
        return new CreateUserResponse(null, null, message, false);
    }
}

