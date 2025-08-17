package com.lucas.text_processing_api.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
 * @version 1.5
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
    @DisplayName("Deve gerar anagramas com sucesso")
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
    @DisplayName("Deve retornar erro 400 para requisição vazia")
    void shouldReturnBadRequestForEmptyRequest() throws Exception {
        // Arrange
        AnagramRequest request = new AnagramRequest();
        request.setLetters(""); // String vazia
        
        // Act & Assert
        mockMvc.perform(post("/api/anagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 400 para requisição com espaços")
    void shouldReturnBadRequestForRequestWithSpaces() throws Exception {
        // Arrange
        AnagramRequest request = new AnagramRequest();
        request.setLetters("a b c"); // Com espaços
        
        // Act & Assert
        mockMvc.perform(post("/api/anagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 400 para requisição com caracteres especiais")
    void shouldReturnBadRequestForRequestWithSpecialCharacters() throws Exception {
        // Arrange
        AnagramRequest request = new AnagramRequest();
        request.setLetters("a-b_c"); // Com caracteres especiais
        
        // Act & Assert
        mockMvc.perform(post("/api/anagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 500 quando serviço falha")
    void shouldReturnInternalServerErrorWhenServiceFails() throws Exception {
        // Arrange
        AnagramRequest request = new AnagramRequest();
        request.setLetters("abc");
        
        when(anagramService.generateAnagrams("abc"))
            .thenThrow(new RuntimeException("Erro interno"));

        // Act & Assert
        mockMvc.perform(post("/api/anagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(anagramService).generateAnagrams("abc");
    }

    @Test
    @DisplayName("Deve retornar anagramas do cache quando disponível")
    void shouldReturnAnagramsFromCache() throws Exception {
        // Arrange
        AnagramRequest request = new AnagramRequest();
        request.setLetters("abc");
        
        List<String> anagrams = List.of("abc", "acb", "bac", "bca", "cab", "cba");
        AnagramResponse response = new AnagramResponse("abc", anagrams);
        response.setFromCache(true);
        response.setProcessingTimeMs(5);
        
        when(anagramService.generateAnagrams("abc")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/anagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromCache").value(true))
                .andExpect(jsonPath("$.processingTimeMs").value(5));

        verify(anagramService).generateAnagrams("abc");
    }

    @Test
    @DisplayName("Deve lidar com entrada muito longa")
    void shouldHandleVeryLongInput() throws Exception {
        // Arrange
        AnagramRequest request = new AnagramRequest();
        request.setLetters("abcdefghijklmnopqrstuvwxyz"); // 26 letras
        
        List<String> anagrams = List.of("abc", "def"); // Mock simplificado
        AnagramResponse response = new AnagramResponse("abcdefghijklmnopqrstuvwxyz", anagrams);
        response.setFromCache(false);
        response.setProcessingTimeMs(100);
        
        when(anagramService.generateAnagrams("abcdefghijklmnopqrstuvwxyz")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/anagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalLetters").value("abcdefghijklmnopqrstuvwxyz"));

        verify(anagramService).generateAnagrams("abcdefghijklmnopqrstuvwxyz");
    }

    @Test
    @DisplayName("Deve validar formato JSON da requisição")
    void shouldValidateJsonFormat() throws Exception {
        // Arrange - JSON malformado
        String invalidJson = "{\"letters\": \"abc\",}"; // Vírgula extra
        
        // Act & Assert
        mockMvc.perform(post("/api/anagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve aceitar requisição sem cache")
    void shouldAcceptRequestWithoutCache() throws Exception {
        // Arrange
        AnagramRequest request = new AnagramRequest();
        request.setLetters("xyz");
        
        List<String> anagrams = List.of("xyz", "xzy", "yxz", "yzx", "zxy", "zyx");
        AnagramResponse response = new AnagramResponse("xyz", anagrams);
        response.setFromCache(false);
        response.setProcessingTimeMs(20);
        
        when(anagramService.generateAnagrams("xyz")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/anagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromCache").value(false))
                .andExpect(jsonPath("$.processingTimeMs").value(20));

        verify(anagramService).generateAnagrams("xyz");
    }
}
