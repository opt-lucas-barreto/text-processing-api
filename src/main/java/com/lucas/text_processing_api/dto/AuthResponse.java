package com.lucas.text_processing_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respostas de autenticação
 * 
 * @author Lucas
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private String type = "Bearer";
    private String username;
    private String role;
    private String message;
    
    public AuthResponse(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.message = "Autenticação realizada com sucesso";
    }
}
