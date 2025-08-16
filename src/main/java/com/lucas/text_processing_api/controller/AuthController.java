package com.lucas.text_processing_api.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lucas.text_processing_api.dto.AuthRequest;
import com.lucas.text_processing_api.dto.AuthResponse;
import com.lucas.text_processing_api.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller REST para autenticação
 * 
 * Este controller fornece endpoints para autenticação de usuários
 * e geração de tokens JWT.
 * 
 * @author Lucas
 * @version 2.5
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Autenticação", description = "Endpoints para autenticação e gerenciamento de usuários")
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Endpoint para autenticação de usuários
     * 
     * Este endpoint aceita credenciais de usuário e retorna
     * um token JWT válido para autenticação.
     * 
     * @param request dados de autenticação (username e password)
     * @return ResponseEntity contendo o token JWT e informações do usuário
     */
    @Operation(
        summary = "Autenticar usuário",
        description = "Endpoint para autenticação de usuários e geração de token JWT"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuário autenticado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Login bem-sucedido",
                    value = "{\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"type\": \"Bearer\", \"username\": \"admin\", \"role\": \"ADMIN\", \"message\": \"Autenticação realizada com sucesso\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciais inválidas"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados de entrada inválidos"
        )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @Parameter(description = "Dados de autenticação", required = true)
        @Valid @RequestBody AuthRequest request) {
        try {
            log.info("Tentativa de login para usuário: {}", request.getUsername());
            
            AuthResponse response = authService.authenticate(request);
            
            log.info("Login realizado com sucesso para usuário: {}", request.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.warn("Falha no login para usuário {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    /**
     * Endpoint para criação de novos usuários
     * 
     * Este endpoint permite criar novos usuários no sistema.
     * 
     * @param request dados do usuário a ser criado
     * @return ResponseEntity contendo a resposta de autenticação
     */
    @Operation(
        summary = "Registrar novo usuário",
        description = "Endpoint para criação de novos usuários no sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Usuário criado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados de entrada inválidos ou usuário já existe"
        )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
        @Parameter(description = "Dados do usuário", required = true)
        @Valid @RequestBody AuthRequest request) {
        try {
            log.info("Tentativa de registro para usuário: {}", request.getUsername());
            
            AuthResponse response = authService.createUser(request);
            
            log.info("Usuário registrado com sucesso: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Erro no registro para usuário {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Endpoint para verificar se o token JWT é válido
     * 
     * @return ResponseEntity indicando que o token é válido
     */
    @Operation(
        summary = "Validar token JWT",
        description = "Endpoint para verificar se o token JWT é válido"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token válido",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Token válido",
                    value = "{\"message\": \"Token JWT válido\", \"status\": \"AUTHORIZED\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token inválido ou expirado"
        )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/validate")
    public ResponseEntity<Map<String, String>> validateToken() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Token JWT válido");
        response.put("status", "AUTHORIZED");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint de health check para autenticação
     * 
     * @return ResponseEntity indicando que o serviço de autenticação está funcionando
     */
    @Operation(
        summary = "Health check da autenticação",
        description = "Endpoint para verificar o status do serviço de autenticação"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Serviço funcionando",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Serviço saudável",
                    value = "{\"status\": \"UP\", \"message\": \"Serviço de autenticação está funcionando\", \"timestamp\": \"2024-08-16T00:24:25.612\"}"
                )
            )
        )
    })
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Serviço de autenticação está funcionando");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }
}
