# Exemplos cURL para Windows - Text Processing API (COM JWT)

Este arquivo contém exemplos de como testar a API de anagramas usando cURL no Windows, incluindo comandos PowerShell equivalentes, com autenticação JWT.

## 🚀 **Pré-requisitos**

- **cURL**: Incluído no Windows 10/11 por padrão
- **PowerShell**: Disponível em todas as versões do Windows
- **API rodando**: Certifique-se de que a aplicação está rodando em `http://localhost:8080`

## 🔐 **Autenticação JWT**

### 1. **Login para Obter Token**

```bash
# cURL
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"admin\", \"password\": \"admin123\"}"

# PowerShell equivalente
$loginData = @{ username = "admin"; password = "admin123" } | ConvertTo-Json
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $loginData -ContentType "application/json"
$token = $response.token
Write-Host "Token: $token"
```

**Resposta esperada:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin",
  "role": "ADMIN",
  "message": "Autenticação realizada com sucesso"
}
```

### 2. **Login como Usuário Normal**

```bash
# cURL
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"user\", \"password\": \"user123\"}"

# PowerShell equivalente
$loginData = @{ username = "user"; password = "user123" } | ConvertTo-Json
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $loginData -ContentType "application/json"
$userToken = $response.token
Write-Host "User Token: $userToken"
```

### 3. **Validar Token**

```bash
# cURL
curl -X GET "http://localhost:8080/api/auth/validate" \
  -H "Authorization: Bearer $token"

# PowerShell equivalente
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/validate" -Method Get -Headers $headers
```

## 📋 **Comandos Básicos**

### 4. **Verificar Saúde da API (SEM AUTENTICAÇÃO - PÚBLICO)**

```bash
# cURL
curl http://localhost:8080/api/anagrams/health

# PowerShell equivalente
Invoke-RestMethod -Uri "http://localhost:8080/api/anagrams/health" -Method Get
```

**Resposta esperada:**
```json
{
  "message": "Text Processing API está funcionando",
  "status": "UP",
  "timestamp": "2025-08-16T09:10:01.434784600"
}
```

### 5. **Verificar Saúde da Autenticação (SEM AUTENTICAÇÃO - PÚBLICO)**

```bash
# cURL
curl http://localhost:8080/api/auth/health

# PowerShell equivalente
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/health" -Method Get
```

**Resposta esperada:**
```json
{
  "status": "UP",
  "message": "Serviço de autenticação está funcionando",
  "timestamp": "2025-08-16T09:10:01.434784600"
}
```

### 6. **Status do Cache (COM AUTENTICAÇÃO)**

```bash
# cURL
curl -X GET "http://localhost:8080/api/anagrams/cache/status" \
  -H "Authorization: Bearer $token"

# PowerShell equivalente
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri "http://localhost:8080/api/anagrams/cache/status" -Method Get -Headers $headers
```

**Resposta esperada:**
```json
{
  "cacheEnabled": true,
  "message": "Status do cache recuperado com sucesso"
}
```

## 🎯 **Geração de Anagramas (COM AUTENTICAÇÃO)**

### 7. **Gerar Anagramas Básicos**

```bash
# cURL - Palavra simples
curl -X POST "http://localhost:8080/api/anagrams/generate" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d "{\"letters\": \"abc\"}"

# PowerShell equivalente
$body = @{ letters = "abc" } | ConvertTo-Json
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri "http://localhost:8080/api/anagrams/generate" -Method Post -Body $body -ContentType "application/json" -Headers $headers
```

**Resposta esperada:**
```json
{
  "originalLetters": "abc",
  "anagrams": ["abc", "acb", "bac", "bca", "cab", "cba"],
  "totalAnagrams": 6,
  "fromCache": false,
  "processingTimeMs": 15
}
```

### 8. **Testar Cache (Primeira Chamada)**

```bash
# cURL
curl -X POST "http://localhost:8080/api/anagrams/generate" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d "{\"letters\": \"test\"}"

# PowerShell equivalente
$body = @{ letters = "test" } | ConvertTo-Json
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri "http://localhost:8080/api/anagrams/generate" -Method Post -Body $body -ContentType "application/json" -Headers $headers
```

**Resposta esperada (cache miss):**
```json
{
  "originalLetters": "test",
  "anagrams": ["test", "tets", "tset", "tste", "ttes", "ttse", "estt", "etst", "etts", "sett", "stet", "stte"],
  "totalAnagrams": 12,
  "fromCache": false,
  "processingTimeMs": 25
}
```

### 9. **Testar Cache (Segunda Chamada)**

```bash
# cURL
curl -X POST "http://localhost:8080/api/anagrams/generate" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d "{\"letters\": \"test\"}"

# PowerShell equivalente
$body = @{ letters = "test" } | ConvertTo-Json
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri "http://localhost:8080/api/anagrams/generate" -Method Post -Body $body -ContentType "application/json" -Headers $headers
```

**Resposta esperada (cache hit):**
```json
{
  "originalLetters": "test",
  "anagrams": ["test", "tets", "tset", "tste", "ttes", "ttse", "estt", "etst", "etts", "sett", "stet", "stte"],
  "totalAnagrams": 12,
  "fromCache": true,
  "processingTimeMs": 2
}
```

### 10. **Geração sem Cache**

```bash
# cURL
curl -X POST "http://localhost:8080/api/anagrams/generate-no-cache" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d "{\"letters\": \"xyz\"}"

# PowerShell equivalente
$body = @{ letters = "xyz" } | ConvertTo-Json
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri "http://localhost:8080/api/anagrams/generate-no-cache" -Method Post -Body $body -ContentType "application/json" -Headers $headers
```

## 🧮 **Cálculo de Total de Anagramas (COM AUTENTICAÇÃO)**

### 11. **Cálculo para "abc"**

```bash
# cURL
curl -X GET "http://localhost:8080/api/anagrams/calculate-total/abc" \
  -H "Authorization: Bearer $token"

# PowerShell equivalente
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri "http://localhost:8080/api/anagrams/calculate-total/abc" -Method Get -Headers $headers
```

**Resposta esperada:**
```json
{
  "letters": "abc",
  "totalAnagrams": 6,
  "message": "Total de anagramas calculado com sucesso"
}
```

### 12. **Cálculo para "abcd"**

```bash
# cURL
curl -X GET "http://localhost:8080/api/anagrams/calculate-total/abcd" \
  -H "Authorization: Bearer $token"

# PowerShell equivalente
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri "http://localhost:8080/api/anagrams/calculate-total/abcd" -Method Get -Headers $headers
```

## 🗄️ **Gerenciamento de Cache (APENAS ADMIN)**

### 13. **Limpar Cache Específico**

```bash
# cURL
curl -X DELETE "http://localhost:8080/api/anagrams/cache/abc" \
  -H "Authorization: Bearer $token"

# PowerShell equivalente
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri "http://localhost:8080/api/anagrams/cache/abc" -Method Delete -Headers $headers
```

**Resposta esperada:**
```json
{
  "message": "Anagramas removidos do cache com sucesso",
  "letters": "abc"
}
```

### 14. **Limpar Todo o Cache**

```bash
# cURL
curl -X DELETE "http://localhost:8080/api/anagrams/cache" \
  -H "Authorization: Bearer $token"

# PowerShell equivalente
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri "http://localhost:8080/api/anagrams/cache" -Method Delete -Headers $headers
```

**Resposta esperada:**
```json
{
  "message": "Cache de anagramas limpo com sucesso"
}
```

### 15. **Teste de Acesso Negado (Usuário USER)**

```bash
# cURL
curl -X DELETE "http://localhost:8080/api/anagrams/cache" \
  -H "Authorization: Bearer $userToken"

# PowerShell equivalente
$headers = @{ Authorization = "Bearer $userToken" }
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/anagrams/cache" -Method Delete -Headers $headers
} catch {
    Write-Host "Acesso negado (esperado para usuário USER): $($_.Exception.Message)"
}
```

**Resposta esperada:** `401 Unauthorized` (acesso negado)

## 🚫 **Teste de Acesso Negado sem Autenticação**

### 16. **Tentar Acessar Endpoint Protegido sem Token**

```bash
# cURL
curl -X POST "http://localhost:8080/api/anagrams/generate" \
  -H "Content-Type: application/json" \
  -d "{\"letters\": \"abc\"}"

# PowerShell equivalente
$body = @{ letters = "abc" } | ConvertTo-Json
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/anagrams/generate" -Method Post -Body $body -ContentType "application/json"
} catch {
    Write-Host "Acesso negado (esperado): $($_.Exception.Message)"
}
```

**Resposta esperada:** `401 Unauthorized`

### 17. **Tentar Acessar com Token Inválido**

```bash
# cURL
curl -X POST "http://localhost:8080/api/anagrams/generate" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer invalid-token" \
  -d "{\"letters\": \"abc\"}"

# PowerShell equivalente
$body = @{ letters = "abc" } | ConvertTo-Json
$headers = @{ Authorization = "Bearer invalid-token" }
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/anagrams/generate" -Method Post -Body $body -ContentType "application/json" -Headers $headers
} catch {
    Write-Host "Token inválido (esperado): $($_.Exception.Message)"
}
```

## 📝 **Registro de Novos Usuários**

### 18. **Registrar Novo Usuário**

```bash
# cURL
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"newuser\", \"password\": \"password123\", \"email\": \"newuser@example.com\", \"fullName\": \"Novo Usuário\", \"role\": \"USER\"}"

# PowerShell equivalente
$newUser = @{
    username = "newuser"
    password = "password123"
    email = "newuser@example.com"
    fullName = "Novo Usuário"
    role = "USER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -Body $newUser -ContentType "application/json"
```

**Resposta esperada:**
```json
{
  "id": 3,
  "username": "newuser",
  "email": "newuser@example.com",
  "fullName": "Novo Usuário",
  "isActive": true,
  "role": "USER"
}
```

### 19. **Testar Login com Novo Usuário**

```bash
# cURL
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"newuser\", \"password\": \"password123\"}"

# PowerShell equivalente
$loginData = @{ username = "newuser"; password = "password123" } | ConvertTo-Json
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $loginData -ContentType "application/json"
$newUserToken = $response.token
Write-Host "Novo usuário token: $newUserToken"
```

## 🔄 **Cache Inteligente (COM AUTENTICAÇÃO)**

### 20. **Teste de Cache Inteligente**

```bash
# Primeiro, gerar anagramas para "test"
curl -X POST "http://localhost:8080/api/anagrams/generate" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d "{\"letters\": \"test\"}"

# Agora testar "tets" (mesma composição, ordem diferente)
curl -X POST "http://localhost:8080/api/anagrams/generate" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d "{\"letters\": \"tets\"}"
```

**Resposta esperada para "tets":**
```json
{
  "originalLetters": "tets",
  "anagrams": ["test", "tets", "tset", "tste", "ttes", "ttse", "estt", "etst", "etts", "sett", "stet", "stte"],
  "totalAnagrams": 12,
  "fromCache": true,
  "processingTimeMs": 1
}
```

## ⚡ **Teste de Performance (COM AUTENTICAÇÃO)**

### 21. **Medir Tempo de Resposta**

```bash
# cURL com medida de tempo
Measure-Command {
    curl -X POST "http://localhost:8080/api/anagrams/generate" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $token" \
      -d "{\"letters\": \"hello\"}" > $null
}

# PowerShell equivalente
$stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
$body = @{ letters = "hello" } | ConvertTo-Json
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri "http://localhost:8080/api/anagrams/generate" -Method Post -Body $body -ContentType "application/json" -Headers $headers > $null
$stopwatch.Stop()
Write-Host "Tempo de resposta: $($stopwatch.ElapsedMilliseconds)ms"
```

## 🧪 **Validação de Entrada (COM AUTENTICAÇÃO)**

### 22. **Teste com Entrada Vazia**

```bash
# cURL
curl -X POST "http://localhost:8080/api/anagrams/generate" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d "{\"letters\": \"\"}"

# PowerShell equivalente
$body = @{ letters = "" } | ConvertTo-Json
$headers = @{ Authorization = "Bearer $token" }
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/anagrams/generate" -Method Post -Body $body -ContentType "application/json" -Headers $headers
} catch {
    Write-Host "Validação funcionou (esperado): $($_.Exception.Message)"
}
```

**Resposta esperada:** `400 Bad Request`

### 23. **Teste com Números**

```bash
# cURL
curl -X POST "http://localhost:8080/api/anagrams/generate" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d "{\"letters\": \"123\"}"

# PowerShell equivalente
$body = @{ letters = "123" } | ConvertTo-Json
$headers = @{ Authorization = "Bearer $token" }
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/anagrams/generate" -Method Post -Body $body -ContentType "application/json" -Headers $headers
} catch {
    Write-Host "Validação funcionou (esperado): $($_.Exception.Message)"
}
```

## 🔍 **Casos de Borda (COM AUTENTICAÇÃO)**

### 24. **Teste com Uma Letra**

```bash
# cURL
curl -X POST "http://localhost:8080/api/anagrams/generate" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d "{\"letters\": \"a\"}"

# PowerShell equivalente
$body = @{ letters = "a" } | ConvertTo-Json
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri "http://localhost:8080/api/anagrams/generate" -Method Post -Body $body -ContentType "application/json" -Headers $headers
```

### 25. **Teste com Letras Repetidas**

```bash
# cURL
curl -X POST "http://localhost:8080/api/anagrams/generate" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d "{\"letters\": \"aab\"}"

# PowerShell equivalente
$body = @{ letters = "aab" } | ConvertTo-Json
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri "http://localhost:8080/api/anagrams/generate" -Method Post -Body $body -ContentType "application/json" -Headers $headers
```

## 📋 **Script de Teste Completo para PowerShell**

### 26. **Script Automatizado**

```powershell
# Script de teste completo
Write-Host "🧪 TESTE COMPLETO DA API COM JWT" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan

# 1. Login
Write-Host "1. Fazendo login..." -ForegroundColor Yellow
$loginData = @{ username = "admin"; password = "admin123" } | ConvertTo-Json
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $loginData -ContentType "application/json"
$token = $response.token

if ($token) {
    Write-Host "✅ Login realizado com sucesso" -ForegroundColor Green
    Write-Host "Token: $($token.Substring(0, [Math]::Min(20, $token.Length)))..." -ForegroundColor Yellow
} else {
    Write-Host "❌ Falha no login" -ForegroundColor Red
    exit 1
}

# 2. Health Check
Write-Host "2. Verificando saúde da API..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/api/anagrams/health" -Method Get
    if ($health.status -eq "UP") {
        Write-Host "✅ API saudável" -ForegroundColor Green
    } else {
        Write-Host "❌ API não saudável" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Falha no health check: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. Geração de Anagramas
Write-Host "3. Testando geração de anagramas..." -ForegroundColor Yellow
try {
    $body = @{ letters = "abc" } | ConvertTo-Json
    $headers = @{ Authorization = "Bearer $token" }
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/anagrams/generate" -Method Post -Body $body -ContentType "application/json" -Headers $headers
    
    if ($response.totalAnagrams -eq 6) {
        Write-Host "✅ Geração OK" -ForegroundColor Green
    } else {
        Write-Host "❌ Geração falhou" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Falha na geração: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. Cache
Write-Host "4. Testando cache..." -ForegroundColor Yellow
try {
    $body = @{ letters = "abc" } | ConvertTo-Json
    $headers = @{ Authorization = "Bearer $token" }
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/anagrams/generate" -Method Post -Body $body -ContentType "application/json" -Headers $headers
    
    if ($response.fromCache -eq $true) {
        Write-Host "✅ Cache funcionando" -ForegroundColor Green
    } else {
        Write-Host "❌ Cache falhou" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Falha no cache: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "🎉 Teste completo finalizado!" -ForegroundColor Green
```

### 27. **Executar Script**

```powershell
# Salvar como test-complete.ps1 e executar
.\test-complete.ps1
```

## 💡 **Dicas de Uso no Windows**

### 1. **Sempre Incluir o Header de Autorização**
```powershell
$headers = @{ Authorization = "Bearer $token" }
```

### 2. **Verificar Respostas de Erro**
- `401 Unauthorized`: Token inválido, expirado ou endpoint protegido sem autenticação
- `403 Forbidden`: Usuário autenticado mas sem permissão para o endpoint (não aplicável na configuração atual)
- `400 Bad Request`: Dados de entrada inválidos

### 3. **Renovar Token quando Necessário**
```powershell
# Se receber 401, fazer novo login
$loginData = @{ username = "admin"; password = "admin123" } | ConvertTo-Json
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $loginData -ContentType "application/json"
$token = $response.token
```

### 4. **Usar Variáveis para Tokens Diferentes**
```powershell
# Token de admin para operações de cache
$adminToken = "..."

# Token de user para operações básicas
$userToken = "..."
```

## 🚨 **Troubleshooting no Windows**

### Problema: Token Expirado
**Sintoma:** `401 Unauthorized`
**Solução:** Fazer novo login para obter novo token

### Problema: Acesso Negado
**Sintoma:** `401 Unauthorized`
**Solução:** Fazer login para obter um token JWT válido

### Problema: Aplicação Não Responde
**Sintoma:** `Connection refused`
**Solução:** Verificar se aplicação está rodando em `localhost:8080`

### Problema: Redis Não Disponível
**Sintoma:** Erros de cache
**Solução:** Aplicação usa fallback automático para cache em memória

### Problema: PowerShell Execution Policy
**Sintoma:** Script não executa
**Solução:** Executar `Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser`

## 📚 **Recursos Adicionais**

- **README.md**: Documentação completa do projeto
- **test-api.ps1**: Script de teste automatizado para Windows
- **test-api.sh**: Script de teste automatizado para Linux/Mac
- **Console H2**: `http://localhost:8080/h2-console` para visualizar banco de dados

---

## 🎯 **Resumo dos Testes**

1. **✅ Autenticação JWT**: Login e validação de tokens
2. **✅ Controle de Acesso**: Verificação de roles e permissões
3. **✅ Funcionalidade Básica**: Geração de anagramas com autenticação
4. **✅ Cache Redis**: Verificação de hit/miss com autenticação
5. **✅ Validações**: Entrada inválida e casos de borda
6. **✅ Performance**: Tempo de resposta e otimizações
7. **✅ Gerenciamento**: Cache e status da aplicação
8. **✅ Casos de Borda**: Letras únicas, repetidas, vazias

**💡 Dica**: Execute os testes em sequência para verificar o funcionamento completo da API com segurança JWT!

**🎯 A API está pronta para uso com segurança JWT no Windows!** 🔐✨
