package com.lucas.text_processing_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do Swagger/OpenAPI
 *
 * Esta classe configura a documentação interativa da API usando OpenAPI 3.0
 *
 * @author Lucas
 * @version 2.5
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configura a documentação OpenAPI
     *
     * @return OpenAPI configurado
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Text Processing API")
                .description("API para processamento de texto com geração de anagramas, cache Redis e segurança JWT")
                .version("2.5.0")
                .contact(new Contact()
                    .name("Lucas")
                    .email("lucasbbarreto2@gmail.com")
                    .url("https://github.com/lucas/text-processing-api"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Servidor de Desenvolvimento"),
                new Server()
                    .url("https://api.example.com")
                    .description("Servidor de Produção")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    /**
     * Cria o esquema de segurança para JWT
     *
     * @return SecurityScheme configurado
     */
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .bearerFormat("JWT")
            .scheme("bearer");
    }
}
