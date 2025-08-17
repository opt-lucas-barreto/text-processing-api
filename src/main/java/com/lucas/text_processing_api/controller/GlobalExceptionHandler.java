package com.lucas.text_processing_api.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.lucas.text_processing_api.dto.AuthResponse;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler global de exceções para tratar erros de validação e outras exceções
 * 
 * @author Lucas
 * @version 1.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Trata exceções de validação de argumentos de método
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("Erro de validação: {}", errors);
        
        String message = "Dados inválidos: ";
        if (errors.containsKey("username")) {
            message += errors.get("username");
        } else if (errors.containsKey("password")) {
            message += errors.get("password");
        } else {
            message += "Verifique os dados informados";
        }
        
        return ResponseEntity.badRequest()
            .body(new AuthResponse(null, null, null, null, message));
    }

    /**
     * Trata exceções de violação de constraints
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<AuthResponse> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Violação de constraint: {}", ex.getMessage());
        
        return ResponseEntity.badRequest()
            .body(new AuthResponse(null, null, null, null, "Dados inválidos: " + ex.getMessage()));
    }

    /**
     * Trata exceções genéricas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponse> handleGenericException(Exception ex) {
        log.error("Erro genérico: {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new AuthResponse(null, null, null, null, "Erro interno do servidor"));
    }
}
