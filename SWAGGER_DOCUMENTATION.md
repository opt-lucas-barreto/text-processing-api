# 📚 Documentação Swagger/OpenAPI - Text Processing API

## 📋 **Visão Geral**

Este documento descreve a implementação e uso do Swagger/OpenAPI na Text Processing API, fornecendo uma interface interativa para testar e documentar todos os endpoints da API.

---

## 🚀 **Acesso ao Swagger UI**

### **URLs de Acesso**
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **OpenAPI YAML**: `http://localhost:8080/v3/api-docs.yaml`

### **Configuração de Segurança**
- **Autenticação**: Bearer Token (JWT)
- **Escopo**: Todos os endpoints protegidos requerem token válido
- **Endpoints Públicos**: `/api/auth/**`, `/api/anagrams/health`, `/swagger-ui/**`

---

## 🔧 **Configuração Técnica**

### **Dependências Maven**
```xml
<!-- Swagger/OpenAPI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
</dependency>
```

### **Classe de Configuração**
```java
@Configuration
public class OpenApiConfig {
    
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
}
```

### **Configurações application.properties**
```properties
# Configurações do Swagger/OpenAPI
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.doc-expansion=none
springdoc.swagger-ui.enable-swagger-ui=true
```

---

## 🏷️ **Tags e Organização**

### **1. Tag: Autenticação**
- **Descrição**: Endpoints para autenticação e gerenciamento de usuários
- **Endpoints**:
  - `POST /api/auth/login` - Autenticar usuário
  - `POST /api/auth/register` - Registrar novo usuário
  - `GET /api/auth/validate` - Validar token JWT
  - `GET /api/auth/health` - Health check da autenticação

### **2. Tag: Anagramas**
- **Descrição**: Endpoints para geração de anagramas e gerenciamento de cache
- **Endpoints**:
  - `POST /api/anagrams/generate` - Gerar anagramas com cache
  - `POST /api/anagrams/generate-no-cache` - Gerar anagramas sem cache
  - `GET /api/anagrams/calculate-total/{letters}` - Calcular total de anagramas
  - `GET /api/anagrams/cache/status` - Status do cache
  - `DELETE /api/anagrams/cache/{letters}` - Remover do cache (ADMIN)
  - `DELETE /api/anagrams/cache` - Limpar todo cache (ADMIN)
  - `GET /api/anagrams/health` - Health check da API

---

## 🔐 **Autenticação no Swagger**

### **Como Autenticar**
1. **Acesse o Swagger UI**: `http://localhost:8080/swagger-ui.html`
2. **Clique no botão "Authorize"** (ícone de cadeado)
3. **Digite seu token JWT**: `Bearer <seu-token-aqui>`
4. **Clique em "Authorize"**
5. **Agora você pode testar todos os endpoints protegidos**

### **Exemplo de Token**
```
Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTYzMTY4NDgwMCwiZXhwIjoxNjMxNzcxMjAwfQ.example
```

---

## 📝 **Anotações Swagger Implementadas**

### **Anotações de Classe**
```java
@Tag(name = "Autenticação", description = "Endpoints para autenticação e gerenciamento de usuários")
@Tag(name = "Anagramas", description = "Endpoints para geração de anagramas e gerenciamento de cache")
```

### **Anotações de Método**
```java
@Operation(
    summary = "Autenticar usuário",
    description = "Endpoint para autenticação de usuários e geração de token JWT"
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "Usuário autenticado com sucesso",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = AuthResponse.class),
            examples = @ExampleObject(
                name = "Login bem-sucedido",
                value = "{\"token\": \"...\", \"type\": \"Bearer\", \"username\": \"admin\", \"role\": \"ADMIN\", \"message\": \"Autenticação realizada com sucesso\"}"
            )
        )
    ),
    @ApiResponse(
        responseCode = "401",
        description = "Credenciais inválidas"
    )
})
@SecurityRequirement(name = "Bearer Authentication")
```

### **Anotações de Parâmetro**
```java
@Parameter(description = "Dados de autenticação", required = true)
@Valid @RequestBody AuthRequest request
```

---

## 🎯 **Funcionalidades do Swagger UI**

### **1. Interface Interativa**
- **Teste Direto**: Execute requisições diretamente no navegador
- **Validação**: Verificação automática de parâmetros obrigatórios
- **Respostas**: Visualização das respostas em tempo real

### **2. Documentação Automática**
- **Schemas**: Modelos de dados automaticamente documentados
- **Exemplos**: Exemplos de requisição e resposta
- **Códigos de Status**: Documentação de todos os códigos HTTP

### **3. Autenticação Integrada**
- **Bearer Token**: Suporte nativo para JWT
- **Persistência**: Token mantido durante a sessão
- **Segurança**: Endpoints protegidos claramente identificados

### **4. Organização por Tags**
- **Agrupamento Lógico**: Endpoints organizados por funcionalidade
- **Navegação Fácil**: Busca e filtros por tag
- **Documentação Clara**: Descrições detalhadas de cada endpoint

---

## 📊 **Exemplos de Uso**

### **1. Testando Login**
1. Acesse o endpoint `POST /api/auth/login`
2. Clique em "Try it out"
3. Preencha o corpo da requisição:
```json
{
  "username": "admin",
  "password": "admin123"
}
```
4. Clique em "Execute"
5. Veja a resposta com o token JWT

### **2. Testando Geração de Anagramas**
1. **Primeiro, faça login** para obter o token
2. **Autorize** com o token obtido
3. Acesse o endpoint `POST /api/anagrams/generate`
4. Preencha o corpo:
```json
{
  "letters": "abc"
}
```
5. Execute e veja os anagramas gerados

### **3. Testando Cache**
1. **Gere anagramas** para "abc" (primeira vez)
2. **Gere novamente** para "abc" (deve vir do cache)
3. **Compare os tempos** de resposta
4. **Verifique o campo** `fromCache` nas respostas

---

## 🔍 **Recursos Avançados**

### **1. Filtros e Busca**
- **Busca por Tag**: Filtre endpoints por funcionalidade
- **Busca por Método**: Filtre por GET, POST, DELETE, etc.
- **Busca por Nome**: Encontre endpoints específicos

### **2. Exportação**
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **OpenAPI YAML**: `http://localhost:8080/v3/api-docs.yaml`
- **Importação**: Use em ferramentas como Postman, Insomnia

### **3. Personalização**
- **Tema**: Interface responsiva e moderna
- **Layout**: Organização clara e intuitiva
- **Responsividade**: Funciona em dispositivos móveis

---

## 🚨 **Troubleshooting**

### **Problema: Swagger UI não carrega**
**Solução**: Verifique se a aplicação está rodando e acesse `http://localhost:8080/swagger-ui.html`

### **Problema: Endpoints protegidos não funcionam**
**Solução**: Use o botão "Authorize" para inserir seu token JWT

### **Problema: Erro 401 em endpoints públicos**
**Solução**: Verifique se o SecurityConfig permite acesso aos endpoints do Swagger

### **Problema: Schemas não aparecem**
**Solução**: Verifique se as anotações `@Schema` estão corretas nos DTOs

---

## 📚 **Recursos Adicionais**

### **Links Úteis**
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI Spec**: `http://localhost:8080/v3/api-docs`
- **H2 Console**: `http://localhost:8080/h2-console`

### **Documentação Relacionada**
- **README.md**: Visão geral do projeto
- **ARCHITECTURE.md**: Arquitetura detalhada
- **curl-examples.md**: Exemplos de uso com cURL

---

## 🎉 **Benefícios da Implementação**

### **Para Desenvolvedores**
- **Documentação Interativa**: Teste endpoints diretamente no navegador
- **Autenticação Integrada**: Suporte nativo para JWT
- **Schemas Automáticos**: Modelos de dados sempre atualizados

### **Para Usuários**
- **Interface Intuitiva**: Fácil de usar e navegar
- **Exemplos Práticos**: Exemplos de requisição e resposta
- **Testes em Tempo Real**: Validação imediata de funcionalidades

### **Para o Projeto**
- **Documentação Profissional**: Padrão da indústria
- **Manutenibilidade**: Documentação sempre sincronizada com o código
- **Colaboração**: Facilita o trabalho em equipe

---

**🎯 A API agora possui documentação Swagger/OpenAPI completa e profissional!** 🚀✨
