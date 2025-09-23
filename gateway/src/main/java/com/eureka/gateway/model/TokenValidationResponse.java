package com.eureka.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data   // if you use Lombok
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {
    private boolean valid;
    private String message;
    private String username;
}