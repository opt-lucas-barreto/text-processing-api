package com.lucas.text_processing_api.controller;

import com.lucas.text_processing_api.dto.AnagramRequest;
import com.lucas.text_processing_api.dto.AnagramResponse;
import com.lucas.text_processing_api.service.AnagramService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller REST para geração de anagramas
 * 
 * Este controller fornece endpoints para geração de anagramas,
 * gerenciamento de cache e informações sobre a aplicação.
 * Todos os endpoints (exceto health) requerem autenticação JWT.
 * 
 * @author Lucas
 * @version 2.5
 */
@RestController
@RequestMapping("/api/anagrams")
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Anagramas", description = "Endpoints para geração de anagramas e gerenciamento de cache")
public class AnagramController {

    @Autowired
    private AnagramService anagramService;

    /**
     * Endpoint principal para geração de anagramas
     * 
     * Este endpoint aceita uma requisição POST com letras e retorna
     * todos os anagramas possíveis, utilizando cache quando disponível.
     * Requer autenticação JWT válida.
     * 
     * @param request requisição contendo as letras para geração de anagramas
     * @return ResponseEntity contendo os anagramas gerados
     */
    @Operation(
        summary = "Gerar anagramas",
        description = "Endpoint para geração de anagramas com cache inteligente"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Anagramas gerados com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AnagramResponse.class),
                examples = @ExampleObject(
                    name = "Anagramas gerados",
                    value = "{\"originalLetters\": \"abc\", \"anagrams\": [\"abc\", \"acb\", \"bac\", \"bca\", \"cab\", \"cba\"], \"totalAnagrams\": 6, \"fromCache\": false, \"processingTimeMs\": 5}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados de entrada inválidos"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor"
        )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/generate")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<AnagramResponse> generateAnagrams(
        @Parameter(description = "Letras para geração de anagramas", required = true)
        @Valid @RequestBody AnagramRequest request) {
        try {
            log.info("Recebida requisição para gerar anagramas: {}", request.getLetters());
            
            AnagramResponse response = anagramService.generateAnagrams(request.getLetters());
            
            log.info("Anagramas gerados com sucesso. Total: {}", response.getTotalAnagrams());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Requisição inválida: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erro interno ao gerar anagramas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint para geração de anagramas sem cache
     * 
     * Este endpoint força a geração de novos anagramas, ignorando o cache.
     * Útil para testes ou quando se deseja sempre gerar novos resultados.
     * Requer autenticação JWT válida.
     * 
     * @param request requisição contendo as letras para geração de anagramas
     * @return ResponseEntity contendo os anagramas gerados
     */
    @PostMapping("/generate-no-cache")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<AnagramResponse> generateAnagramsWithoutCache(@Valid @RequestBody AnagramRequest request) {
        try {
            log.info("Recebida requisição para gerar anagramas sem cache: {}", request.getLetters());
            
            AnagramResponse response = anagramService.generateAnagramsWithoutCache(request.getLetters());
            
            log.info("Anagramas gerados sem cache com sucesso. Total: {}", response.getTotalAnagrams());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Requisição inválida: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erro interno ao gerar anagramas sem cache: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint para remover anagramas específicos do cache
     * 
     * Requer autenticação JWT válida e papel de ADMIN.
     * 
     * @param letters letras para remover do cache
     * @return ResponseEntity indicando sucesso da operação
     */
    @DeleteMapping("/cache/{letters}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> removeFromCache(@PathVariable String letters) {
        try {
            log.info("Recebida requisição para remover do cache: {}", letters);
            
            anagramService.removeFromCache(letters);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Anagramas removidos do cache com sucesso");
            response.put("letters", letters);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao remover do cache: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint para limpar todo o cache de anagramas
     * 
     * Requer autenticação JWT válida e papel de ADMIN.
     * 
     * @return ResponseEntity indicando sucesso da operação
     */
    @DeleteMapping("/cache")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> clearCache() {
        try {
            log.info("Recebida requisição para limpar cache");
            
            anagramService.clearCache();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cache de anagramas limpo com sucesso");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao limpar cache: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint para verificar o status do cache
     * 
     * Requer autenticação JWT válida.
     * 
     * @return ResponseEntity contendo informações sobre o cache
     */
    @GetMapping("/cache/status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCacheStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("cacheEnabled", anagramService.isCacheEnabled());
            status.put("message", "Status do cache recuperado com sucesso");
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("Erro ao verificar status do cache: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint para calcular o número total de anagramas possíveis
     * 
     * Requer autenticação JWT válida.
     * 
     * @param letters letras para calcular o total de anagramas
     * @return ResponseEntity contendo o total de anagramas possíveis
     */
    @GetMapping("/calculate-total/{letters}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> calculateTotalAnagrams(@PathVariable String letters) {
        try {
            log.info("Recebida requisição para calcular total de anagramas: {}", letters);
            
            long total = anagramService.calculateTotalAnagrams(letters);
            
            Map<String, Object> response = new HashMap<>();
            response.put("letters", letters);
            response.put("totalAnagrams", total);
            response.put("message", "Total de anagramas calculado com sucesso");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Requisição inválida: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erro ao calcular total de anagramas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint de health check
     * 
     * Este endpoint não requer autenticação e pode ser usado
     * para verificar se a aplicação está funcionando.
     * 
     * @return ResponseEntity indicando que a aplicação está funcionando
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Text Processing API está funcionando");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }
}
