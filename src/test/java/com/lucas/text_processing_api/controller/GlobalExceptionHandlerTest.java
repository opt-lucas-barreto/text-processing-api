package com.lucas.text_processing_api.controller;

import com.lucas.text_processing_api.dto.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o GlobalExceptionHandler
 * 
 * @author Lucas
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Deve tratar exceção de violação de constraint")
    void shouldHandleConstraintViolationException() {
        // Arrange
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolationException ex = new ConstraintViolationException("Violação de constraint", violations);

        // Act
        ResponseEntity<AuthResponse> response = exceptionHandler.handleConstraintViolation(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Dados inválidos: Violação de constraint", response.getBody().getMessage());
        assertNull(response.getBody().getToken());
        assertNull(response.getBody().getUsername());
        assertNull(response.getBody().getRole());
    }

    @Test
    @DisplayName("Deve tratar exceção genérica")
    void shouldHandleGenericException() {
        // Arrange
        Exception ex = new RuntimeException("Erro interno do sistema");

        // Act
        ResponseEntity<AuthResponse> response = exceptionHandler.handleGenericException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Erro interno do servidor", response.getBody().getMessage());
        assertNull(response.getBody().getToken());
        assertNull(response.getBody().getUsername());
        assertNull(response.getBody().getRole());
    }

    @Test
    @DisplayName("Deve tratar exceção com mensagem vazia")
    void shouldHandleExceptionWithEmptyMessage() {
        // Arrange
        Exception ex = new RuntimeException("");

        // Act
        ResponseEntity<AuthResponse> response = exceptionHandler.handleGenericException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Erro interno do servidor", response.getBody().getMessage());
    }
}
