package com.lucas.text_processing_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuração para Redis embutido
 * 
 * Esta configuração fornece uma instância Redis embutida para desenvolvimento
 * e testes, funcionando independente do perfil ativo.
 * 
 * @author Lucas
 * @version 1.0
 */
@Configuration
public class EmbeddedRedisConfig {

    private redis.embedded.RedisServer redisServer;

    /**
     * Inicia o servidor Redis embutido
     */
    @PostConstruct
    public void startRedis() {
        try {
            // Criar diretório temporário para o Redis
            Path tempDir = Files.createTempDirectory("redis-embedded");
            
            // Configurar Redis com menos memória e diretório específico
            redisServer = redis.embedded.RedisServer.builder()
                .port(6379)
                .setting("maxheap 32mb")  // Limitar heap a 32MB
                .setting("heapdir " + tempDir.toString())  // Usar diretório temporário
                .setting("save \"\"")  // Desabilitar persistência
                .setting("appendonly no")  // Desabilitar AOF
                .build();
            
            redisServer.start();
            System.out.println("✅ Redis embutido iniciado com sucesso na porta 6379");
            
        } catch (Exception e) {
            // Se não conseguir iniciar o Redis embutido, loga o erro mas não falha
            System.err.println("⚠️ Aviso: Não foi possível iniciar Redis embutido: " + e.getMessage());
            System.err.println("ℹ️ A aplicação continuará funcionando sem cache Redis");
        }
    }

    /**
     * Para o servidor Redis embutido
     */
    @PreDestroy
    public void stopRedis() {
        if (redisServer != null && redisServer.isActive()) {
            redisServer.stop();
            System.out.println("🛑 Redis embutido parado");
        }
    }

    /**
     * Configura a fábrica de conexão para o Redis embutido
     * 
     * @return RedisConnectionFactory configurada para o Redis embutido
     */
    @Bean
    @Primary
    public RedisConnectionFactory embeddedRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("localhost", 6379);
        return new LettuceConnectionFactory(config);
    }
}
