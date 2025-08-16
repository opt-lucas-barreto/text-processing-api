package com.lucas.text_processing_api.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilitário para geração de anagramas
 * 
 * Esta classe implementa o algoritmo de geração de anagramas usando backtracking.
 * O algoritmo gera todas as permutações possíveis de um conjunto de letras.
 * 
 * @author Lucas
 * @version 1.0
 */
@Component
public class AnagramGenerator {

    /**
     * Gera todos os anagramas possíveis para um conjunto de letras
     * 
     * Este método utiliza o algoritmo de backtracking para gerar todas as
     * permutações possíveis das letras fornecidas.
     * 
     * @param letters string contendo as letras para geração de anagramas
     * @return lista de todos os anagramas possíveis
     * @throws IllegalArgumentException se a entrada for inválida
     */
    public List<String> generateAnagrams(String letters) {
        // Validação da entrada
        if (letters == null || letters.trim().isEmpty()) {
            throw new IllegalArgumentException("As letras não podem estar vazias");
        }
        
        if (!letters.matches("^[a-zA-Z]+$")) {
            throw new IllegalArgumentException("Apenas letras são permitidas");
        }

        // Normaliza as letras para minúsculas para consistência
        String normalizedLetters = letters.toLowerCase();
        
        List<String> result = new ArrayList<>();
        char[] chars = normalizedLetters.toCharArray();
        
        // Gera todas as permutações usando backtracking
        generatePermutations(chars, 0, result);
        
        // Remove duplicatas se houver letras repetidas
        result = result.stream().distinct().toList();
        
        return result;
    }

    /**
     * Método recursivo para gerar permutações usando backtracking
     * 
     * Este método implementa o algoritmo de backtracking para gerar todas as
     * permutações possíveis de um array de caracteres.
     * 
     * @param chars array de caracteres para permutar
     * @param start índice inicial para a permutação atual
     * @param result lista para armazenar os resultados
     */
    private void generatePermutations(char[] chars, int start, List<String> result) {
        // Caso base: se chegamos ao final do array, adiciona a permutação atual
        if (start == chars.length - 1) {
            result.add(new String(chars));
            return;
        }

        // Gera permutações para cada posição
        for (int i = start; i < chars.length; i++) {
            // Troca os caracteres nas posições start e i
            swap(chars, start, i);
            
            // Recursivamente gera permutações para o restante
            generatePermutations(chars, start + 1, result);
            
            // Desfaz a troca (backtracking)
            swap(chars, start, i);
        }
    }

    /**
     * Troca dois caracteres em um array
     * 
     * @param chars array de caracteres
     * @param i primeira posição
     * @param j segunda posição
     */
    private void swap(char[] chars, int i, int j) {
        char temp = chars[i];
        chars[i] = chars[j];
        chars[j] = temp;
    }

    /**
     * Calcula o número total de anagramas possíveis
     * 
     * Este método calcula o fatorial do número de letras, que é igual
     * ao número total de anagramas possíveis.
     * 
     * @param letters string contendo as letras
     * @return número total de anagramas possíveis
     */
    public long calculateTotalAnagrams(String letters) {
        if (letters == null || letters.isEmpty()) {
            return 0;
        }
        
        int n = letters.length();
        long factorial = 1;
        
        for (int i = 2; i <= n; i++) {
            factorial *= i;
        }
        
        return factorial;
    }
}
