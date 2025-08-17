package com.lucas.text_processing_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.lucas.text_processing_api.dto.AnagramResponse;

/**
 * Teste de integração para Redis usando Redis Embedded
 * 
 * Este teste usa o mesmo Redis Embedded que a aplicação
 * está usando, garantindo testes robustos sem dependências
 * externas como Docker.
 * 
 * @author Lucas
 * @version 2.0
 */
@SpringBootTest
@ActiveProfiles("test")
class RedisIntegrationTest {

    @Autowired
    private AnagramService anagramService;

    @Autowired
    private RedisCacheService cacheService;

    @BeforeEach
    void setUp() {
        // Limpar cache antes de cada teste
        cacheService.clearCache();
    }

    @Test
    @DisplayName("Deve gerar anagramas e salvar no cache Redis Embedded")
    void shouldGenerateAnagramsAndSaveToRedisCache() {
        // Arrange
        String letters = "abc";

        // Act
        AnagramResponse response = anagramService.generateAnagrams(letters);

        // Assert
        assertNotNull(response);
        assertEquals("abc", response.getOriginalLetters());
        assertEquals(6, response.getTotalAnagrams());
        assertFalse(response.isFromCache());
        assertTrue(response.getProcessingTimeMs() >= 0);

        // Verificar se foi salvo no cache
        AnagramResponse cachedResponse = cacheService.getFromCache(letters);
        assertNotNull(cachedResponse);
        assertEquals("abc", cachedResponse.getOriginalLetters());
        assertEquals(6, cachedResponse.getTotalAnagrams());
    }

    @Test
    @DisplayName("Deve retornar anagramas do cache Redis Embedded")
    void shouldReturnAnagramsFromRedisCache() {
        // Arrange
        String letters = "test";
        
        // Primeira chamada - gera e salva no cache
        AnagramResponse firstResponse = anagramService.generateAnagrams(letters);
        assertNotNull(firstResponse);
        assertFalse(firstResponse.isFromCache());

        // Act - segunda chamada deve vir do cache
        AnagramResponse secondResponse = anagramService.generateAnagrams(letters);

        // Assert
        assertNotNull(secondResponse);
        assertEquals("test", secondResponse.getOriginalLetters());
        assertEquals(12, secondResponse.getTotalAnagrams()); // "test" tem 4 letras, mas "t" se repete, então 4!/2! = 12
        assertTrue(secondResponse.isFromCache());
    }

    @Test
    @DisplayName("Deve funcionar com diferentes tamanhos de entrada")
    void shouldWorkWithDifferentInputSizes() {
        // Teste com 1 letra
        AnagramResponse singleLetter = anagramService.generateAnagrams("a");
        assertNotNull(singleLetter);
        assertEquals(1, singleLetter.getTotalAnagrams());

        // Teste com 2 letras
        AnagramResponse twoLetters = anagramService.generateAnagrams("ab");
        assertNotNull(twoLetters);
        assertEquals(2, twoLetters.getTotalAnagrams());

        // Teste com 3 letras
        AnagramResponse threeLetters = anagramService.generateAnagrams("abc");
        assertNotNull(threeLetters);
        assertEquals(6, threeLetters.getTotalAnagrams());

        // Teste com 4 letras
        AnagramResponse fourLetters = anagramService.generateAnagrams("test");
        assertNotNull(fourLetters);
        assertEquals(12, fourLetters.getTotalAnagrams()); // "test" tem 4 letras, mas "t" se repete, então 4!/2! = 12
    }

    @Test
    @DisplayName("Deve normalizar letras para minúsculas")
    void shouldNormalizeLettersToLowerCase() {
        // Arrange
        String mixedCaseLetters = "AbC";

        // Act
        AnagramResponse response = anagramService.generateAnagrams(mixedCaseLetters);

        // Assert
        assertNotNull(response);
        assertEquals("abc", response.getOriginalLetters());
        assertEquals(6, response.getTotalAnagrams());

        // Verificar se foi salvo no cache com a chave normalizada
        AnagramResponse cachedResponse = cacheService.getFromCache("abc");
        assertNotNull(cachedResponse);
        assertEquals("abc", cachedResponse.getOriginalLetters());
    }

    @Test
    @DisplayName("Deve limpar cache corretamente")
    void shouldClearCacheCorrectly() {
        // Arrange
        String letters = "xyz";
        anagramService.generateAnagrams(letters);

        // Verificar se está no cache
        assertNotNull(cacheService.getFromCache(letters));

        // Act
        cacheService.clearCache();

        // Assert
        assertNull(cacheService.getFromCache(letters));
    }

    @Test
    @DisplayName("Deve remover item específico do cache")
    void shouldRemoveSpecificItemFromCache() {
        // Arrange
        String letters1 = "abc";
        String letters2 = "def";
        
        anagramService.generateAnagrams(letters1);
        anagramService.generateAnagrams(letters2);

        // Verificar se ambos estão no cache
        assertNotNull(cacheService.getFromCache(letters1));
        assertNotNull(cacheService.getFromCache(letters2));

        // Act - remover apenas "abc"
        cacheService.removeFromCache(letters1);

        // Assert
        assertNull(cacheService.getFromCache(letters1));
        assertNotNull(cacheService.getFromCache(letters2)); // "def" ainda deve estar lá
    }

    @Test
    @DisplayName("Deve verificar se cache está habilitado")
    void shouldCheckIfCacheIsEnabled() {
        // Act & Assert
        assertTrue(cacheService.isCacheEnabled());
    }

    @Test
    @DisplayName("Deve lidar com cache miss e hit corretamente")
    void shouldHandleCacheMissAndHitCorrectly() {
        // Arrange
        String letters = "hello";

        // Primeira chamada - cache miss
        AnagramResponse firstResponse = anagramService.generateAnagrams(letters);
        assertNotNull(firstResponse);
        assertFalse(firstResponse.isFromCache());

        // Segunda chamada - cache hit
        AnagramResponse secondResponse = anagramService.generateAnagrams(letters);
        assertNotNull(secondResponse);
        assertTrue(secondResponse.isFromCache());

        // Verificar se os dados são consistentes
        assertEquals(firstResponse.getTotalAnagrams(), secondResponse.getTotalAnagrams());
        assertEquals(firstResponse.getOriginalLetters(), secondResponse.getOriginalLetters());
        assertEquals(firstResponse.getAnagrams(), secondResponse.getAnagrams());
    }

    @Test
    @DisplayName("Deve testar performance do cache Redis Embedded")
    void shouldTestCachePerformance() {
        // Arrange
        String letters = "hello"; // Usando uma palavra menor para evitar timeout
        
        // Primeira chamada - sem cache
        long startTime = System.currentTimeMillis();
        AnagramResponse firstResponse = anagramService.generateAnagrams(letters);
        long firstCallTime = System.currentTimeMillis() - startTime;
        
        // Segunda chamada - com cache
        startTime = System.currentTimeMillis();
        AnagramResponse secondResponse = anagramService.generateAnagrams(letters);
        long secondCallTime = System.currentTimeMillis() - startTime;
        
        // Assert
        assertNotNull(firstResponse);
        assertNotNull(secondResponse);
        assertTrue(secondCallTime < firstCallTime, 
            "Chamada com cache deve ser mais rápida. Primeira: " + firstCallTime + "ms, Segunda: " + secondCallTime + "ms");
    }

    @Test
    @DisplayName("Deve lidar com múltiplas operações simultâneas no cache")
    void shouldHandleMultipleConcurrentOperations() {
        // Arrange
        String[] testInputs = {"a", "ab", "abc", "test", "hello"};
        
        // Act - gerar anagramas para múltiplas entradas
        for (String input : testInputs) {
            AnagramResponse response = anagramService.generateAnagrams(input);
            assertNotNull(response);
            assertFalse(response.isFromCache());
        }
        
        // Verificar se todos estão no cache
        for (String input : testInputs) {
            AnagramResponse cachedResponse = cacheService.getFromCache(input);
            assertNotNull(cachedResponse, "Cache deve conter resposta para: " + input);
        }
        
        // Segunda chamada para todas - deve vir do cache
        for (String input : testInputs) {
            AnagramResponse response = anagramService.generateAnagrams(input);
            assertNotNull(response);
            assertTrue(response.isFromCache(), "Resposta para '" + input + "' deve vir do cache");
        }
    }
}
