package com.lucas.text_processing_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para respostas de geração de anagramas
 * 
 * Esta classe representa a resposta da API contendo os anagramas gerados
 * e informações sobre o processamento.
 * 
 * @author Lucas
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnagramResponse {

    /**
     * String original fornecida como entrada
     */
    private String originalLetters;

    /**
     * Lista de todos os anagramas possíveis
     */
    private List<String> anagrams;

    /**
     * Quantidade total de anagramas gerados
     */
    private int totalAnagrams;

    /**
     * Indica se o resultado foi obtido do cache
     */
    private boolean fromCache;

    /**
     * Tempo de processamento em milissegundos
     */
    private long processingTimeMs;

    /**
     * Construtor para criar resposta com dados básicos
     * 
     * @param originalLetters letras originais
     * @param anagrams lista de anagramas
     */
    public AnagramResponse(String originalLetters, List<String> anagrams) {
        this.originalLetters = originalLetters;
        this.anagrams = anagrams;
        this.totalAnagrams = anagrams.size();
        this.fromCache = false;
        this.processingTimeMs = 0;
    }
}
