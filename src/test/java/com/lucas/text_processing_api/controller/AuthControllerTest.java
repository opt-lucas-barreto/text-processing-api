package com.lucas.text_processing_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucas.text_processing_api.dto.AuthRequest;
import com.lucas.text_processing_api.dto.AuthResponse;
import com.lucas.text_processing_api.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes unitários para o AuthController
 * 
 * @author Lucas
 * @version 2.5
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    @DisplayName("Deve fazer login com sucesso")
    void shouldLoginSuccessfully() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("testpass");

        AuthResponse response = new AuthResponse("jwt-token", "Bearer", "testuser", "USER", "Login realizado com sucesso");
        when(authService.authenticate(any(AuthRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("Deve retornar erro para credenciais inválidas")
    void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpass");

        when(authService.authenticate(any(AuthRequest.class)))
                .thenThrow(new RuntimeException("Credenciais inválidas"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve registrar usuário com sucesso")
    void shouldRegisterUserSuccessfully() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("newuser");
        request.setPassword("newpass");

        AuthResponse response = new AuthResponse("jwt-token", "Bearer", "newuser", "USER", "Usuário criado com sucesso");
        when(authService.createUser(any(AuthRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @DisplayName("Deve retornar erro para registro inválido")
    void shouldReturnBadRequestForInvalidRegistration() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("existinguser");
        request.setPassword("pass");

        when(authService.createUser(any(AuthRequest.class)))
                .thenThrow(new RuntimeException("Nome de usuário já existe"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve validar token com sucesso")
    void shouldValidateTokenSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Token JWT válido"))
                .andExpect(jsonPath("$.status").value("AUTHORIZED"));
    }

    @Test
    @DisplayName("Deve retornar health check com sucesso")
    void shouldReturnHealthCheckSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.message").value("Serviço de autenticação está funcionando"));
    }

    @Test
    @DisplayName("Deve validar campos obrigatórios")
    void shouldValidateRequiredFields() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        // Campos vazios para testar validação

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
