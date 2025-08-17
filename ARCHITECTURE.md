# 🏗️ Documentação Técnica de Arquitetura - Text Processing API (COM JWT)

## 📋 **Visão Geral**

Este documento descreve detalhadamente a arquitetura, padrões de projeto e decisões técnicas implementadas na Text Processing API, incluindo a nova camada de segurança JWT. É uma documentação interna para desenvolvedores e arquitetos.

**⚠️ IMPORTANTE**: Este arquivo contém informações técnicas detalhadas e não deve ser commitado no repositório público.

---

## 🎯 **Arquitetura Geral**

### **1.1 Padrão Arquitetural**

A aplicação segue uma **Arquitetura em Camadas (Layered Architecture)** com separação clara de responsabilidades e uma camada de segurança integrada:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│                    (Controllers)                           │
├─────────────────────────────────────────────────────────────┤
│                    Security Layer                          │
│                    (JWT, Spring Security)                  │
├─────────────────────────────────────────────────────────────┤
│                    Business Layer                          │
│                    (Services)                              │
├─────────────────────────────────────────────────────────────┤
│                    Data Access Layer                       │
│                    (Cache, Utils, Repository)              │
├─────────────────────────────────────────────────────────────┤
│                    Infrastructure Layer                     │
│                    (Config, Redis, H2 Database)            │
└─────────────────────────────────────────────────────────────┘
```

### **1.2 Princípios de Design**

- **Separation of Concerns**: Cada camada tem responsabilidade específica
- **Dependency Inversion**: Dependências injetadas, não criadas
- **Single Responsibility**: Cada classe tem uma única responsabilidade
- **Open/Closed Principle**: Extensível sem modificação
- **Security by Design**: Segurança integrada em todas as camadas

---

## 🔐 **Arquitetura de Segurança JWT**

### **2.1 Visão Geral da Segurança**

A aplicação implementa um sistema de segurança robusto baseado em JWT (JSON Web Tokens) com Spring Security:

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

### **2.2 Componentes de Segurança**

#### **JwtAuthenticationFilter**
```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) {
        // 1. Extrair token do header Authorization
        // 2. Validar token JWT
        // 3. Configurar contexto de segurança
        // 4. Passar para próximo filtro
    }
}
```

#### **SecurityConfig**
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/anagrams/health").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

### **2.3 Fluxo de Autenticação**

```
1. Cliente → POST /api/auth/login (credenciais)
2. AuthService → Valida credenciais
3. JwtService → Gera token JWT
4. Cliente → Armazena token
5. Cliente → Inclui token no header Authorization
6. JwtAuthenticationFilter → Valida token
7. SecurityContext → Configura autenticação
8. Controller → Processa requisição
```

### **2.4 Controle de Acesso por Role**

#### **Anotações de Segurança**
```java
@RestController
@RequestMapping("/api/anagrams")
public class AnagramController {
    
    @PostMapping("/generate")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<AnagramResponse> generateAnagrams(...) {
        // Acesso para usuários autenticados
    }
    
    @DeleteMapping("/cache")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> clearCache(...) {
        // Acesso apenas para administradores
    }
}
```

#### **Mapeamento de Roles**
- **USER**: Acesso básico aos endpoints de anagramas
- **ADMIN**: Acesso total, incluindo gerenciamento de cache

---

## 🧩 **Padrões de Projeto Implementados**

### **3.1 Padrão MVC (Model-View-Controller)**

#### **Implementação:**
```java
@RestController
@RequestMapping("/api/anagrams")
public class AnagramController {
    
    @Autowired
    private AnagramService anagramService;
    
    @PostMapping("/generate")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<AnagramResponse> generateAnagrams(
        @Valid @RequestBody AnagramRequest request) {
        // Controller: Gerencia requisições HTTP
        // Model: AnagramRequest/AnagramResponse (DTOs)
        // View: Resposta JSON
    }
}
```

#### **Benefícios:**
- **Separação clara** entre lógica de apresentação e negócio
- **Testabilidade** melhorada com componentes isolados
- **Manutenibilidade** com responsabilidades bem definidas
- **Segurança integrada** com controle de acesso por role

### **3.2 Padrão Service Layer**

#### **Implementação:**
```java
@Service
@Slf4j
public class AnagramService {
    
    @Autowired
    private RedisCacheService cacheService;
    
    @Autowired
    private AnagramGenerator generator;
    
    public AnagramResponse generateAnagrams(String letters) {
        // Orquestração de operações
        // Lógica de negócio centralizada
        // Coordenação entre diferentes componentes
    }
}
```

#### **Características:**
- **Orchestration**: Coordena diferentes componentes
- **Business Logic**: Centraliza regras de negócio
- **Transaction Management**: Gerencia transações
- **Logging**: Logs estruturados para monitoramento

### **3.3 Padrão Repository (Cache)**

#### **Implementação:**
```java
@Service
@Slf4j
public class RedisCacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public void saveAnagrams(String letters, AnagramResponse response) {
        // Abstração de acesso a dados
        // Interface consistente para cache
        // Fallback para cache em memória
    }
}
```

### **3.4 Padrão DTO (Data Transfer Object)**

#### **Implementação:**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    
    @NotBlank(message = "Username é obrigatório")
    private String username;
    
    @NotBlank(message = "Password é obrigatório")
    private String password;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private String role;
    private String message;
}
```

#### **Benefícios:**
- **Validação**: Anotações Bean Validation
- **Serialização**: JSON automático
- **Segurança**: Não expõe entidades internas
- **Versioning**: Controle de versão da API

---

## 🗄️ **Arquitetura de Dados**

### **4.1 Banco de Dados H2**

#### **Configuração:**
```properties
# H2 Database
spring.datasource.url=jdbc:h2:mem:text_processing_db
spring.datasource.username=sa
spring.datasource.password=password
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

#### **Entidade User:**
```java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String email;
    
    @Column(name = "full_name")
    private String fullName;
    
    @Column(name = "is_active")
    private boolean isActive = true;
    
    @Column(name = "role")
    private String role = "USER";
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
```

#### **UserRepository:**
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

### **4.2 Cache Redis**

#### **Configuração:**
```properties
# Redis
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.timeout=2000ms

# Cache
app.anagram.cache.enabled=true
app.anagram.cache.ttl=3600
```

#### **Estratégia de Cache:**
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

---

## 🔧 **Configurações de Segurança**

### **5.1 JWT Configuration**

#### **Properties:**
```properties
# JWT
jwt.secret=text-processing-api-secret-key-2024-very-long-and-secure-key-for-jwt-signing
jwt.expiration=86400000
```

#### **JwtService:**
```java
@Service
@Slf4j
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    public String generateToken(UserDetails userDetails) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        
        return Jwts.builder()
                .claims(new HashMap<>())
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
    
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
```

### **5.2 AuthService**

#### **Implementação:**
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    public AuthResponse authenticate(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }
}
```

---

## 🧪 **Arquitetura de Testes**

### **6.1 Testes Unitários**

#### **Estrutura:**
```
src/test/java/
├── controller/
│   ├── AnagramControllerTest.java
│   └── AuthControllerTest.java
├── service/
│   ├── AnagramServiceTest.java
│   ├── AuthServiceTest.java
│   └── JwtServiceTest.java
└── util/
    └── AnagramGeneratorTest.java
```

#### **Testes de Segurança:**
```java
@ExtendWith(MockitoExtension.class)
class AnagramControllerTest {
    
    @Test
    @DisplayName("Deve retornar erro 401 para usuário não autenticado")
    void shouldReturnUnauthorizedForUnauthenticatedUser() throws Exception {
        AnagramRequest request = new AnagramRequest();
        request.setLetters("abc");
        
        mockMvc.perform(post("/api/anagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("Deve permitir acesso ao endpoint health sem autenticação")
    @WithMockUser(roles = "USER")
    void shouldAllowHealthEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/anagrams/health"))
                .andExpect(status().isOk());
    }
}
```

### **6.2 Testes de Integração**

#### **Testes Java com JUnit 5:**
- **AnagramApiIntegrationTest**: Testes HTTP reais com TestRestTemplate
- **RedisIntegrationTest**: Testes de integração com Redis Embedded

#### **Características dos Testes:**
- **Redis Embedded**: Funciona sem instalação externa
- **Autenticação Real**: Login JWT real em cada teste
- **HTTP Real**: TestRestTemplate para requisições HTTP completas
- **Spring Security**: Segurança ativa durante testes
- **Perfil de Teste**: Configuração específica para testes

#### **Cobertura:**
- ✅ **Autenticação JWT**: Login e validação de tokens
- ✅ **Controle de Acesso**: Verificação de roles e permissões
- ✅ **Cache Hit/Miss**: Verificação de comportamento
- ✅ **Cache Inteligente**: Reutilização de composições
- ✅ **Validação de Entrada**: Tratamento de erros
- ✅ **Performance**: Tempos de resposta
- ✅ **Gerenciamento**: Limpeza e status do cache
- ✅ **Redis Embedded**: Funcionamento sem dependências externas
- ✅ **HTTP Real**: TestRestTemplate com Spring Security ativo
- ✅ **Cenários de Carga**: Múltiplas requisições simultâneas

---

## 📊 **Monitoramento e Logs**

### **7.1 Logs de Segurança**

#### **Configuração:**
```properties
# Logging
logging.level.com.lucas.text_processing_api=DEBUG
logging.level.org.springframework.security=DEBUG
```

#### **Exemplos de Logs:**
```
INFO  - Tentativa de autenticação para usuário: admin
INFO  - Autenticação bem-sucedida para usuário: admin
DEBUG - Usuário autenticado via JWT: admin
WARN  - Token JWT inválido para usuário: user
ERROR - Erro na autenticação para usuário: invalid_user
```

### **7.2 Métricas de Segurança**

- **Login Rate**: Tentativas de login por minuto
- **Token Validation**: Taxa de validação de tokens
- **Access Control**: Tentativas de acesso negado
- **Role Usage**: Distribuição de uso por role

---

## 🚀 **Deploy e Configuração**

### **8.1 Configurações de Produção**

#### **JWT Secret:**
```properties
# PRODUÇÃO: Alterar para chave segura e única
jwt.secret=${JWT_SECRET:your-production-secret-key-here}
jwt.expiration=${JWT_EXPIRATION:86400000}
```

#### **Banco de Dados:**
```properties
# PRODUÇÃO: Usar banco persistente (PostgreSQL, MySQL)
spring.datasource.url=${DATABASE_URL:jdbc:postgresql://localhost:5432/text_processing}
spring.datasource.username=${DATABASE_USERNAME:app_user}
spring.datasource.password=${DATABASE_PASSWORD:secure_password}
```

#### **Redis:**
```properties
# PRODUÇÃO: Configurar Redis externo
spring.redis.host=${REDIS_HOST:redis.example.com}
spring.redis.port=${REDIS_PORT:6379}
spring.redis.password=${REDIS_PASSWORD:secure_redis_password}
```

### **8.2 Variáveis de Ambiente**

#### **Segurança:**
```bash
export JWT_SECRET="your-super-secure-jwt-secret-key-2024"
export JWT_EXPIRATION="86400000"
```

#### **Banco de Dados:**
```bash
export DATABASE_URL="jdbc:postgresql://localhost:5432/text_processing"
export DATABASE_USERNAME="app_user"
export DATABASE_PASSWORD="secure_password"
```

#### **Redis:**
```bash
export REDIS_HOST="redis.example.com"
export REDIS_PORT="6379"
export REDIS_PASSWORD="secure_redis_password"
```

---

## 🔮 **Roadmap e Melhorias Futuras**

### **9.1 Segurança**

- [ ] **Refresh Tokens**: Renovação automática de tokens
- [ ] **OAuth2**: Integração com provedores externos (Google, GitHub)
- [ ] **Rate Limiting**: Proteção contra abuso e ataques
- [ ] **Audit Logs**: Logs detalhados de acesso e uso
- [ ] **Multi-factor Authentication**: 2FA para usuários críticos

### **9.2 Performance**

- [ ] **Cache Distribuído**: Redis Cluster para alta disponibilidade
- [ ] **Compressão**: Compressão de dados no cache
- [ ] **Métricas Avançadas**: Prometheus + Grafana
- [ ] **Load Balancing**: Distribuição de carga

### **9.3 Funcionalidades**

- [ ] **Machine Learning**: Sugestões de palavras similares
- [ ] **API GraphQL**: Consultas flexíveis
- [ ] **WebSocket**: Notificações em tempo real
- [ ] **Multi-idioma**: Suporte a diferentes alfabetos

---

## 📚 **Referências e Recursos**

### **10.1 Documentação Oficial**

- **Spring Security**: https://docs.spring.io/spring-security/reference/
- **JWT**: https://jwt.io/introduction
- **Spring Boot**: https://spring.io/projects/spring-boot
- **H2 Database**: http://www.h2database.com/html/main.html

### **10.2 Padrões de Segurança**

- **OWASP Top 10**: https://owasp.org/www-project-top-ten/
- **JWT Best Practices**: https://auth0.com/blog/a-look-at-the-latest-draft-for-jwt-bcp/
- **Spring Security Best Practices**: https://spring.io/guides/topicals/spring-security-architecture/

### **10.3 Ferramentas de Teste**

- **Postman**: Testes de API
- **JUnit 5**: Framework de testes
- **Mockito**: Mocking framework
- **Spring Security Test**: Testes de segurança
- **TestRestTemplate**: Testes de integração HTTP reais
- **Redis Embedded**: Testes sem dependências externas
- **Spring Boot Test**: Configuração de testes integrados

---

## 🎯 **Conclusão**

A Text Processing API implementa uma arquitetura robusta e segura com:

- **Segurança JWT integrada** com Spring Security
- **Controle de acesso por roles** (USER/ADMIN)
- **Banco de dados H2** para usuários de teste
- **Cache Redis inteligente** com fallback
- **Testes automatizados** para todas as funcionalidades
- **Testes de integração** com Redis Embedded e HTTP real
- **Documentação completa** para desenvolvedores

A arquitetura segue princípios SOLID e padrões de projeto estabelecidos, garantindo:

- **Manutenibilidade**: Código bem estruturado e documentado
- **Testabilidade**: Componentes isolados e testáveis
- **Escalabilidade**: Preparado para crescimento futuro
- **Segurança**: Proteção robusta contra ataques comuns

**🚀 A API está pronta para uso com segurança JWT e testes robustos!** 🔐✨🧪
