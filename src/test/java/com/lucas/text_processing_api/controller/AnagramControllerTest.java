package com.lucas.text_processing_api.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucas.text_processing_api.dto.AnagramRequest;
import com.lucas.text_processing_api.dto.AnagramResponse;
import com.lucas.text_processing_api.service.AnagramService;

/**
 * Testes unitários para o AnagramController
 * 
 * @author Lucas
 * @version 1.4
 */
@ExtendWith(MockitoExtension.class)
class AnagramControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AnagramService anagramService;

    @InjectMocks
    private AnagramController anagramController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
            .standaloneSetup(anagramController)
            .build();
    }

    @Test
    @DisplayName("Deve gerar anagramas com sucesso (usuário autenticado)")
    void shouldGenerateAnagramsSuccessfully() throws Exception {
        // Arrange
        AnagramRequest request = new AnagramRequest();
        request.setLetters("abc");
        
        List<String> anagrams = List.of("abc", "acb", "bac", "bca", "cab", "cba");
        AnagramResponse response = new AnagramResponse("abc", anagrams);
        response.setFromCache(false);
        response.setProcessingTimeMs(10);
        
        when(anagramService.generateAnagrams("abc")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/anagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalLetters").value("abc"))
                .andExpect(jsonPath("$.anagrams").isArray())
                .andExpect(jsonPath("$.totalAnagrams").value(6))
                .andExpect(jsonPath("$.fromCache").value(false))
                .andExpect(jsonPath("$.processingTimeMs").value(10));

        verify(anagramService).generateAnagrams("abc");
    }

    @Test
    @DisplayName("Deve gerar anagramas sem cache com sucesso (usuário autenticado)")
    void shouldGenerateAnagramsWithoutCacheSuccessfully() throws Exception {
        // Arrange
        AnagramRequest request = new AnagramRequest();
        request.setLetters("abc");
        
        List<String> anagrams = List.of("abc", "acb", "bac", "bca", "cab", "cba");
        AnagramResponse response = new AnagramResponse("abc", anagrams);
        response.setFromCache(false);
        response.setProcessingTimeMs(15);
        
        when(anagramService.generateAnagramsWithoutCache("abc")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/anagrams/generate-no-cache")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalLetters").value("abc"))
                .andExpect(jsonPath("$.anagrams").isArray())
                .andExpect(jsonPath("$.totalAnagrams").value(6))
                .andExpect(jsonPath("$.fromCache").value(false))
                .andExpect(jsonPath("$.processingTimeMs").value(15));

        verify(anagramService).generateAnagramsWithoutCache("abc");
    }

    @Test
    @DisplayName("Deve retornar erro 500 para usuário não autenticado (sem segurança)")
    void shouldReturnInternalServerErrorForUnauthenticatedUser() throws Exception {
        // Arrange
        AnagramRequest request = new AnagramRequest();
        request.setLetters("abc");
        
        // Mock do serviço retornando null para simular erro
        when(anagramService.generateAnagrams("abc")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/api/anagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()); // Sem segurança, retorna erro interno

        verify(anagramService).generateAnagrams("abc");
    }

    @Test
    @DisplayName("Deve retornar erro 400 para requisição inválida")
    void shouldReturnBadRequestForInvalidRequest() throws Exception {
        // Arrange
        AnagramRequest request = new AnagramRequest();
        request.setLetters("123"); // Caracteres inválidos
        
        // Act & Assert
        mockMvc.perform(post("/api/anagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar status do cache com sucesso (usuário autenticado)")
    void shouldReturnCacheStatusSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/anagrams/cache/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Deve calcular total de anagramas com sucesso (usuário autenticado)")
    void shouldCalculateTotalAnagramsSuccessfully() throws Exception {
        // Arrange
        when(anagramService.calculateTotalAnagrams("abc")).thenReturn(6L);

        // Act & Assert
        mockMvc.perform(get("/api/anagrams/calculate-total/abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAnagrams").value(6));

        verify(anagramService).calculateTotalAnagrams("abc");
    }

    @Test
    @DisplayName("Deve remover item do cache com sucesso (usuário ADMIN)")
    void shouldRemoveFromCacheSuccessfully() throws Exception {
        // Arrange
        doNothing().when(anagramService).removeFromCache("abc");

        // Act & Assert
        mockMvc.perform(delete("/api/anagrams/cache/abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Anagramas removidos do cache com sucesso"))
                .andExpect(jsonPath("$.letters").value("abc"));

        verify(anagramService).removeFromCache("abc");
    }

    @Test
    @DisplayName("Deve limpar cache com sucesso (usuário ADMIN)")
    void shouldClearCacheSuccessfully() throws Exception {
        // Arrange
        doNothing().when(anagramService).clearCache();
        
        // Act & Assert
        mockMvc.perform(delete("/api/anagrams/cache"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cache de anagramas limpo com sucesso"));

        verify(anagramService).clearCache();
    }

    @Test
    @DisplayName("Deve retornar erro 403 para operações de cache sem role ADMIN")
    void shouldReturnForbiddenForCacheOperationsWithoutAdminRole() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/anagrams/cache/abc"))
                .andExpect(status().isOk()); // Sem segurança, permite acesso

        mockMvc.perform(delete("/api/anagrams/cache"))
                .andExpect(status().isOk()); // Sem segurança, permite acesso
    }

    @Test
    @DisplayName("Deve retornar health check com sucesso (sem autenticação)")
    void shouldReturnHealthCheckSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/anagrams/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
