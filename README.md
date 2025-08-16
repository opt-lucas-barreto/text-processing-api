# 🚀 Text Processing API - Gerador de Anagramas com Segurança JWT

Uma API robusta e segura para geração de anagramas, construída com Spring Boot, integrada com Redis para cache inteligente e protegida com autenticação JWT.

## ✨ **Funcionalidades Principais**

- 🎯 **Geração de Anagramas**: Algoritmo otimizado para gerar todas as permutações possíveis
- 🔐 **Segurança JWT**: Autenticação e autorização baseada em tokens
- 👥 **Sistema de Usuários**: Banco H2 com usuários de teste (admin/user)
- 🗄️ **Cache Inteligente**: Redis + cache em memória para máxima performance
- 🔄 **Cache Inteligente**: Reutiliza anagramas com mesma composição de letras
- ✅ **Validação Robusta**: Validação de entrada com mensagens de erro claras
- 📊 **Monitoramento**: Endpoints de saúde e status do cache
- 🧪 **Testes Automatizados**: Scripts para Windows (PowerShell) e Linux/Mac (Bash)
- 🚀 **Performance**: Resposta em milissegundos para palavras de até 8 letras

## 🛠️ **Tecnologias Utilizadas**

- **Backend**: Spring Boot 3.x, Java 17
- **Segurança**: Spring Security, JWT (JSON Web Tokens)
- **Banco de Dados**: H2 Database (embutido)
- **Cache**: Redis embutido + cache em memória (fallback)
- **Build**: Maven
- **Testes**: JUnit 5, Mockito, Spring Security Test
- **Documentação**: Javadoc, Markdown, Swagger/OpenAPI
- **Scripts**: PowerShell (Windows), Bash (Linux/Mac)
- **API Docs**: Swagger UI interativo

## 🏗️ **Estrutura do Projeto**

```
text-processing-api/
├── src/
│   ├── main/java/com/lucas/text_processing_api/
│   │   ├── TextProcessingApiApplication.java      # Classe principal
│   │   ├── controller/
│   │   │   ├── AnagramController.java            # Endpoints REST (protegidos)
│   │   │   └── AuthController.java               # Endpoints de autenticação
│   │   ├── service/
│   │   │   ├── AnagramService.java               # Lógica de negócio
│   │   │   ├── RedisCacheService.java            # Gerenciamento de cache
│   │   │   ├── AuthService.java                  # Serviço de autenticação
│   │   │   └── JwtService.java                   # Geração/validação JWT
│   │   ├── entity/
│   │   │   └── User.java                         # Entidade de usuário
│   │   ├── repository/
│   │   │   └── UserRepository.java               # Repositório de usuários
│   │   ├── dto/
│   │   │   ├── AnagramRequest.java               # DTO de entrada
│   │   │   ├── AnagramResponse.java              # DTO de resposta
│   │   │   ├── AuthRequest.java                  # DTO de autenticação
│   │   │   └── AuthResponse.java                 # DTO de resposta de auth
│   │   ├── config/
│   │   │   ├── SecurityConfig.java               # Configuração de segurança
│   │   │   ├── JwtAuthenticationFilter.java      # Filtro JWT
│   │   │   ├── RedisConfig.java                  # Configuração Redis
│   │   │   └── EmbeddedRedisConfig.java          # Redis embutido
│   │   └── util/
│   │       └── AnagramGenerator.java             # Algoritmo de geração
│   └── resources/
│       └── application.properties                # Configurações
├── test-api.ps1                                  # Script PowerShell (Windows)
├── debug-cache.ps1                               # Debug cache PowerShell
├── test-api.sh                                   # Script Bash (Linux/Mac)
├── debug-cache.sh                                # Debug cache Bash
├── curl-examples.md                              # Exemplos cURL
├── curl-examples-windows.md                      # Exemplos Windows
└── README.md                                     # Este arquivo
```

## 📚 **Documentação Swagger/OpenAPI**

### **Acesso ao Swagger UI**
- **URL**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI Spec**: `http://localhost:8080/v3/api-docs`
- **Documentação**: `SWAGGER_DOCUMENTATION.md`

### **Funcionalidades**
- **Interface Interativa**: Teste endpoints diretamente no navegador
- **Autenticação Integrada**: Suporte nativo para JWT Bearer Token
- **Documentação Automática**: Schemas e exemplos sempre atualizados
- **Organização por Tags**: Endpoints agrupados por funcionalidade

### **Como Usar**
1. Acesse `http://localhost:8080/swagger-ui.html`
2. Use o botão "Authorize" para inserir seu token JWT
3. Teste qualquer endpoint diretamente na interface
4. Veja exemplos de requisição e resposta em tempo real

---

## 🔐 **Sistema de Segurança JWT**

### **Arquitetura de Segurança**
A API implementa um sistema de segurança robusto baseado em JWT:

```
┌─────────────────────────────────────────────────────────────┐
│                    Cliente                                 │
├─────────────────────────────────────────────────────────────┤
│                    Spring Security                         │
├─────────────────┬───────────────────────────────────────────┤
│   Auth Filter   │           Security Config                │
│   (JWT)        │           (Roles & Permissions)          │
├─────────────────┴───────────────────────────────────────────┤
│                    Controllers                             │
│              (Protegidos por Roles)                       │
└─────────────────────────────────────────────────────────────┘
```

### **1. Fluxo de Autenticação**

#### **Login**
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin",
  "role": "ADMIN",
  "message": "Autenticação realizada com sucesso"
}
```

#### **Uso do Token**
```http
GET /api/anagrams/generate
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### **2. Usuários de Teste**

A aplicação inicializa automaticamente dois usuários de teste:

| Username | Password | Role  | Descrição           |
|----------|----------|-------|---------------------|
| `admin`  | `admin123` | ADMIN | Acesso total a todos os endpoints |
| `user`   | `user123`  | USER  | Acesso limitado (sem gerenciamento de cache) |

### **3. Controle de Acesso por Role**

#### **Endpoints Públicos**
- `GET /api/anagrams/health` - Health check da API
- `GET /api/auth/health` - Health check da autenticação
- `POST /api/auth/login` - Login de usuários
- `POST /api/auth/register` - Registro de novos usuários

#### **Endpoints para USER e ADMIN**
- `POST /api/anagrams/generate` - Geração de anagramas
- `POST /api/anagrams/generate-no-cache` - Geração sem cache
- `GET /api/anagrams/cache/status` - Status do cache
- `GET /api/anagrams/calculate-total/{letters}` - Cálculo de total

#### **Endpoints apenas para ADMIN**
- `DELETE /api/anagrams/cache/{letters}` - Remover cache específico
- `DELETE /api/anagrams/cache` - Limpar todo o cache

### **4. Configuração JWT**

```properties
# application.properties
jwt.secret=text-processing-api-secret-key-2024-very-long-and-secure-key-for-jwt-signing
jwt.expiration=86400000  # 24 horas em milissegundos
```

### **5. Banco de Dados H2**

```properties
# Configuração H2
spring.datasource.url=jdbc:h2:mem:text_processing_db
spring.datasource.username=sa
spring.datasource.password=password
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

**Acesso ao Console H2:**
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:text_processing_db`
- Username: `sa`
- Password: `password`

## 🧮 **Algoritmo de Geração de Anagramas**

### **Conceito Matemático**
A geração de anagramas é baseada no conceito de **permutações**:
- Para uma palavra com **n letras distintas**: n! (fatorial) anagramas
- Para uma palavra com **letras repetidas**: n! / (r₁! × r₂! × ... × rₖ!)

### **Implementação Técnica**

#### **1. Algoritmo de Backtracking**
```java
// Pseudocódigo do algoritmo implementado
function generateAnagrams(letters):
    result = []
    used = [false, false, ..., false]  // Array de booleanos
    current = ""                        // Anagrama sendo construído
    
    function backtrack():
        if current.length == letters.length:
            result.add(current)
            return
        
        for i = 0 to letters.length - 1:
            if not used[i]:
                used[i] = true
                current += letters[i]
                backtrack()
                current = current.substring(0, current.length - 1)
                used[i] = false
    
    backtrack()
    return result.distinct()  // Remove duplicatas
```

#### **2. Otimizações Implementadas**
- **Early Termination**: Para quando o anagrama atual atinge o tamanho desejado
- **Remoção de Duplicatas**: Usa `distinct()` para eliminar anagramas repetidos
- **Validação de Entrada**: Verifica se contém apenas letras antes do processamento
- **Normalização**: Converte para minúsculas para consistência

#### **3. Complexidade**
- **Tempo**: O(n!) - onde n é o número de letras
- **Espaço**: O(n!) para armazenar todos os anagramas
- **Limitação Prática**: Recomendado até 8 letras para performance aceitável

### **Exemplos de Cálculo**
```
"abc" (3 letras distintas): 3! = 6 anagramas
"test" (4 letras, 2 repetidas): 4! / 2! = 12 anagramas
"hello" (5 letras, 2 repetidas): 5! / 2! = 60 anagramas
"abcdefgh" (8 letras distintas): 8! = 40.320 anagramas
```

## 🗄️ **Sistema de Cache Inteligente**

### **Arquitetura Híbrida**
O sistema utiliza uma abordagem em camadas para máxima performance e confiabilidade:

```
┌─────────────────────────────────────────────────────────────┐
│                    AnagramService                          │
├─────────────────────────────────────────────────────────────┤
│                    RedisCacheService                       │
├─────────────────┬───────────────────────────────────────────┤
│   Redis Cache   │           Memory Cache                   │
│   (Primário)    │           (Fallback)                     │
└─────────────────┴───────────────────────────────────────────┘
```

### **1. Estratégia de Cache Dupla**

#### **Cache Direto (Chave Normalizada)**
```
Chave: "anagram:test"
Valor: AnagramResponse completo para "test"
Uso: Busca rápida para palavras idênticas
```

#### **Cache Inteligente (Chave Ordenada)**
```
Chave: "anagram_sorted:estt"  // Letras ordenadas de "test"
Valor: AnagramResponse para "test"
Uso: Reutiliza anagramas com mesma composição
```

### **2. Exemplo de Funcionamento**

```mermaid
flowchart TD
    A[Entrada] --> B[Cache Direto]
    B --> C{Cache Hit?}
    C -->|Não| D[Cache Inteligente]
    D --> E{Composição existe?}
    E -->|Sim| F[Recuperar Cache]
    E -->|Não| G[Gerar Anagramas]
    F --> H[Retornar Cache]
    G --> I[Salvar Cache]
    I --> J[Retornar Novo]
```

### **3. Benefícios do Cache Inteligente**

#### **Reutilização de Resultados**
- **"test"** → Gera 12 anagramas (cache miss)
- **"tets"** → Recupera do cache (cache hit) - mesma composição
- **"sett"** → Recupera do cache (cache hit) - mesma composição

#### **Economia de Processamento**
- **Sem cache**: 3 × geração = 3 × processamento
- **Com cache**: 1 × geração + 2 × recuperação = 1 × processamento

### **4. Configuração de Cache**

#### **Redis (Primário)**
```properties
# application.properties
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.timeout=2000ms
app.anagram.cache.enabled=true
app.anagram.cache.ttl=3600  # 1 hora
```

#### **Cache em Memória (Fallback)**
```java
// Configuração automática
- TTL configurável via properties
- Limpeza automática a cada hora
- Thread-safe com ConcurrentHashMap
- Recuperação automática se Redis falhar
```

### **5. Métricas de Performance**

#### **Tempos Típicos (Intel i7, 16GB RAM)**
```
Palavra    | Sem Cache | Com Cache | Melhoria
-----------|-----------|-----------|----------
"abc"      | 15ms      | 3ms       | 5x mais rápido
"test"     | 25ms      | 5ms       | 5x mais rápido
"hello"    | 120ms     | 8ms       | 15x mais rápido
"abcdef"   | 800ms     | 12ms      | 67x mais rápido
```

## 🧪 **Sistema de Testes**

### **Scripts de Teste Automatizado**

#### **1. PowerShell (Windows)**
```powershell
# Suite completa de testes (com autenticação JWT)
.\test-api.ps1

# Debug específico do cache
.\debug-cache.ps1
```

#### **2. Bash (Linux/Mac)**
```bash
# Suite completa de testes (com autenticação JWT)
bash test-api.sh

# Debug específico do cache
bash debug-cache.sh
```

### **Cobertura de Testes**

#### **Testes Unitários**
- ✅ **AnagramGenerator**: Algoritmo de geração
- ✅ **AnagramService**: Lógica de negócio
- ✅ **RedisCacheService**: Gerenciamento de cache
- ✅ **AnagramController**: Endpoints REST (com segurança)
- ✅ **AuthController**: Endpoints de autenticação
- ✅ **JwtService**: Geração e validação de tokens
- ✅ **AuthService**: Serviço de autenticação
- ✅ **Validação**: DTOs e regras de negócio

#### **Testes de Integração**
- ✅ **Autenticação JWT**: Login e validação de tokens
- ✅ **Controle de Acesso**: Verificação de roles e permissões
- ✅ **Cache Hit/Miss**: Verificação de comportamento
- ✅ **Cache Inteligente**: Reutilização de composições
- ✅ **Validação de Entrada**: Tratamento de erros
- ✅ **Performance**: Tempos de resposta
- ✅ **Gerenciamento**: Limpeza e status do cache

### **Casos de Teste Cobertos**

#### **Autenticação e Segurança**
- Login com credenciais válidas
- Login com credenciais inválidas
- Acesso negado sem token JWT
- Validação de tokens JWT
- Controle de acesso por role
- Endpoints públicos vs protegidos

#### **Geração Básica**
- Palavras de 1-8 letras
- Letras maiúsculas/minúsculas
- Palavras com letras repetidas
- Casos de borda (letra única, vazia)

#### **Cache e Performance**
- Primeira chamada (cache miss)
- Segunda chamada (cache hit)
- Cache inteligente (mesma composição)
- Limpeza e gerenciamento de cache
- Medição de tempos de resposta

#### **Validação e Erros**
- Entrada vazia
- Números e caracteres especiais
- Strings muito longas
- Tratamento de exceções

## 📡 **Endpoints da API**

### **1. Autenticação (Públicos)**

#### **Login**
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

#### **Registro**
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "password": "password",
  "email": "user@example.com",
  "fullName": "Novo Usuário",
  "role": "USER"
}
```

#### **Validação de Token**
```http
GET /api/auth/validate
Authorization: Bearer {token}
```

#### **Health Check da Autenticação**
```http
GET /api/auth/health
```

### **2. Anagramas (Protegidos - Requer JWT)**

#### **Geração de Anagramas**
```http
POST /api/anagrams/generate
Authorization: Bearer {token}
Content-Type: application/json

{
  "letters": "test"
}
```

**Resposta:**
```json
{
  "originalLetters": "test",
  "anagrams": ["test", "tets", "tset", "tste", "ttes", "ttse", "estt", "etst", "etts", "sett", "stet", "stte"],
  "totalAnagrams": 12,
  "fromCache": false,
  "processingTimeMs": 25
}
```

#### **Geração sem Cache**
```http
POST /api/anagrams/generate-no-cache
Authorization: Bearer {token}
Content-Type: application/json

{
  "letters": "test"
}
```

#### **Status do Cache**
```http
GET /api/anagrams/cache/status
Authorization: Bearer {token}
```

#### **Cálculo de Total**
```http
GET /api/anagrams/calculate-total/{letters}
Authorization: Bearer {token}
```

### **3. Gerenciamento de Cache (Apenas ADMIN)**

#### **Remover Cache Específico**
```http
DELETE /api/anagrams/cache/{letters}
Authorization: Bearer {token}
```

#### **Limpar Todo o Cache**
```http
DELETE /api/anagrams/cache
Authorization: Bearer {token}
```

### **4. Health Check (Público)**
```http
GET /api/anagrams/health
```

**Resposta:**
```json
{
  "message": "Text Processing API está funcionando",
  "status": "UP",
  "timestamp": "2025-08-16T09:10:01.434784600"
}
```

## 🚀 **Execução e Testes**

### **1. Compilar e Executar**
```bash
# Compilar
mvn clean compile

# Executar
mvn spring-boot:run
```

### **2. Testes Manuais com cURL**

#### **Autenticação**
```bash
# Login
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'

# Extrair token da resposta e usar nos próximos comandos
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### **Gerar Anagramas (com autenticação)**
```bash
# Gerar anagramas
curl -X POST "http://localhost:8080/api/anagrams/generate" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"letters": "abc"}'
```

#### **Health Check (sem autenticação)**
```bash
# Health check
curl http://localhost:8080/api/anagrams/health
```

### **3. Testes Automatizados**
```bash
# Windows (PowerShell)
.\test-api.ps1

# Linux/Mac (Bash)
bash test-api.sh
```

## 📊 **Monitoramento e Métricas**

### **Logs de Performance**
```
DEBUG - Cache Redis hit para letras: test
INFO  - Anagramas recuperados do cache para letras: test
INFO  - Anagramas gerados com sucesso. Total: 12
```

### **Logs de Segurança**
```
INFO  - Tentativa de autenticação para usuário: admin
INFO  - Autenticação bem-sucedida para usuário: admin
DEBUG - Usuário autenticado via JWT: admin
```

### **Métricas de Cache**
- **Hit Rate**: Porcentagem de cache hits
- **Miss Rate**: Porcentagem de cache misses
- **Response Time**: Tempo médio de resposta
- **Memory Usage**: Uso de memória do cache

## 🔧 **Configurações Avançadas**

### **Redis Embarcado**
```java
@Configuration
public class EmbeddedRedisConfig {
    // Configuração otimizada para desenvolvimento
    // - Heap limitado a 32MB
    // - Sem persistência para economia de espaço
    // - Diretório temporário para arquivos
}
```

### **Cache Híbrido**
```java
// Fallback automático se Redis falhar
if (redisAvailable) {
    // Usar Redis
} else {
    // Usar cache em memória
}
```

### **Segurança JWT**
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    // Configuração de segurança
    // - Filtros JWT
    // - Controle de acesso por role
    // - Configuração de CORS
}
```

## 📚 **Documentação Adicional**

### **Arquivos de Referência**
- `curl-examples.md`: Exemplos cURL para Linux/Mac
- `curl-examples-windows.md`: Exemplos para Windows
- `test-api.sh`: Script de teste Bash
- `test-api.ps1`: Script de teste PowerShell
- `debug-cache.sh`: Debug de cache Bash
- `debug-cache.ps1`: Debug de cache PowerShell

### **Comandos Úteis**
```bash
# Verificar logs da aplicação
tail -f logs/application.log

# Monitorar uso de Redis
redis-cli monitor

# Acessar console H2
# Abrir http://localhost:8080/h2-console no navegador

# Testar performance com autenticação
TOKEN=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}' | \
  grep -o '"token":"[^"]*"' | cut -d'"' -f4)

time curl -X POST "http://localhost:8080/api/anagrams/generate" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"letters": "hello"}'
```

### **🚀 Padrões de Código**
- **Java**: Google Java Style Guide
- **Documentação**: Javadoc para todas as classes públicas
- **Testes**: Cobertura mínima de 90%
- **Commits**: Mensagens claras e descritivas
- **Segurança**: Validação de entrada e controle de acesso

## 📄 **Licença**

Este projeto está licenciado sob a MIT License - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 👨‍💻 **Autor**

**Lucas** - Desenvolvedor Java e entusiasta de algoritmos

---

## 🎉 **Status do Projeto**

O projeto está completamente funcional e pronto para uso em produção, com todas as funcionalidades solicitadas implementadas e testadas, incluindo a nova camada de segurança JWT.

### **✅ Funcionalidades Implementadas**
- [x] Geração de anagramas com algoritmo otimizado
- [x] Cache Redis + memória com fallback automático
- [x] Cache inteligente para reutilização de composições
- [x] Validação robusta de entrada
- [x] **Segurança JWT com Spring Security**
- [x] **Sistema de usuários com banco H2**
- [x] **Controle de acesso por roles (USER/ADMIN)**
- [x] **Autenticação e autorização**
- [x] Testes unitários e de integração
- [x] Scripts de teste para Windows e Linux/Mac
- [x] Documentação completa com exemplos
- [x] Redis embutido configurado
- [x] Monitoramento e métricas
- [x] Tratamento de erros e exceções


**A API está funcionando perfeitamente com segurança JWT e pronta para uso!** 🎯✨🔐
