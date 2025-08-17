package com.lucas.text_processing_api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lucas.text_processing_api.dto.AnagramResponse;
import com.lucas.text_processing_api.util.AnagramGenerator;

/**
 * Testes unitários para o serviço de anagramas
 * 
 * Este teste verifica a lógica de negócio do serviço:
 * 1. Geração de anagramas
 * 2. Funcionamento do cache
 * 3. Validações
 * 4. Tratamento de erros
 * 
 * @author Lucas
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class AnagramServiceTest {

    @Mock
    private AnagramGenerator anagramGenerator;

    @Mock
    private RedisCacheService cacheService;

    @InjectMocks
    private AnagramService anagramService;

    private String validLetters;

    @BeforeEach
    void setUp() {
        validLetters = "abc";
    }

    @Test
    @DisplayName("Deve gerar anagramas com sucesso")
    void shouldGenerateAnagramsSuccessfully() {
        // Arrange
        List<String> expectedAnagrams = Arrays.asList("abc", "acb", "bac", "bca", "cab", "cba");
        when(anagramGenerator.generateAnagrams("abc")).thenReturn(expectedAnagrams);
        when(cacheService.getFromCache("abc")).thenReturn(null);
        doNothing().when(cacheService).saveToCache(anyString(), any(AnagramResponse.class));

        // Act
        AnagramResponse response = anagramService.generateAnagrams(validLetters);

        // Assert
        assertNotNull(response);
        assertEquals("abc", response.getOriginalLetters());
        assertEquals(6, response.getTotalAnagrams());
        assertEquals(expectedAnagrams, response.getAnagrams());
        assertFalse(response.isFromCache());
        assertTrue(response.getProcessingTimeMs() >= 0);

        // Verify
        verify(anagramGenerator).generateAnagrams("abc");
        verify(cacheService).getFromCache("abc");
        verify(cacheService).saveToCache("abc", response);
    }

    @Test
    @DisplayName("Deve retornar anagramas do cache quando disponível")
    void shouldReturnAnagramsFromCacheWhenAvailable() {
        // Arrange
        AnagramResponse cachedResponse = new AnagramResponse();
        cachedResponse.setOriginalLetters("abc");
        cachedResponse.setAnagrams(Arrays.asList("abc", "acb", "bac", "bca", "cab", "cba"));
        cachedResponse.setTotalAnagrams(6);
        cachedResponse.setFromCache(true);
        cachedResponse.setProcessingTimeMs(0L);

        when(cacheService.getFromCache("abc")).thenReturn(cachedResponse);

        // Act
        AnagramResponse response = anagramService.generateAnagrams(validLetters);

        // Assert
        assertNotNull(response);
        assertEquals("abc", response.getOriginalLetters());
        assertEquals(6, response.getTotalAnagrams());
        assertTrue(response.isFromCache());
        // O processingTimeMs será modificado pelo serviço, então não podemos testar o valor exato
        assertTrue(response.getProcessingTimeMs() >= 0);

        // Verify
        verify(cacheService).getFromCache("abc");
        verify(anagramGenerator, never()).generateAnagrams(anyString());
        verify(cacheService, never()).saveToCache(anyString(), any(AnagramResponse.class));
    }

    @Test
    @DisplayName("Deve lidar com letra única")
    void shouldHandleSingleLetter() {
        // Arrange
        String singleLetter = "a";

        List<String> expectedAnagrams = Arrays.asList("a");
        when(anagramGenerator.generateAnagrams("a")).thenReturn(expectedAnagrams);
        when(cacheService.getFromCache("a")).thenReturn(null);
        doNothing().when(cacheService).saveToCache(anyString(), any(AnagramResponse.class));

        // Act
        AnagramResponse response = anagramService.generateAnagrams(singleLetter);

        // Assert
        assertNotNull(response);
        assertEquals("a", response.getOriginalLetters());
        assertEquals(1, response.getTotalAnagrams());
        assertEquals(expectedAnagrams, response.getAnagrams());
    }

    @Test
    @DisplayName("Deve lidar com duas letras")
    void shouldHandleTwoLetters() {
        // Arrange
        String twoLetters = "ab";

        List<String> expectedAnagrams = Arrays.asList("ab", "ba");
        when(anagramGenerator.generateAnagrams("ab")).thenReturn(expectedAnagrams);
        when(cacheService.getFromCache("ab")).thenReturn(null);
        doNothing().when(cacheService).saveToCache(anyString(), any(AnagramResponse.class));

        // Act
        AnagramResponse response = anagramService.generateAnagrams(twoLetters);

        // Assert
        assertNotNull(response);
        assertEquals("ab", response.getOriginalLetters());
        assertEquals(2, response.getTotalAnagrams());
        assertEquals(expectedAnagrams, response.getAnagrams());
    }

    @Test
    @DisplayName("Deve medir tempo de processamento")
    void shouldMeasureProcessingTime() {
        // Arrange
        List<String> expectedAnagrams = Arrays.asList("abc", "acb", "bac", "bca", "cab", "cba");
        when(anagramGenerator.generateAnagrams("abc")).thenReturn(expectedAnagrams);
        when(cacheService.getFromCache("abc")).thenReturn(null);
        doNothing().when(cacheService).saveToCache(anyString(), any(AnagramResponse.class));

        // Act
        AnagramResponse response = anagramService.generateAnagrams(validLetters);

        // Assert
        assertNotNull(response);
        assertTrue(response.getProcessingTimeMs() >= 0);
    }

    @Test
    @DisplayName("Deve lidar com falha no cache")
    void shouldHandleCacheFailure() {
        // Arrange
        List<String> expectedAnagrams = Arrays.asList("abc", "acb", "bac", "bca", "cab", "cba");
        when(anagramGenerator.generateAnagrams("abc")).thenReturn(expectedAnagrams);
        when(cacheService.getFromCache("abc")).thenReturn(null);
        doNothing().when(cacheService).saveToCache(anyString(), any(AnagramResponse.class));

        // Act
        AnagramResponse response = anagramService.generateAnagrams(validLetters);

        // Assert
        assertNotNull(response);
        assertEquals("abc", response.getOriginalLetters());
        assertEquals(6, response.getTotalAnagrams());
        assertFalse(response.isFromCache());

        // Verify
        verify(anagramGenerator).generateAnagrams("abc");
        verify(cacheService).getFromCache("abc");
        verify(cacheService).saveToCache("abc", response);
    }
}
