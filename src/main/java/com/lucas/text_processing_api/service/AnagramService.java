package com.lucas.text_processing_api.service;

import com.lucas.text_processing_api.dto.AnagramResponse;
import com.lucas.text_processing_api.util.AnagramGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serviço principal para geração de anagramas
 * 
 * Este serviço orquestra a geração de anagramas, incluindo verificação de cache,
 * geração de novos anagramas quando necessário e armazenamento no cache.
 * 
 * @author Lucas
 * @version 1.0
 */
@Service
@Slf4j
public class AnagramService {

    @Autowired
    private AnagramGenerator anagramGenerator;

    @Autowired
    private RedisCacheService cacheService;

    /**
     * Gera anagramas para um conjunto de letras
     * 
     * Este método primeiro verifica se os anagramas já existem no cache.
     * Se não existirem, gera novos anagramas e os armazena no cache.
     * 
     * @param letters string contendo as letras para geração de anagramas
     * @return AnagramResponse contendo os anagramas e informações do processamento
     */
    public AnagramResponse generateAnagrams(String letters) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Iniciando geração de anagramas para letras: {}", letters);
            
            // Primeiro, tenta buscar do cache
            AnagramResponse cachedResponse = cacheService.getFromCache(letters);
            if (cachedResponse != null) {
                cachedResponse.setProcessingTimeMs(System.currentTimeMillis() - startTime);
                log.info("Anagramas recuperados do cache para letras: {}", letters);
                return cachedResponse;
            }
            
            // Se não estiver no cache, gera novos anagramas
            log.debug("Gerando novos anagramas para letras: {}", letters);
            List<String> anagrams = anagramGenerator.generateAnagrams(letters);
            
            // Cria a resposta
            AnagramResponse response = new AnagramResponse(letters.toLowerCase(), anagrams);
            response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            
            // Salva no cache para uso futuro
            cacheService.saveToCache(letters, response);
            
            log.info("Anagramas gerados com sucesso para letras: {}. Total: {}", 
                    letters, anagrams.size());
            
            return response;
            
        } catch (Exception e) {
            log.error("Erro ao gerar anagramas para letras: {}. Erro: {}", 
                    letters, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gera anagramas sem usar cache
     * 
     * Este método força a geração de novos anagramas, ignorando o cache.
     * Útil para testes ou quando se deseja sempre gerar novos resultados.
     * 
     * @param letters string contendo as letras para geração de anagramas
     * @return AnagramResponse contendo os anagramas gerados
     */
    public AnagramResponse generateAnagramsWithoutCache(String letters) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Gerando anagramas sem cache para letras: {}", letters);
            
            List<String> anagrams = anagramGenerator.generateAnagrams(letters);
            
            AnagramResponse response = new AnagramResponse(letters.toLowerCase(), anagrams);
            response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            response.setFromCache(false);
            
            log.info("Anagramas gerados sem cache para letras: {}. Total: {}", 
                    letters, anagrams.size());
            
            return response;
            
        } catch (Exception e) {
            log.error("Erro ao gerar anagramas sem cache para letras: {}. Erro: {}", 
                    letters, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Remove anagramas específicos do cache
     * 
     * @param letters letras para remover do cache
     */
    public void removeFromCache(String letters) {
        log.info("Removendo anagramas do cache para letras: {}", letters);
        cacheService.removeFromCache(letters);
    }

    /**
     * Limpa todo o cache de anagramas
     */
    public void clearCache() {
        log.info("Limpando todo o cache de anagramas");
        cacheService.clearCache();
    }

    /**
     * Verifica se o cache está habilitado
     * 
     * @return true se o cache estiver habilitado, false caso contrário
     */
    public boolean isCacheEnabled() {
        return cacheService.isCacheEnabled();
    }

    /**
     * Calcula o número total de anagramas possíveis para um conjunto de letras
     * 
     * @param letters string contendo as letras
     * @return número total de anagramas possíveis
     */
    public long calculateTotalAnagrams(String letters) {
        return anagramGenerator.calculateTotalAnagrams(letters);
    }
}
