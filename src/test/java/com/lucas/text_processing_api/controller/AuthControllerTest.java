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
 * @version 3.0
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
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.message").value("Login realizado com sucesso"));
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
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
    }

    @Test
    @DisplayName("Deve retornar erro para usuário não encontrado")
    void shouldReturnUnauthorizedForUserNotFound() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("nonexistent");
        request.setPassword("testpass");

        when(authService.authenticate(any(AuthRequest.class)))
                .thenThrow(new RuntimeException("Usuário não encontrado"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
    }

    @Test
    @DisplayName("Deve retornar erro 400 para username vazio")
    void shouldReturnBadRequestForEmptyUsername() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("");
        request.setPassword("testpass");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        // Não podemos verificar a mensagem específica pois o Spring intercepta antes
    }

    @Test
    @DisplayName("Deve retornar erro 400 para password vazio")
    void shouldReturnBadRequestForEmptyPassword() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        // Não podemos verificar a mensagem específica pois o Spring intercepta antes
    }

    @Test
    @DisplayName("Deve retornar erro 400 para username com espaços")
    void shouldReturnBadRequestForUsernameWithSpaces() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("test user");
        request.setPassword("testpass");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username deve conter apenas letras, números e underscore"));
    }

    @Test
    @DisplayName("Deve retornar erro 400 para username com caracteres especiais")
    void shouldReturnBadRequestForUsernameWithSpecialCharacters() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("test@user");
        request.setPassword("testpass");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username deve conter apenas letras, números e underscore"));
    }

    @Test
    @DisplayName("Deve retornar erro 400 para username null")
    void shouldReturnBadRequestForNullUsername() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername(null);
        request.setPassword("testpass");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        // Não podemos verificar a mensagem específica pois o Spring intercepta antes
    }

    @Test
    @DisplayName("Deve retornar erro 400 para password null")
    void shouldReturnBadRequestForNullPassword() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        // Não podemos verificar a mensagem específica pois o Spring intercepta antes
    }

    @Test
    @DisplayName("Deve registrar usuário com sucesso")
    void shouldRegisterUserSuccessfully() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("newuser");
        request.setPassword("newpass123");

        AuthResponse response = new AuthResponse("jwt-token", "Bearer", "newuser", "USER", "Usuário criado com sucesso");
        when(authService.createUser(any(AuthRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.message").value("Usuário criado com sucesso"));
    }

    @Test
    @DisplayName("Deve retornar erro 400 para registro com username muito curto")
    void shouldReturnBadRequestForShortUsername() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("ab");
        request.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username deve ter pelo menos 3 caracteres"));
    }

    @Test
    @DisplayName("Deve retornar erro 400 para registro com password muito curto")
    void shouldReturnBadRequestForShortPassword() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password deve ter pelo menos 6 caracteres"));
    }

    @Test
    @DisplayName("Deve retornar erro 400 para registro com username já existente")
    void shouldReturnBadRequestForExistingUsername() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");

        when(authService.createUser(any(AuthRequest.class)))
                .thenThrow(new RuntimeException("Nome de usuário já existe"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nome de usuário já existe"));
    }

    @Test
    @DisplayName("Deve retornar erro 400 para registro com username com caracteres especiais")
    void shouldReturnBadRequestForRegisterUsernameWithSpecialCharacters() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("user-name");
        request.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username deve conter apenas letras, números e underscore"));
    }

    @Test
    @DisplayName("Deve retornar erro 400 para registro com username vazio")
    void shouldReturnBadRequestForRegisterEmptyUsername() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("");
        request.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        // Não podemos verificar a mensagem específica pois o Spring intercepta antes
    }

    @Test
    @DisplayName("Deve retornar erro 400 para registro com password vazio")
    void shouldReturnBadRequestForRegisterEmptyPassword() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        // Não podemos verificar a mensagem específica pois o Spring intercepta antes
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
    @DisplayName("Deve validar campos obrigatórios com @Valid")
    void shouldValidateRequiredFieldsWithValidAnnotation() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        // Campos vazios para testar validação do @Valid

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 400 para erro genérico no login")
    void shouldReturnBadRequestForGenericLoginError() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("testpass");

        when(authService.authenticate(any(AuthRequest.class)))
                .thenThrow(new RuntimeException("Erro interno do sistema"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro na autenticação: Erro interno do sistema"));
    }

    @Test
    @DisplayName("Deve retornar erro 400 para erro genérico no registro")
    void shouldReturnBadRequestForGenericRegisterError() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(authService.createUser(any(AuthRequest.class)))
                .thenThrow(new RuntimeException("Erro na base de dados"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro no registro: Erro na base de dados"));
    }
}
