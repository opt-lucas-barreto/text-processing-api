package com.lucas.text_processing_api.service;

import com.lucas.text_processing_api.dto.AnagramResponse;
import com.lucas.text_processing_api.util.AnagramGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o serviço de anagramas
 * 
 * Esta classe testa todas as funcionalidades do AnagramService,
 * incluindo integração com cache e geração de anagramas.
 * 
 * @author Lucas
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de Anagramas")
class AnagramServiceTest {

    @Mock
    private AnagramGenerator anagramGenerator;

    @Mock
    private RedisCacheService cacheService;

    @InjectMocks
    private AnagramService anagramService;

    @Test
    @DisplayName("Deve gerar anagramas e salvar no cache quando não estiver em cache")
    void shouldGenerateAnagramsAndSaveToCacheWhenNotInCache() {
        // Arrange
        String letters = "abc";
        List<String> expectedAnagrams = List.of("abc", "acb", "bac", "bca", "cab", "cba");
        
        when(cacheService.getFromCache(letters)).thenReturn(null);
        when(anagramGenerator.generateAnagrams(letters)).thenReturn(expectedAnagrams);
        doNothing().when(cacheService).saveToCache(anyString(), any(AnagramResponse.class));

        // Act
        AnagramResponse result = anagramService.generateAnagrams(letters);

        // Assert
        assertNotNull(result);
        assertEquals(letters.toLowerCase(), result.getOriginalLetters());
        assertEquals(expectedAnagrams, result.getAnagrams());
        assertEquals(expectedAnagrams.size(), result.getTotalAnagrams());
        assertFalse(result.isFromCache());
        assertTrue(result.getProcessingTimeMs() >= 0);

        // Verifica se o cache foi consultado e atualizado
        verify(cacheService).getFromCache(letters);
        verify(anagramGenerator).generateAnagrams(letters);
        verify(cacheService).saveToCache(letters, result);
    }

    @Test
    @DisplayName("Deve retornar anagramas do cache quando disponível")
    void shouldReturnAnagramsFromCacheWhenAvailable() {
        // Arrange
        String letters = "abc";
        List<String> cachedAnagrams = List.of("abc", "acb", "bac", "bca", "cab", "cba");
        AnagramResponse cachedResponse = new AnagramResponse(letters.toLowerCase(), cachedAnagrams);
        cachedResponse.setFromCache(true);
        
        when(cacheService.getFromCache(letters)).thenReturn(cachedResponse);

        // Act
        AnagramResponse result = anagramService.generateAnagrams(letters);

        // Assert
        assertNotNull(result);
        assertEquals(letters.toLowerCase(), result.getOriginalLetters());
        assertEquals(cachedAnagrams, result.getAnagrams());
        assertEquals(cachedAnagrams.size(), result.getTotalAnagrams());
        assertTrue(result.isFromCache());
        assertTrue(result.getProcessingTimeMs() >= 0);

        // Verifica se o cache foi consultado mas não foi atualizado
        verify(cacheService).getFromCache(letters);
        verify(anagramGenerator, never()).generateAnagrams(anyString());
        verify(cacheService, never()).saveToCache(anyString(), any(AnagramResponse.class));
    }

    @Test
    @DisplayName("Deve gerar anagramas sem cache quando solicitado")
    void shouldGenerateAnagramsWithoutCacheWhenRequested() {
        // Arrange
        String letters = "abc";
        List<String> expectedAnagrams = List.of("abc", "acb", "bac", "bca", "cab", "cba");
        
        when(anagramGenerator.generateAnagrams(letters)).thenReturn(expectedAnagrams);

        // Act
        AnagramResponse result = anagramService.generateAnagramsWithoutCache(letters);

        // Assert
        assertNotNull(result);
        assertEquals(letters.toLowerCase(), result.getOriginalLetters());
        assertEquals(expectedAnagrams, result.getAnagrams());
        assertEquals(expectedAnagrams.size(), result.getTotalAnagrams());
        assertFalse(result.isFromCache());
        assertTrue(result.getProcessingTimeMs() >= 0);

        // Verifica se o cache não foi consultado
        verify(cacheService, never()).getFromCache(anyString());
        verify(anagramGenerator).generateAnagrams(letters);
        verify(cacheService, never()).saveToCache(anyString(), any(AnagramResponse.class));
    }

    @Test
    @DisplayName("Deve propagar exceção do gerador de anagramas")
    void shouldPropagateExceptionFromAnagramGenerator() {
        // Arrange
        String letters = "abc";
        String errorMessage = "Erro na geração de anagramas";
        
        when(cacheService.getFromCache(letters)).thenReturn(null);
        when(anagramGenerator.generateAnagrams(letters))
            .thenThrow(new IllegalArgumentException(errorMessage));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> anagramService.generateAnagrams(letters)
        );
        
        assertEquals(errorMessage, exception.getMessage());
        
        // Verifica se o cache foi consultado mas não foi atualizado
        verify(cacheService).getFromCache(letters);
        verify(anagramGenerator).generateAnagrams(letters);
        verify(cacheService, never()).saveToCache(anyString(), any(AnagramResponse.class));
    }

    @Test
    @DisplayName("Deve remover anagramas específicos do cache")
    void shouldRemoveSpecificAnagramsFromCache() {
        // Arrange
        String letters = "abc";
        doNothing().when(cacheService).removeFromCache(letters);

        // Act
        anagramService.removeFromCache(letters);

        // Assert
        verify(cacheService).removeFromCache(letters);
    }

    @Test
    @DisplayName("Deve limpar todo o cache")
    void shouldClearAllCache() {
        // Arrange
        doNothing().when(cacheService).clearCache();

        // Act
        anagramService.clearCache();

        // Assert
        verify(cacheService).clearCache();
    }

    @Test
    @DisplayName("Deve verificar se o cache está habilitado")
    void shouldCheckIfCacheIsEnabled() {
        // Arrange
        boolean expectedCacheEnabled = true;
        when(cacheService.isCacheEnabled()).thenReturn(expectedCacheEnabled);

        // Act
        boolean result = anagramService.isCacheEnabled();

        // Assert
        assertEquals(expectedCacheEnabled, result);
        verify(cacheService).isCacheEnabled();
    }

    @Test
    @DisplayName("Deve calcular o total de anagramas possíveis")
    void shouldCalculateTotalAnagrams() {
        // Arrange
        String letters = "abc";
        long expectedTotal = 6;
        when(anagramGenerator.calculateTotalAnagrams(letters)).thenReturn(expectedTotal);

        // Act
        long result = anagramService.calculateTotalAnagrams(letters);

        // Assert
        assertEquals(expectedTotal, result);
        verify(anagramGenerator).calculateTotalAnagrams(letters);
    }

    @Test
    @DisplayName("Deve lidar com entrada vazia no cálculo de total")
    void shouldHandleEmptyInputInTotalCalculation() {
        // Arrange
        String letters = "";
        long expectedTotal = 0;
        when(anagramGenerator.calculateTotalAnagrams(letters)).thenReturn(expectedTotal);

        // Act
        long result = anagramService.calculateTotalAnagrams(letters);

        // Assert
        assertEquals(expectedTotal, result);
        verify(anagramGenerator).calculateTotalAnagrams(letters);
    }

    @Test
    @DisplayName("Deve lidar com entrada nula no cálculo de total")
    void shouldHandleNullInputInTotalCalculation() {
        // Arrange
        String letters = null;
        long expectedTotal = 0;
        when(anagramGenerator.calculateTotalAnagrams(letters)).thenReturn(expectedTotal);

        // Act
        long result = anagramService.calculateTotalAnagrams(letters);

        // Assert
        assertEquals(expectedTotal, result);
        verify(anagramGenerator).calculateTotalAnagrams(letters);
    }
}
