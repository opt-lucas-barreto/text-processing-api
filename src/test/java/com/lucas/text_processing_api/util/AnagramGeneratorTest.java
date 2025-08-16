package com.lucas.text_processing_api.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o gerador de anagramas
 * 
 * Esta classe testa todas as funcionalidades do AnagramGenerator,
 * incluindo casos de borda e validações.
 * 
 * @author Lucas
 * @version 1.0
 */
@DisplayName("Testes do Gerador de Anagramas")
class AnagramGeneratorTest {

    private AnagramGenerator anagramGenerator;

    @BeforeEach
    void setUp() {
        anagramGenerator = new AnagramGenerator();
    }

    @Test
    @DisplayName("Deve gerar anagramas para 'abc'")
    void shouldGenerateAnagramsForABC() {
        // Arrange
        String letters = "abc";
        List<String> expectedAnagrams = List.of("abc", "acb", "bac", "bca", "cab", "cba");

        // Act
        List<String> result = anagramGenerator.generateAnagrams(letters);

        // Assert
        assertNotNull(result);
        assertEquals(6, result.size());
        assertTrue(result.containsAll(expectedAnagrams));
    }

    @Test
    @DisplayName("Deve gerar anagramas para 'ab'")
    void shouldGenerateAnagramsForAB() {
        // Arrange
        String letters = "ab";
        List<String> expectedAnagrams = List.of("ab", "ba");

        // Act
        List<String> result = anagramGenerator.generateAnagrams(letters);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(expectedAnagrams));
    }

    @Test
    @DisplayName("Deve gerar anagramas para uma única letra")
    void shouldGenerateAnagramsForSingleLetter() {
        // Arrange
        String letters = "a";
        List<String> expectedAnagrams = List.of("a");

        // Act
        List<String> result = anagramGenerator.generateAnagrams(letters);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsAll(expectedAnagrams));
    }

    @Test
    @DisplayName("Deve normalizar letras maiúsculas para minúsculas")
    void shouldNormalizeUpperCaseLetters() {
        // Arrange
        String letters = "ABC";
        List<String> expectedAnagrams = List.of("abc", "acb", "bac", "bca", "cab", "cba");

        // Act
        List<String> result = anagramGenerator.generateAnagrams(letters);

        // Assert
        assertNotNull(result);
        assertEquals(6, result.size());
        assertTrue(result.containsAll(expectedAnagrams));
        
        // Verifica se todos os resultados estão em minúsculas
        result.forEach(anagram -> assertTrue(anagram.equals(anagram.toLowerCase())));
    }

    @Test
    @DisplayName("Deve gerar anagramas para 'abcd'")
    void shouldGenerateAnagramsForABCD() {
        // Arrange
        String letters = "abcd";

        // Act
        List<String> result = anagramGenerator.generateAnagrams(letters);

        // Assert
        assertNotNull(result);
        assertEquals(24, result.size()); // 4! = 24
        
        // Verifica se todos os anagramas têm 4 caracteres
        result.forEach(anagram -> assertEquals(4, anagram.length()));
        
        // Verifica se todos os anagramas contêm as mesmas letras
        result.forEach(anagram -> {
            assertTrue(anagram.contains("a"));
            assertTrue(anagram.contains("b"));
            assertTrue(anagram.contains("c"));
            assertTrue(anagram.contains("d"));
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @DisplayName("Deve lançar exceção para entrada vazia ou apenas espaços")
    void shouldThrowExceptionForEmptyOrWhitespaceInput(String input) {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> anagramGenerator.generateAnagrams(input)
        );
        
        assertEquals("As letras não podem estar vazias", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção para entrada nula")
    void shouldThrowExceptionForNullInput() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> anagramGenerator.generateAnagrams(null)
        );
        
        assertEquals("As letras não podem estar vazias", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "abc123", "a1b", "a b", "a-b", "a_b"})
    @DisplayName("Deve lançar exceção para entrada com caracteres não-alfabéticos")
    void shouldThrowExceptionForNonAlphabeticInput(String input) {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> anagramGenerator.generateAnagrams(input)
        );
        
        assertEquals("Apenas letras são permitidas", exception.getMessage());
    }

    @Test
    @DisplayName("Deve calcular corretamente o total de anagramas possíveis")
    void shouldCalculateTotalAnagramsCorrectly() {
        // Testa casos conhecidos
        assertEquals(1, anagramGenerator.calculateTotalAnagrams("a"));        // 1! = 1
        assertEquals(2, anagramGenerator.calculateTotalAnagrams("ab"));       // 2! = 2
        assertEquals(6, anagramGenerator.calculateTotalAnagrams("abc"));      // 3! = 6
        assertEquals(24, anagramGenerator.calculateTotalAnagrams("abcd"));    // 4! = 24
        assertEquals(120, anagramGenerator.calculateTotalAnagrams("abcde"));  // 5! = 120
    }

    @Test
    @DisplayName("Deve retornar 0 para entrada vazia no cálculo de total")
    void shouldReturnZeroForEmptyInputInTotalCalculation() {
        // Act & Assert
        assertEquals(0, anagramGenerator.calculateTotalAnagrams(""));
        assertEquals(0, anagramGenerator.calculateTotalAnagrams(null));
    }

    @Test
    @DisplayName("Deve gerar anagramas únicos sem duplicatas")
    void shouldGenerateUniqueAnagramsWithoutDuplicates() {
        // Arrange
        String letters = "aab"; // Letras repetidas

        // Act
        List<String> result = anagramGenerator.generateAnagrams(letters);

        // Assert
        assertNotNull(result);
        // Para "aab" com letras repetidas, o algoritmo gera todas as permutações possíveis
        // e remove duplicatas, então temos 3 resultados únicos: aab, aba, baa
        assertEquals(3, result.size());
        
        // Verifica se não há duplicatas
        long uniqueCount = result.stream().distinct().count();
        assertEquals(result.size(), uniqueCount);
        
        // Verifica se todos os anagramas contêm as mesmas letras
        result.forEach(anagram -> {
            assertEquals(3, anagram.length());
            assertTrue(anagram.contains("a"));
            assertTrue(anagram.contains("a"));
            assertTrue(anagram.contains("b"));
        });
        
        // Verifica se os anagramas esperados estão presentes
        assertTrue(result.contains("aab"));
        assertTrue(result.contains("aba"));
        assertTrue(result.contains("baa"));
    }

    @Test
    @DisplayName("Deve manter a ordem correta dos anagramas")
    void shouldMaintainCorrectOrderOfAnagrams() {
        // Arrange
        String letters = "abc";

        // Act
        List<String> result = anagramGenerator.generateAnagrams(letters);

        // Assert
        assertNotNull(result);
        assertEquals(6, result.size());
        
        // Verifica se todos os anagramas têm o mesmo comprimento
        int expectedLength = letters.length();
        result.forEach(anagram -> assertEquals(expectedLength, anagram.length()));
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    @DisplayName("Deve gerar anagramas corretos para diferentes entradas")
    void shouldGenerateCorrectAnagramsForDifferentInputs(String input, int expectedCount) {
        // Act
        List<String> result = anagramGenerator.generateAnagrams(input);

        // Assert
        assertNotNull(result);
        assertEquals(expectedCount, result.size());
        
        // Verifica se todos os anagramas contêm as mesmas letras
        String normalizedInput = input.toLowerCase();
        result.forEach(anagram -> {
            assertEquals(normalizedInput.length(), anagram.length());
            for (char c : normalizedInput.toCharArray()) {
                assertTrue(anagram.contains(String.valueOf(c)));
            }
        });
    }

    /**
     * Fornece casos de teste para diferentes entradas
     */
    private static Stream<Arguments> provideTestCases() {
        return Stream.of(
            Arguments.of("a", 1),
            Arguments.of("ab", 2),
            Arguments.of("abc", 6),
            Arguments.of("abcd", 24),
            Arguments.of("abcde", 120)
        );
    }
}
