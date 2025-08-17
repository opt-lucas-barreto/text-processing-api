package com.lucas.text_processing_api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucas.text_processing_api.dto.AuthRequest;
import com.lucas.text_processing_api.dto.AnagramRequest;

/**
 * Testes de integração para verificar o fluxo completo da aplicação
 * 
 * @author Lucas
 * @version 1.0
 */
@SpringBootTest
@ActiveProfiles("test")
class IntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Deve registrar usuário e fazer login com sucesso")
    void shouldRegisterUserAndLoginSuccessfully() throws Exception {
        // Arrange - Dados de teste
        AuthRequest registerRequest = new AuthRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("testpass123");

        // Act & Assert - Registro
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.message").exists());

        // Act & Assert - Login
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Deve retornar erro 400 para registro com username vazio")
    void shouldReturnBadRequestForEmptyUsername() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("");
        request.setPassword("testpass123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 400 para registro com password vazio")
    void shouldReturnBadRequestForEmptyPassword() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 400 para registro com username muito curto")
    void shouldReturnBadRequestForShortUsername() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("ab");
        request.setPassword("testpass123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 400 para registro com password muito curto")
    void shouldReturnBadRequestForShortPassword() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("12345");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 400 para registro com caracteres especiais no username")
    void shouldReturnBadRequestForSpecialCharactersInUsername() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("test-user");
        request.setPassword("testpass123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 400 para anagramas com caracteres inválidos")
    void shouldReturnBadRequestForInvalidAnagramCharacters() throws Exception {
        // Arrange
        AnagramRequest request = new AnagramRequest();
        request.setLetters("123");

        // Act & Assert
        mockMvc.perform(post("/api/anagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 400 para anagramas com espaços")
    void shouldReturnBadRequestForAnagramWithSpaces() throws Exception {
        // Arrange
        AnagramRequest request = new AnagramRequest();
        request.setLetters("a b c");

        // Act & Assert
        mockMvc.perform(post("/api/anagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 400 para anagramas vazios")
    void shouldReturnBadRequestForEmptyAnagram() throws Exception {
        // Arrange
        AnagramRequest request = new AnagramRequest();
        request.setLetters("");

        // Act & Assert
        mockMvc.perform(post("/api/anagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
