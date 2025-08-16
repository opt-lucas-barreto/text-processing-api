package com.lucas.text_processing_api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Classe principal da aplicação Text Processing API
 * 
 * Esta aplicação fornece funcionalidades para processamento de texto,
 * incluindo geração de anagramas com cache Redis para otimização de performance
 * e autenticação JWT para segurança.
 * 
 * @author Lucas
 * @version 2.1
 */
@SpringBootApplication
@EnableCaching
@Slf4j
public class TextProcessingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TextProcessingApiApplication.class, args);
    }
}
