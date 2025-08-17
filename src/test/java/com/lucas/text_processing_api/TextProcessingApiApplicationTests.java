package com.lucas.text_processing_api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração da aplicação
 * 
 * @author Lucas
 * @version 1.1
 */
@SpringBootTest
@ActiveProfiles("test")
class TextProcessingApiApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        // Verifica se o contexto Spring foi carregado com sucesso
        assertNotNull(applicationContext);
    }

    @Test
    void shouldLoadRequiredBeans() {
        // Verifica se os beans principais estão sendo carregados
        assertTrue(applicationContext.containsBean("anagramController"));
        assertTrue(applicationContext.containsBean("authController"));
        assertTrue(applicationContext.containsBean("anagramService"));
        assertTrue(applicationContext.containsBean("authService"));
        assertTrue(applicationContext.containsBean("anagramGenerator"));
        assertTrue(applicationContext.containsBean("redisCacheService"));
    }

    @Test
    void shouldLoadConfigurationBeans() {
        // Verifica se os beans de configuração estão sendo carregados
        assertTrue(applicationContext.containsBean("securityConfig"));
        assertTrue(applicationContext.containsBean("jwtAuthenticationFilter"));
        assertTrue(applicationContext.containsBean("redisConfig"));
        assertTrue(applicationContext.containsBean("embeddedRedisConfig"));
    }

    @Test
    void shouldLoadExceptionHandler() {
        // Verifica se o handler global de exceções está sendo carregado
        assertTrue(applicationContext.containsBean("globalExceptionHandler"));
    }
}
