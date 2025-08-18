# 🏗️ Diagramas UML - Text Processing API (COM JWT E TESTES)

## 📋 **Visão Geral**

Este documento contém os diagramas UML da Text Processing API criados usando apenas texto e ASCII art. Esta abordagem garante que os diagramas sejam sempre visíveis e editáveis, independentemente de ferramentas externas.

**🆕 ATUALIZADO**: Inclui camada de segurança JWT, testes de integração com Redis Embedded e TestRestTemplate.

---

## 🎯 **Diagrama de Classes Principal**

### **Estrutura Geral das Classes**

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              PRESENTATION LAYER                                    │
├─────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                        AnagramController                                   │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ - anagramService: AnagramService                                          │   │
│  │ + generateAnagrams(request: AnagramRequest): ResponseEntity<AnagramResponse> │   │
│  │ + getCacheStatus(): ResponseEntity<CacheStatus>                           │   │
│  │ + clearCache(letters: String): ResponseEntity<String>                     │   │
│  │ + health(): ResponseEntity<HealthResponse>                                │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                    │                                             │
│                                    │ uses                                        │
│                                    ▼                                             │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                         AuthController                                     │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ - authService: AuthService                                                 │   │
│  │ + login(request: AuthRequest): ResponseEntity<AuthResponse>               │   │
│  │ + register(request: RegisterRequest): ResponseEntity<AuthResponse>        │   │
│  │ + validateToken(token: String): ResponseEntity<ValidationResponse>        │   │
│  │ + health(): ResponseEntity<HealthResponse>                                │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ uses
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              BUSINESS LAYER                                       │
├─────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                          AnagramService                                    │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ - cacheService: RedisCacheService                                         │   │
│  │ - generator: AnagramGenerator                                             │   │
│  │ + generateAnagrams(letters: String): AnagramResponse                      │   │
│  │ - generateAnagramsInternal(letters: String): AnagramResponse             │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                    │                                             │
│                                    │ uses                                        │
│                                    ▼                                             │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                           AuthService                                      │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ - userRepository: UserRepository                                          │   │
│  │ - passwordEncoder: PasswordEncoder                                        │   │
│  │ - jwtService: JwtService                                                  │   │
│  │ - authenticationManager: AuthenticationManager                            │   │
│  │ + authenticate(request: AuthRequest): AuthResponse                        │   │
│  │ + loadUserByUsername(username: String): UserDetails                       │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                    │                                             │
│                                    │ uses                                        │
│                                    ▼                                             │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                           JwtService                                       │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ - secret: String                                                           │   │
│  │ - expiration: Long                                                         │   │
│  │ + generateToken(userDetails: UserDetails): String                          │   │
│  │ + extractUsername(token: String): String                                   │   │
│  │ + isTokenValid(token: String, userDetails: UserDetails): boolean          │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ uses
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                            DATA ACCESS LAYER                                      │
├─────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                        RedisCacheService                                   │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ - redisTemplate: RedisTemplate<String, Object>                            │   │
│  │ - memoryCache: ConcurrentHashMap<String, CacheEntry>                      │   │
│  │ - cacheTtl: long                                                          │   │
│  │ - redisAvailable: boolean                                                 │   │
│  │ + getFromCache(letters: String): AnagramResponse                          │   │
│  │ + saveToCache(letters: String, response: AnagramResponse): void           │   │
│  │ + removeFromCache(letters: String): void                                  │   │
│  │ + clearCache(): void                                                      │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                    │                                             │
│                                    │ uses                                        │
│                                    ▼                                             │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                        AnagramGenerator                                    │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ + generateAnagrams(letters: String): List<String>                         │   │
│  │ + calculateTotalAnagrams(letters: String): long                           │   │
│  │ - backtrack(letters: String, used: boolean[], current: String, result: List<String>): void │
│  │ - validateInput(letters: String): void                                    │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                    │                                             │
│                                    │ uses                                        │
│                                    ▼                                             │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                          UserRepository                                    │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ + findByUsername(username: String): Optional<User>                        │   │
│  │ + existsByUsername(username: String): boolean                             │   │
│  │ + existsByEmail(email: String): boolean                                   │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                    │                                             │
│                                    │ manages                                     │
│                                    ▼                                             │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                              User Entity                                   │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ - id: Long                                                                │   │
│  │ - username: String                                                        │   │
│  │ - password: String                                                        │   │
│  │ - email: String                                                           │   │
│  │ - fullName: String                                                        │   │
│  │ - isActive: boolean                                                       │   │
│  │ - role: String                                                            │   │
│  │ + getAuthorities(): Collection<GrantedAuthority>                          │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ uses
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                           INFRASTRUCTURE LAYER                                    │
├─────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                            RedisConfig                                     │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ + redisConnectionFactory(): RedisConnectionFactory                         │   │
│  │ + embeddedRedisConnectionFactory(): RedisConnectionFactory                 │   │
│  │ + embeddedRedisServer(): RedisServer                                      │   │
│  │ + redisTemplate(): RedisTemplate<String, Object>                          │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                    │                                             │
│                                    │ manages                                     │
│                                    ▼                                             │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                       EmbeddedRedisConfig                                  │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ - redisServer: RedisServer                                                 │   │
│  │ + startRedis(): void                                                       │   │
│  │ + stopRedis(): void                                                        │   │
│  │ + embeddedRedisConnectionFactory(): RedisConnectionFactory                 │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                    │                                             │
│                                    │ uses                                        │
│                                    ▼                                             │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                          SecurityConfig                                    │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ + securityFilterChain(http: HttpSecurity): SecurityFilterChain             │   │
│  │ + authenticationProvider(): AuthenticationProvider                         │   │
│  │ + passwordEncoder(): PasswordEncoder                                       │   │
│  │ + userDetailsService(): UserDetailsService                                 │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                    │                                             │
│                                    │ uses                                        │
│                                    ▼                                             │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                    JwtAuthenticationFilter                                │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ - jwtService: JwtService                                                   │   │
│  │ - userDetailsService: UserDetailsService                                   │   │
│  │ + doFilterInternal(request, response, filterChain): void                  │   │
│  │ + extractToken(request: HttpServletRequest): String                       │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### **DTOs e Classes de Suporte**

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              DTOs e Classes                                      │
├─────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                        AnagramRequest                                      │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ - letters: String                                                          │   │
│  │ + getLetters(): String                                                     │   │
│  │ + setLetters(letters: String): void                                        │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                    │                                             │
│                                    │ uses                                        │
│                                    ▼                                             │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                       AnagramResponse                                      │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ - originalLetters: String                                                  │   │
│  │ - anagrams: List<String>                                                   │   │
│  │ - totalAnagrams: long                                                      │   │
│  │ - fromCache: boolean                                                       │   │
│  │ - processingTimeMs: long                                                   │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                    │                                             │
│                                    │ uses                                        │
│                                    ▼                                             │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                         AuthRequest                                        │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ - username: String                                                         │   │
│  │ - password: String                                                         │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                    │                                             │
│                                    │ uses                                        │
│                                    ▼                                             │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                        AuthResponse                                        │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ - token: String                                                            │   │
│  │ - type: String                                                             │   │
│  │ - username: String                                                         │   │
│  │ - role: String                                                             │   │
│  │ - message: String                                                          │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                    │                                             │
│                                    │ uses                                        │
│                                    ▼                                             │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                      RegisterRequest                                       │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ - username: String                                                         │   │
│  │ - password: String                                                         │   │
│  │ - email: String                                                            │   │
│  │ - fullName: String                                                         │   │
│  │ - role: String                                                             │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                    │                                             │
│                                    │ uses                                        │
│                                    ▼                                             │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                    ValidationResponse                                      │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ - valid: boolean                                                           │   │
│  │ - username: String                                                         │   │
│  │ - role: String                                                             │   │
│  │ - message: String                                                          │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                    DTOs                                           │
├─────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                         AnagramRequest                                     │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ - letters: String                                                          │   │
│  │ + getLetters(): String                                                     │   │
│  │ + setLetters(letters: String): void                                        │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                    │                                             │
│                                    │ creates                                     │
│                                    ▼                                             │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                        AnagramResponse                                     │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ - letters: String                                                          │   │
│  │ - anagrams: List<String>                                                   │   │
│  │ - total: int                                                               │   │
│  │ - fromCache: boolean                                                       │   │
│  │ - processingTimeMs: long                                                   │   │
│  │ + getLetters(): String                                                     │   │
│  │ + getAnagrams(): List<String>                                              │   │
│  │ + getTotal(): int                                                          │   │
│  │ + isFromCache(): boolean                                                   │   │
│  │ + getProcessingTimeMs(): long                                              │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              Exception Handling                                   │
├─────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                     GlobalExceptionHandler                                  │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ + handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> │
│  │ + handleGenericException(ex: Exception): ResponseEntity<ErrorResponse>     │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                    │                                             │
│                                    │ creates                                     │
│                                    ▼                                             │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                          ErrorResponse                                     │   │
│  ├─────────────────────────────────────────────────────────────────────────────┤   │
│  │ - message: String                                                           │   │
│  │ - errors: List<String>                                                      │   │
│  │ - timestamp: LocalDateTime                                                  │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔄 **Diagrama de Sequência - Geração de Anagramas**

### **Fluxo de Autenticação JWT**

```
┌─────────┐    ┌──────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────┐
│ Client  │    │ AuthController│   │ AuthService │   │ JwtService  │   │  Redis  │
└────┬────┘    └──────┬───────┘    └─────┬───────┘    └─────┬───────┘    └────┬────┘
     │                 │                  │                  │                 │
     │ POST /login     │                  │                  │                 │
     │ ──────────────►│                  │                  │                 │
     │                 │                  │                  │                 │
     │                 │ authenticate    │                  │                 │
     │                 │ ──────────────► │                  │                 │
     │                 │                  │                  │                 │
     │                 │                  │ validateUser     │                  │                 │
     │                 │                  │ ──────────────► │                 │
     │                 │                  │                  │                 │
     │                 │                  │                  │ generateToken  │                 │
     │                 │                  │                  │ ──────────────►│                 │
     │                 │                  │                  │                 │                 │
     │                 │                  │                  │ JWT Token      │                 │
     │                 │                  │                  │ ◄──────────────│                 │
     │                 │                  │                  │                 │                 │
     │                 │                  │ AuthResponse     │                 │                 │
     │                 │ ◄────────────────│                  │                 │                 │
     │                 │                  │                  │                 │                 │
     │ HTTP 200 + JWT  │                  │                  │                 │                 │
     │ ◄───────────────│                  │                  │                 │                 │
     │                 │                  │                  │                 │                 │
```

### **Fluxo Principal de Geração (com Autenticação)**

### **Fluxo Principal de Geração (com Autenticação)**

```
┌─────────┐    ┌──────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────┐    ┌─────────────┐
│ Client  │    │   Controller │    │   Service   │    │    Cache    │    │  Redis  │    │ Generator   │
└────┬────┘    └──────┬───────┘    └─────┬───────┘    └─────┬───────┘    └────┬────┘    └─────┬───────┘
     │                 │                  │                  │                 │                │
     │ POST /generate  │                  │                  │                 │                │
     │ + Bearer Token │                  │                  │                 │                │
     │ ──────────────►│                  │                  │                 │                │
     │                 │                  │                  │                 │                │
     │                 │ Validate request │                  │                 │                │
     │                 │ ──────────────► │                  │                 │                │
     │                 │                  │                  │                 │                │
     │                 │                  │ generateAnagrams │                 │                │
     │                 │                  │ ──────────────► │                  │                │
     │                 │                  │                  │                  │                │
     │                 │                  │                  │ getFromCache    │                 │
     │                 │                  │                  │ ──────────────►│                 │
     │                 │                  │                  │                  │                │
     │                 │                  │                  │                 │ GET anagram:xyz│
     │                 │                  │                  │                 │ ──────────────►│
     │                 │                  │                  │                 │                 │
     │                 │                  │                  │                 │ null (miss)    │
     │                 │                  │                  │                 │ ◄──────────────│
     │                 │                  │                  │                 │                 │
     │                 │                  │                  │ Check memory    │                 │
     │                 │                  │                  │ ◄──────────────│                 │
     │                 │                  │                  │                 │                 │
     │                 │                  │                  │ null (miss)    │                 │
     │                 │                  │ ◄────────────────│                 │                 │
     │                 │                  │                  │                 │                 │
     │                 │                  │ generateAnagrams │                 │                 │
     │                 │                  │ ──────────────► │                 │                 │
     │                 │                  │                  │                 │                 │
     │                 │                  │                  │                 │                 │ List<String>
     │                 │                  │                  │                 │                 │ ◄──────────────│
     │                 │                  │                  │                 │                 │
     │                 │                  │ Create Response │                  │                 │
     │                 │                  │ ◄──────────────│                  │                 │
     │                 │                  │                  │                 │                 │
     │                 │                  │                  │ saveToCache     │                 │
     │                 │                  │                  │ ──────────────►│                 │
     │                 │                  │                  │                  │                │
     │                 │                  │                  │                 │ SET anagram:xyz│
     │                 │                  │                 │ ──────────────►│                 │
     │                 │                  │                  │                 │                 │
     │                 │                  │                  │                 │ SET sorted:xyz │
     │                 │                  │                 │ ──────────────►│                 │
     │                 │                  │                  │                 │                 │
     │                 │                  │                  │ Save to memory │                 │
     │                 │                  │ ◄────────────────│                 │                 │
     │                 │                  │                  │                 │                 │
     │                 │                  │ AnagramResponse  │                  │                 │
     │                 │ ◄────────────────│                  │                 │                 │
     │                 │                  │                  │                 │                 │
     │ HTTP 200 + JSON │                  │                  │                 │                 │
     │ ◄───────────────│                  │                  │                 │                 │
     │                 │                  │                  │                 │                 │
```

### **Fluxo de Cache Hit**

```
┌─────────┐    ┌──────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────┐
│ Client  │    │   Controller │    │   Service   │    │    Cache    │    │  Redis  │
└────┬────┘    └──────┬───────┘    └─────┬───────┘    └─────┬───────┘    └────┬────┘
     │                 │                  │                  │                 │
     │ POST /generate  │                  │                  │                 │
     │ + Bearer Token │                  │                  │                 │
     │ ──────────────►│                  │                  │                 │
     │                 │                  │                  │                 │
     │                 │ Validate request │                  │                 │
     │                 │ ──────────────► │                  │                 │
     │                 │                  │                  │                 │
     │                 │                  │ generateAnagrams │                 │
     │                 │                  │ ──────────────► │                  │
     │                 │                  │                  │                  │
     │                 │                  │                  │ getFromCache    │                 │
     │                 │                  │                  │ ──────────────►│                 │
     │                 │                  │                  │                  │                │
     │                 │                  │                  │                 │ GET anagram:test│
     │                 │                  │                  │                 │ ──────────────►│
     │                 │                  │                  │                 │                 │
     │                 │                  │                  │                 │ AnagramResponse │
     │                 │                  │                  │                 │ ◄──────────────│
     │                 │                  │                  │                 │                 │
     │                 │                  │                  │ AnagramResponse │                 │
     │                 │                  │ ◄────────────────│                 │                 │
     │                 │                  │                  │                 │                 │
     │                 │                  │ AnagramResponse  │                  │                 │
     │                 │ ◄────────────────│                  │                 │                 │
     │                 │                  │                  │                 │                 │
     │ HTTP 200 + JSON │                  │                  │                 │                 │
     │ ◄───────────────│                  │                  │                 │                 │
     │                 │                  │                  │                 │                 │
```

---

## 🏗️ **Diagrama de Componentes**

### **Arquitetura de Componentes**

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              Text Processing API                                   │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐                │
│  │   Web Layer     │    │ Business Logic  │    │   Cache Layer   │                │
│  │                 │    │                 │    │                 │                │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │                │
│  │ │Controller   │ │    │ │Service      │ │    │ │CacheService │ │                │
│  │ │Exception    │ │    │ │Generator    │ │    │ │Memory Cache │ │                │
│  │ │Handler      │ │    │ │             │ │    │ │             │ │                │
│  └─┴─────────────┴─┘    └─┴─────────────┴─┘    └─┴─────────────┴─┘                │
│           │                       │                       │                        │
│           │ HTTP requests         │ Business logic         │ Data access            │
│           ▼                       ▼                       ▼                        │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐                │
│  │ Security Layer  │    │ Auth Services   │    │   Data Layer    │                │
│  │                 │    │                 │    │                 │                │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │                │
│  │ │JWT Filter   │ │    │ │AuthService  │ │    │ │UserRepo     │ │                │
│  │ │Security     │ │    │ │JwtService   │ │    │ │User Entity  │ │                │
│  │ │Config       │ │    │ │             │ │    │ │             │ │                │
│  └─┴─────────────┴─┘    └─┴─────────────┴─┘    └─┴─────────────┴─┘                │
│           │                       │                       │                        │
│           │ HTTP requests         │ Business logic         │ Data access            │
│           ▼                       ▼                       ▼                        │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐                │
│  │ Configuration   │    │ Data Storage    │    │External Depends │                │
│  │                 │    │                 │    │                 │                │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │                │
│  │ │RedisConfig  │ │    │ │Redis        │ │    │ │Spring Boot  │ │                │
│  │ │Embedded     │ │    │ │Embedded     │ │    │ │Spring Redis │ │                │
│  │ │Config       │ │    │ │Redis        │ │    │ │Lettuce      │ │                │
│  └─┴─────────────┴─┘    └─┴─────────────┴─┘    └─┴─────────────┴─┘                │
│           │                       │                       │                        │
│           │ Configures            │ Stores data            │ Provides framework    │
│           ▼                       ▼                       ▼                        │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 🗄️ **Diagrama de Pacotes**

### **Estrutura de Pacotes Java**

```
com.lucas.text_processing_api
├── controller
│   ├── AnagramController
│   ├── AuthController
│   └── GlobalExceptionHandler
├── service
│   ├── AnagramService
│   ├── RedisCacheService
│   ├── AuthService
│   └── JwtService
├── util
│   └── AnagramGenerator
├── config
│   ├── RedisConfig
│   ├── SecurityConfig
│   └── JwtAuthenticationFilter
├── dto
│   ├── AnagramRequest
│   ├── AnagramResponse
│   ├── AuthRequest
│   ├── AuthResponse
│   ├── RegisterRequest
│   ├── ValidationResponse
│   ├── CacheStatus
│   ├── HealthResponse
│   └── ErrorResponse
├── entity
│   └── User
├── repository
│   └── UserRepository
├── exception
│   └── CustomException
└── model
    └── CacheEntry

Dependencies:
controller → service
controller → dto
service → util
service → dto
service → model
service → entity
service → repository
config → service
security → service
filter → service
```

---

## 🧪 **Diagrama de Testes de Integração**

### **Arquitetura de Testes**

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              Testes de Integração                                  │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐                │
│  │   Test Layer    │    │  HTTP Client    │    │   Test Config   │                │
│  │                 │    │                 │    │                 │                │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │                │
│  │ │Integration  │ │    │ │TestRest     │ │    │ │Test Profile │ │                │
│  │ │Tests        │ │    │ │Template     │ │    │ │Redis        │ │                │
│  │ │             │ │    │ │             │ │    │ │Embedded     │ │                │
│  └─┴─────────────┴─┘    └─┴─────────────┴─┘    └─┴─────────────┴─┘                │
│           │                       │                       │                        │
│           │ Executes              │ Makes HTTP            │ Provides               │
│           ▼                       ▼                       ▼                        │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐                │
│  │   Application   │    │   Spring        │    │   Test          │                │
│  │                 │    │   Security      │    │   Environment   │                │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │                │
│  │ │Spring Boot  │ │    │ │JWT Auth     │ │    │ │H2 Database  │ │                │
│  │ │App          │ │    │ │Active        │ │    │ │Redis        │ │                │
│  │ │             │ │    │ │             │ │    │ │Embedded     │ │                │
│  └─┴─────────────┴─┘    └─┴─────────────┴─┘    └─┴─────────────┴─┘                │
│           │                       │                       │                        │
│           │ Runs                  │ Protects              │ Supports               │
│           ▼                       ▼                       ▼                        │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### **Fluxo de Teste de Integração**

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ Test Class  │    │ TestRest    │    │ Spring      │    │ Redis       │
│             │    │ Template    │    │ Security    │    │ Embedded    │
└─────┬───────┘    └─────┬───────┘    └─────┬───────┘    └─────┬───────┘
      │                  │                  │                  │
      │ 1. Setup         │                  │                  │
      │ ──────────────► │                  │                  │
      │                  │                  │                  │
      │ 2. Login         │                  │                  │
      │ ──────────────► │                  │                  │
      │                  │ POST /login     │                  │
      │                  │ ──────────────► │                  │
      │                  │                  │                  │
      │                  │                  │ Validate         │                  │
      │                  │                  │ ──────────────► │                  │
      │                  │                  │                  │                  │
      │                  │                  │                  │ Start Redis     │                  │
      │                  │                  │                  │ ──────────────► │                  │
      │                  │                  │                  │                  │                  │
      │                  │                  │                  │ Redis Ready     │                  │
      │                  │                  │                  │ ◄──────────────│                  │
      │                  │                  │                  │                  │                  │
      │                  │                  │ JWT Token        │                  │                  │
      │                  │                  │ ◄────────────────│                  │                  │
      │                  │                  │                  │                  │                  │
      │                  │ AuthResponse     │                  │                  │                  │
      │                  │ ◄────────────────│                  │                  │                  │
      │                  │                  │                  │                  │                  │
      │ JWT Token        │                  │                  │                  │                  │
      │ ◄────────────────│                  │                  │                  │                  │
      │                  │                  │                  │                  │                  │
      │ 3. Test Request  │                  │                  │                  │                  │
      │ ──────────────► │                  │                  │                  │                  │
      │                  │ POST /generate  │                  │                  │                  │
      │                  │ + Bearer Token  │                  │                  │                  │
      │                  │ ──────────────► │                  │                  │                  │
      │                  │                  │                  │                  │                  │
      │                  │                  │ Process Request  │                  │                  │
      │                  │                  │ ──────────────► │                  │                  │
      │                  │                  │                  │                  │                  │
      │                  │                  │                  │ Cache/Generate  │                  │                  │
      │                  │                  │                  │ ──────────────► │                  │                  │
      │                  │                  │                  │                  │                  │                  │
      │                  │                  │                  │ AnagramResponse │                  │                  │                  │
      │                  │                  │                  │ ◄──────────────│                  │                  │                  │
      │                  │                  │                  │                  │                  │                  │
      │                  │                  │ AnagramResponse  │                  │                  │                  │
      │                  │ ◄────────────────│                  │                  │                  │                  │
      │                  │                  │                  │                  │                  │                  │
      │                  │ AnagramResponse  │                  │                  │                  │                  │                  │
      │                  │ ◄────────────────│                  │                  │                  │                  │                  │
      │                  │                  │                  │                  │                  │                  │
      │ Test Result      │                  │                  │                  │                  │                  │                  │
      │ ◄────────────────│                  │                  │                  │                  │                  │                  │
      │                  │                  │                  │                  │                  │                  │                  │
```

---

## 🔄 **Diagrama de Estados - Cache Service**

### **Estados do Sistema de Cache**

```
                    ┌─────────────┐
                    │    Start    │
                    └─────┬───────┘
                          │
                          ▼
                    ┌─────────────┐
                    │RedisAvailable│
                    └─────┬───────┘
                          │
                          ▼
                    ┌─────────────┐
                    │NormalOperation│
                    └─────┬───────┘
                          │
                    ┌─────┴─────┐
                    │           │
                    ▼           ▼
              ┌─────────┐ ┌─────────┐
              │CacheHit │ │CacheMiss│
              └────┬────┘ └────┬────┘
                   │           │
                   └─────┬─────┘
                         │
                         ▼
                    ┌─────────────┐
                    │NormalOperation│
                    └─────────────┘
                          │
                          │ Redis fails
                          ▼
                    ┌─────────────┐
                    │RedisUnavailable│
                    └─────┬───────┘
                          │
                          │ Connection timeout
                          ▼
                    ┌─────────────┐
                    │FallbackMode │
                    └─────┬───────┘
                          │
                          ▼
                    ┌─────────────┐
                    │ MemoryOnly  │
                    └─────┬───────┘
                          │
                    ┌─────┴─────┐
                    │           │
                    ▼           ▼
              ┌─────────┐ ┌─────────┐
              │MemoryHit│ │MemoryMiss│
              └────┬────┘ └────┬────┘
                   │           │
                   └─────┬─────┘
                         │
                         ▼
                    ┌─────────────┐
                    │ MemoryOnly  │
                    └─────────────┘
                          │
                          │ Redis recovers
                          ▼
                    ┌─────────────┐
                    │RedisAvailable│
                    └─────────────┘
```

---

## 📊 **Diagrama de Atividades - Processo de Geração**

### **Fluxo de Atividades Principal**

```
                    ┌─────────┐
                    │  Start  │
                    └────┬────┘
                         │
                         ▼
                    ┌─────────────┐
                    │Receive HTTP │
                    │  Request    │
                    └────┬────────┘
                         │
                         ▼
                    ┌─────────────┐
                    │Validate     │
                    │Input Letters│
                    └────┬────────┘
                         │
                         │ Valid?
                    ┌────┴────┐
                    │         │
                    ▼         ▼
              ┌─────────┐ ┌─────────┐
              │   Yes   │ │   No    │
              └────┬────┘ └────┬────┘
                   │           │
                   ▼           ▼
              ┌─────────┐ ┌─────────┐
              │Check    │ │Return   │
              │Cache    │ │Validation│
              │First    │ │Error    │
              └────┬────┘ └────┬────┘
                   │           │
                   ▼           │
              ┌─────────┐      │
              │Cache    │      │
              │Hit?     │      │
              └────┬────┘      │
                   │           │
            ┌──────┴──────┐    │
            │             │    │
            ▼             ▼    │
      ┌─────────┐ ┌─────────┐  │
      │   Yes   │ │   No    │  │
      └────┬────┘ └────┬────┘  │
           │           │       │
           ▼           ▼       │
      ┌─────────┐ ┌─────────┐  │
      │Return   │ │Generate │  │
      │Cached   │ │New      │  │
      │Response │ │Anagrams │  │
      └────┬────┘ └────┬────┘  │
           │           │       │
           │           ▼       │
           │      ┌─────────┐  │
           │      │Create   │  │
           │      │Response │  │
           │      └────┬────┘  │
           │           │       │
           │           ▼       │
           │      ┌─────────┐  │
           │      │Save to  │  │
           │      │Cache    │  │
           │      └────┬────┘  │
           │           │       │
           │           ▼       │
           │      ┌─────────┐  │
           │      │Set      │  │
           │      │fromCache│  │
           │      │= false  │  │
           │      └────┬────┘  │
           │           │       │
           └───────────┼───────┘
                       │
                       ▼
                  ┌─────────┐
                  │Calculate│
                  │Processing│
                  │Time     │
                  └────┬────┘
                       │
                       ▼
                  ┌─────────┐
                  │Return   │
                  │Success  │
                  │Response │
                  └────┬────┘
                       │
                       ▼
                    ┌─────────┐
                    │  Stop   │
                    └─────────┘
```

---

## 🔌 **Diagrama de Implantação**

### **Arquitetura de Implantação**

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              Client Applications                                   │
├─────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                            │
│  │Web Browser  │    │Mobile App   │    │API Client   │                            │
│  └─────┬───────┘    └─────┬───────┘    └─────┬───────┘                            │
└────────┼──────────────────┼──────────────────┼────────────────────────────────────┘
         │                  │                  │
         │ HTTP/HTTPS       │ REST API         │ REST API
         ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              Load Balancer                                        │
├─────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                            │
│  │   Nginx     │    │   HAProxy   │    │   Traefik   │                            │
│  └─────────────┘    └─────────────┘    └─────────────┘                            │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ Load Balance
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                            Application Servers                                    │
├─────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                            │
│  │Spring Boot  │    │Spring Boot  │    │Spring Boot  │                            │
│  │   App 1     │    │   App 2     │    │   App N     │                            │
│  └─────┬───────┘    └─────┬───────┘    └─────┬───────┘                            │
└────────┼──────────────────┼──────────────────┼────────────────────────────────────┘
         │                  │                  │
         │ Cache Operations │ Cache Operations │ Cache Operations
         ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              Cache Layer                                          │
├─────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                            │
│  │Redis Cluster│    │Redis        │    │Redis       │                            │
│  │  Master     │    │ Sentinel    │    │ Replica    │                            │
│  └─────┬───────┘    └─────────────┘    └─────┬───────┘                            │
└────────┼──────────────────────────────────────┼────────────────────────────────────┘
         │                                      │
         │ Persistence                          │ Backup
         ▼                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              Storage Layer                                        │
├─────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                            │
│  │File System  │    │Database     │    │Cloud        │                            │
│  │             │    │(Future)     │    │Storage      │                            │
│  └─────────────┘    └─────────────┘    └─────────────┘                            │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 🧪 **Diagrama de Testes**

### **Estrutura de Testes**

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              Test Suite                                            │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐                │
│  │  Unit Tests     │    │Integration Tests│    │Performance Tests│                │
│  │                 │    │                 │    │                 │                │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │                │
│  │ │Generator    │ │    │ │HTTP Real    │ │    │ │Load Test    │ │                │
│  │ │Service      │ │    │ │Integration  │ │    │ │Stress Test  │ │                │
│  │ │Cache        │ │    │ │Redis        │ │    │ │Cache        │ │                │
│  │ │Controller   │ │    │ │Embedded     │ │    │ │Performance  │ │                │
│  │ │Auth         │ │    │ │JWT Auth     │ │    │ │             │ │                │
│  └─┴─────────────┴─┘    └─┴─────────────┴─┘    └─┴─────────────┴─┘                │
│           │                       │                       │                        │
│           │ Tests                 │ Tests                  │ Tests                  │
│           ▼                       ▼                       ▼                        │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐                │
│  │ Test Utilities  │    │ Test Execution  │    │ Test Framework  │                │
│  │                 │    │                 │    │                 │                │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │                │
│  │ │TestData     │ │    │ │Maven        │ │    │ │JUnit 5      │ │                │
│  │ │Builder      │ │    │ │Surefire     │ │    │ │Mockito      │ │                │
│  │ │MockFactory  │ │    │ │Failsafe     │ │    │ │Spring Boot  │ │                │
│  │ │TestConfig   │ │    │ │             │ │    │ │Test         │ │                │
│  │ │Redis        │ │    │ │             │ │    │ │TestRest     │ │                │
│  │ │Embedded     │ │    │ │             │ │    │ │Template     │ │                │
│  └─┴─────────────┴─┘    └─┴─────────────┴─┘    └─┴─────────────┴─┘                │
│           │                       │                       │                        │
│           │ Provides data         │ Executes tests         │ Provides framework     │
│           ▼                       ▼                       ▼                        │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔍 **Diagrama de Casos de Uso**

### **Casos de Uso Principais**

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              Text Processing API                                   │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│  ┌─────────────┐                    ┌─────────────┐                                │
│  │API User     │                    │System Admin │                                │
│  └─────┬───────┘                    └─────┬───────┘                                │
│        │                                  │                                        │
│        │ Generate Anagrams                │ Clear Cache                             │
│        │ ──────────────┐                 │ ──────────────┐                         │
│        │               │                 │               │                         │
│        ▼               ▼                 ▼               ▼                         │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐                 │
│  │Generate     │ │Check Cache  │ │Clear Cache  │ │Health      │                 │
│  │Anagrams     │ │Status       │ │             │ │Check       │                 │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘                 │
│        │               │                 │               │                        │
│        │               │                 │               │                        │
│        │               │                 │               │                        │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐                 │
│  │Validate     │ │Cache        │ │Cache        │ │System      │                 │
│  │Input        │ │Monitoring   │ │Maintenance  │ │Health      │                 │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘                 │
│                                                                                     │
│  ┌─────────────┐                    ┌─────────────┐                                │
│  │Authentication│                    │Testing      │                                │
│  │System        │                    │System       │                                │
│  └─────┬───────┘                    └─────┬───────┘                                │
│        │                                  │                                        │
│        │ User Authentication              │ Integration Tests                       │
│        │ ──────────────┐                 │ ──────────────┐                         │
│        │               │                 │               │                         │
│        ▼               ▼                 ▼               ▼                         │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐                 │
│  │Login        │ │Register     │ │Redis        │ │HTTP        │                 │
│  │JWT Token    │ │New User     │ │Embedded     │ │Real Tests  │                 │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘                 │
│                                                                                     │
│  ┌─────────────┐                    ┌─────────────┐                                │
│  │Monitoring   │                    │Error       │                                │
│  │System       │                    │Handling    │                                │
│  └─────┬───────┘                    └─────┬───────┘                                │
│        │                                  │                                        │
│        │ Monitor Performance              │ Handle Errors                           │
│        │ ──────────────┐                 │ ──────────────┐                         │
│        │               │                 │               │                         │
│        ▼               ▼                 ▼               ▼                         │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐                 │
│  │Monitor      │ │Performance  │ │Handle       │ │Error       │                 │
│  │Performance  │ │Metrics      │ │Errors       │ │Logging     │                 │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘                 │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 📈 **Diagrama de Métricas e Monitoramento**

### **Sistema de Métricas**

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              Metrics System                                        │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐                │
│  │Application      │    │Business         │    │Infrastructure   │                │
│  │Metrics          │    │Metrics          │    │Metrics          │                │
│  │                 │    │                 │    │                 │                │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │                │
│  │ │Response     │ │    │ │Anagrams     │ │    │ │Redis        │ │                │
│  │ │Time         │ │    │ │Generated    │ │    │ │Performance  │ │                │
│  │ │Cache Hit    │ │    │ │Cache        │ │    │ │Memory       │ │                │
│  │ │Rate         │ │    │ │Efficiency   │ │    │ │Usage        │ │                │
│  │ │Error Rate   │ │    │ │User         │ │    │ │CPU Usage    │ │                │
│  │ │Throughput   │ │    │ │Patterns     │ │    │ │             │ │                │
│  └─┴─────────────┴─┘    └─┴─────────────┴─┘    └─┴─────────────┴─┘                │
│           │                       │                       │                        │
│           │ Exposes               │ Collects               │ Exposes                │
│           ▼                       ▼                       ▼                        │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐                │
│  │Monitoring       │    │Metrics          │    │Observability    │                │
│  │Tools            │    │Collection       │    │Platform         │                │
│  │                 │    │                 │    │                 │                │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │                │
│  │ │Spring Boot  │ │    │ │Micrometer   │ │    │ │Prometheus   │ │                │
│  │ │Actuator     │ │    │ │             │ │    │ │             │ │                │
│  │ │             │ │    │ │             │ │    │ │             │ │                │
│  └─┴─────────────┴─┘    └─┴─────────────┴─┘    └─┴─────────────┴─┘                │
│           │                       │                       │                        │
│           │ Exposes               │ Collects               │ Scrapes                │
│           ▼                       ▼                       ▼                        │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐                │
│  │   Prometheus    │    │    Grafana      │    │   Alerting      │                │
│  │                 │    │                 │    │                 │                │
│  │ Scrapes metrics │    │ Visualizes      │    │ Sends alerts    │                │
│  │ Stores time     │    │ Creates         │    │ Notifies        │                │
│  │ series data     │    │ dashboards      │    │ stakeholders    │                │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘                │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔧 **Como Manter os Diagramas**

### **1. Atualização Manual**
- Edite diretamente o arquivo markdown
- Mantenha a formatação ASCII art consistente
- Use caracteres Unicode para melhor visualização

### **2. Versionamento**
- Commit as mudanças junto com o código
- Mantenha sincronizados com a implementação
- Use comentários para explicar decisões

### **3. Validação**
- Revise com a equipe regularmente
- Valide contra o código implementado
- Atualize quando a arquitetura mudar

---

## 📚 **Referências UML**

### **Padrões Utilizados**
- **Diagrama de Classes**: Estrutura estática do sistema
- **Diagrama de Sequência**: Interação entre componentes
- **Diagrama de Componentes**: Arquitetura de alto nível
- **Diagrama de Pacotes**: Organização do código
- **Diagrama de Estados**: Comportamento do sistema
- **Diagrama de Atividades**: Fluxo de processos
- **Diagrama de Implantação**: Infraestrutura
- **Diagrama de Casos de Uso**: Funcionalidades do sistema

### **Vantagens da Abordagem Textual**
- **Sempre Visível**: Não depende de ferramentas externas
- **Fácil Edição**: Qualquer editor de texto
- **Versionamento**: Controle de versão eficiente
- **Portabilidade**: Funciona em qualquer ambiente

---

## 🎯 **Conclusão**

Os diagramas UML em formato textual fornecem uma visão completa e acessível da arquitetura da Text Processing API, incluindo a nova camada de segurança JWT e sistema de testes de integração:

### **✅ Benefícios:**
- **Acessibilidade**: Sem dependências externas
- **Manutenibilidade**: Fácil de atualizar
- **Versionamento**: Controle de versão eficiente
- **Portabilidade**: Funciona em qualquer ambiente
- **Completude**: Cobre todas as camadas da aplicação


---
