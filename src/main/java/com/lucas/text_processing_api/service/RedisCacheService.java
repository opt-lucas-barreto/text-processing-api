package com.lucas.text_processing_api.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.lucas.text_processing_api.dto.AnagramResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Serviço de cache Redis para anagramas
 * 
 * Este serviço gerencia o armazenamento e recuperação de anagramas no Redis,
 * permitindo reutilização de resultados já calculados para melhorar a performance.
 * 
 * @author Lucas
 * @version 1.0
 */
@Service
@Slf4j
public class RedisCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${app.anagram.cache.enabled:true}")
    private boolean cacheEnabled;

    @Value("${app.anagram.cache.ttl:3600}")
    private long cacheTtl;

    private static final String CACHE_KEY_PREFIX = "anagram:";
    private static final String SORTED_KEY_PREFIX = "anagram_sorted:";
    
    // Cache em memória como fallback
    private final ConcurrentHashMap<String, CacheEntry> memoryCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newScheduledThreadPool(1);
    private boolean redisAvailable = true;

    public RedisCacheService() {
        // Iniciar limpeza automática do cache em memória
        cleanupExecutor.scheduleAtFixedRate(this::cleanupMemoryCache, 1, 1, TimeUnit.HOURS);
    }

    /**
     * Recupera anagramas do cache baseado nas letras fornecidas
     * 
     * @param letters letras para buscar no cache
     * @return AnagramResponse se encontrado no cache, null caso contrário
     */
    public AnagramResponse getFromCache(String letters) {
        if (!cacheEnabled) {
            return null;
        }

        try {
            // Primeiro tenta Redis
            if (redisAvailable) {
                AnagramResponse response = getFromRedisCache(letters);
                if (response != null) {
                    return response;
                }
            }
            
            // Fallback para cache em memória
            return getFromMemoryCache(letters);
            
        } catch (Exception e) {
            log.warn("Erro ao buscar no cache Redis, usando cache em memória: {}", e.getMessage());
            redisAvailable = false;
            return getFromMemoryCache(letters);
        }
    }

    /**
     * Busca no cache Redis
     */
    private AnagramResponse getFromRedisCache(String letters) {
        try {
            // Cria uma chave normalizada para o cache
            String normalizedKey = createCacheKey(letters);
            String sortedKey = createSortedCacheKey(letters);
            
            // Tenta buscar usando a chave normalizada
            AnagramResponse response = (AnagramResponse) redisTemplate.opsForValue().get(normalizedKey);
            if (response != null) {
                log.debug("Cache Redis hit para letras: {}", letters);
                response.setFromCache(true);
                return response;
            }
            
            // Tenta buscar usando a chave ordenada (para anagramas com mesma composição)
            response = (AnagramResponse) redisTemplate.opsForValue().get(sortedKey);
            if (response != null) {
                log.debug("Cache Redis hit para anagramas com mesma composição: {}", letters);
                // Cria nova resposta com as letras originais
                AnagramResponse newResponse = new AnagramResponse(
                    letters.toLowerCase(),
                    response.getAnagrams()
                );
                newResponse.setFromCache(true);
                return newResponse;
            }
            
            log.debug("Cache Redis miss para letras: {}", letters);
            return null;
            
        } catch (Exception e) {
            log.warn("Erro ao acessar Redis, marcando como indisponível: {}", e.getMessage());
            redisAvailable = false;
            return null;
        }
    }

    /**
     * Busca no cache em memória
     */
    private AnagramResponse getFromMemoryCache(String letters) {
        String normalizedKey = createCacheKey(letters);
        String sortedKey = createSortedCacheKey(letters);
        
        // Busca por chave normalizada
        CacheEntry entry = memoryCache.get(normalizedKey);
        if (entry != null && !entry.isExpired()) {
            log.debug("Cache memória hit para letras: {}", letters);
            AnagramResponse response = entry.getResponse();
            response.setFromCache(true);
            return response;
        }
        
        // Busca por chave ordenada
        entry = memoryCache.get(sortedKey);
        if (entry != null && !entry.isExpired()) {
            log.debug("Cache memória hit para anagramas com mesma composição: {}", letters);
            AnagramResponse response = entry.getResponse();
            AnagramResponse newResponse = new AnagramResponse(
                letters.toLowerCase(),
                response.getAnagrams()
            );
            newResponse.setFromCache(true);
            return newResponse;
        }
        
        log.debug("Cache memória miss para letras: {}", letters);
        return null;
    }

    /**
     * Armazena anagramas no cache
     * 
     * @param letters letras originais
     * @param response resposta com anagramas
     */
    public void saveToCache(String letters, AnagramResponse response) {
        if (!cacheEnabled) {
            return;
        }

        try {
            // Tenta salvar no Redis
            if (redisAvailable) {
                saveToRedisCache(letters, response);
            }
        } catch (Exception e) {
            log.warn("Erro ao salvar no Redis, usando cache em memória: {}", e.getMessage());
            redisAvailable = false;
        }
        
        // Sempre salva no cache em memória como fallback
        saveToMemoryCache(letters, response);
    }

    /**
     * Salva no cache Redis
     */
    private void saveToRedisCache(String letters, AnagramResponse response) {
        try {
            // Cria chaves para o cache
            String normalizedKey = createCacheKey(letters);
            String sortedKey = createSortedCacheKey(letters);
            
            // Armazena com a chave normalizada
            redisTemplate.opsForValue().set(normalizedKey, response, cacheTtl, TimeUnit.SECONDS);
            
            // Armazena com a chave ordenada para reutilização de anagramas com mesma composição
            redisTemplate.opsForValue().set(sortedKey, response, cacheTtl, TimeUnit.SECONDS);
            
            log.debug("Anagramas salvos no Redis para letras: {}", letters);
            
        } catch (Exception e) {
            log.error("Erro ao salvar no cache Redis: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Salva no cache em memória
     */
    private void saveToMemoryCache(String letters, AnagramResponse response) {
        String normalizedKey = createCacheKey(letters);
        String sortedKey = createSortedCacheKey(letters);
        
        long expiryTime = System.currentTimeMillis() + (cacheTtl * 1000);
        
        memoryCache.put(normalizedKey, new CacheEntry(response, expiryTime));
        memoryCache.put(sortedKey, new CacheEntry(response, expiryTime));
        
        log.debug("Anagramas salvos no cache memória para letras: {}", letters);
    }

    /**
     * Remove um item específico do cache
     * 
     * @param letters letras para remover do cache
     */
    public void removeFromCache(String letters) {
        if (!cacheEnabled) {
            return;
        }

        try {
            if (redisAvailable) {
                removeFromRedisCache(letters);
            }
        } catch (Exception e) {
            log.warn("Erro ao remover do Redis: {}", e.getMessage());
            redisAvailable = false;
        }
        
        removeFromMemoryCache(letters);
    }

    /**
     * Remove do cache Redis
     */
    private void removeFromRedisCache(String letters) {
        try {
            String normalizedKey = createCacheKey(letters);
            String sortedKey = createSortedCacheKey(letters);
            
            redisTemplate.delete(normalizedKey);
            redisTemplate.delete(sortedKey);
            
            log.debug("Anagramas removidos do Redis para letras: {}", letters);
            
        } catch (Exception e) {
            log.error("Erro ao remover do cache Redis: {}", e.getMessage(), e);
        }
    }

    /**
     * Remove do cache em memória
     */
    private void removeFromMemoryCache(String letters) {
        String normalizedKey = createCacheKey(letters);
        String sortedKey = createSortedCacheKey(letters);
        
        memoryCache.remove(normalizedKey);
        memoryCache.remove(sortedKey);
        
        log.debug("Anagramas removidos do cache memória para letras: {}", letters);
    }

    /**
     * Limpa todo o cache de anagramas
     */
    public void clearCache() {
        if (!cacheEnabled) {
            return;
        }

        try {
            if (redisAvailable) {
                clearRedisCache();
            }
        } catch (Exception e) {
            log.warn("Erro ao limpar Redis: {}", e.getMessage());
            redisAvailable = false;
        }
        
        clearMemoryCache();
    }

    /**
     * Limpa cache Redis
     */
    private void clearRedisCache() {
        try {
            // Busca todas as chaves que começam com o prefixo de anagramas
            Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
            Set<String> sortedKeys = redisTemplate.keys(SORTED_KEY_PREFIX + "*");
            
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            
            if (sortedKeys != null && !sortedKeys.isEmpty()) {
                redisTemplate.delete(sortedKeys);
            }
            
            log.info("Cache Redis de anagramas limpo com sucesso");
            
        } catch (Exception e) {
            log.error("Erro ao limpar cache Redis: {}", e.getMessage(), e);
        }
    }

    /**
     * Limpa cache em memória
     */
    private void clearMemoryCache() {
        memoryCache.clear();
        log.info("Cache em memória de anagramas limpo com sucesso");
    }

    /**
     * Limpeza automática do cache em memória
     */
    private void cleanupMemoryCache() {
        memoryCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        log.debug("Cache em memória limpo, removidas entradas expiradas");
    }

    /**
     * Verifica se o cache está habilitado
     * 
     * @return true se o cache estiver habilitado, false caso contrário
     */
    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    /**
     * Verifica se o Redis está disponível
     * 
     * @return true se o Redis estiver disponível, false caso contrário
     */
    public boolean isRedisAvailable() {
        return redisAvailable;
    }

    /**
     * Cria uma chave de cache normalizada
     * 
     * @param letters letras para criar a chave
     * @return chave de cache normalizada
     */
    private String createCacheKey(String letters) {
        return CACHE_KEY_PREFIX + letters.toLowerCase();
    }

    /**
     * Cria uma chave de cache baseada na composição ordenada das letras
     * 
     * Esta chave permite reutilizar anagramas para conjuntos de letras
     * com a mesma composição, mesmo em ordem diferente.
     * 
     * @param letters letras para criar a chave ordenada
     * @return chave de cache ordenada
     */
    private String createSortedCacheKey(String letters) {
        char[] chars = letters.toLowerCase().toCharArray();
        java.util.Arrays.sort(chars);
        return SORTED_KEY_PREFIX + new String(chars);
    }

    /**
     * Classe interna para cache em memória
     */
    private static class CacheEntry {
        private final AnagramResponse response;
        private final long expiryTime;

        public CacheEntry(AnagramResponse response, long expiryTime) {
            this.response = response;
            this.expiryTime = expiryTime;
        }

        public AnagramResponse getResponse() {
            return response;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
}
