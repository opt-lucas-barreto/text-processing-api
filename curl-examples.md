# 🧪 Exemplos de Teste com cURL - Text Processing API (COM JWT)

Este arquivo contém exemplos de comandos cURL para testar todos os endpoints da API de anagramas com autenticação JWT.

## 📋 Pré-requisitos

- Aplicação rodando em `http://localhost:8080`
- cURL instalado no sistema
- Terminal/Command Prompt aberto

## 🔐 Teste 0: Autenticação JWT

### Login para Obter Token
```bash
# Login como admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

**Resposta Esperada:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin",
  "role": "ADMIN",
  "message": "Autenticação realizada com sucesso"
}
```

### Login como Usuário Normal
```bash
# Login como user
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "user123"}'
```

### Extrair Token (Linux/Mac)
```bash
# Salvar token em variável
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}' | \
  grep -o '"token":"[^"]*"' | cut -d'"' -f4)

echo "Token: $TOKEN"
```

### Extrair Token (Windows PowerShell)
```powershell
# Salvar token em variável
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body '{"username": "admin", "password": "admin123"}' -ContentType "application/json"
$TOKEN = $response.token
Write-Host "Token: $TOKEN"
```

### Validar Token
```bash
curl -X GET http://localhost:8080/api/auth/validate \
  -H "Authorization: Bearer $TOKEN"
```

## 🎯 Teste 1: Geração de Anagramas (COM AUTENTICAÇÃO)

### Geração Básica com Cache
```bash
curl -X POST http://localhost:8080/api/anagrams/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"letters": "abc"}'
```

**Resposta Esperada:**
```json
{
  "originalLetters": "abc",
  "anagrams": ["abc", "acb", "bac", "bca", "cab", "cba"],
  "totalAnagrams": 6,
  "fromCache": false,
  "processingTimeMs": 5
}
```

### Geração sem Cache
```bash
curl -X POST http://localhost:8080/api/anagrams/generate-no-cache \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"letters": "abc"}'
```

### Teste com Letras Maiúsculas
```bash
curl -X POST http://localhost:8080/api/anagrams/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"letters": "ABC"}'
```

### Teste com Palavra Maior
```bash
curl -X POST http://localhost:8080/api/anagrams/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"letters": "abcd"}'
```

## 🧮 Teste 2: Cálculo de Total de Anagramas (COM AUTENTICAÇÃO)

### Cálculo para "abc"
```bash
curl -X GET http://localhost:8080/api/anagrams/calculate-total/abc \
  -H "Authorization: Bearer $TOKEN"
```

**Resposta Esperada:**
```json
{
  "letters": "abc",
  "totalAnagrams": 6,
  "message": "Total de anagramas calculado com sucesso"
}
```

### Cálculo para "abcd"
```bash
curl -X GET http://localhost:8080/api/anagrams/calculate-total/abcd \
  -H "Authorization: Bearer $TOKEN"
```

### Cálculo para "a"
```bash
curl -X GET http://localhost:8080/api/anagrams/calculate-total/a \
  -H "Authorization: Bearer $TOKEN"
```

## 📊 Teste 3: Status e Informações

### Health Check (SEM AUTENTICAÇÃO - PÚBLICO)
```bash
curl http://localhost:8080/api/anagrams/health
```

**Resposta Esperada:**
```json
{
  "status": "UP",
  "message": "Text Processing API está funcionando",
  "timestamp": "2024-08-16T00:24:25.612"
}
```

### Health Check da Autenticação (SEM AUTENTICAÇÃO - PÚBLICO)
```bash
curl http://localhost:8080/api/auth/health
```

**Resposta Esperada:**
```json
{
  "status": "UP",
  "message": "Serviço de autenticação está funcionando",
  "timestamp": "2024-08-16T00:24:25.612"
}
```

### Status do Cache (COM AUTENTICAÇÃO)
```bash
curl -X GET http://localhost:8080/api/anagrams/cache/status \
  -H "Authorization: Bearer $TOKEN"
```

**Resposta Esperada:**
```json
{
  "cacheEnabled": true,
  "message": "Status do cache recuperado com sucesso"
}
```

## 🗄️ Teste 4: Gerenciamento de Cache (APENAS ADMIN)

### Verificar se Usuário é ADMIN
```bash
# Primeiro, fazer login como admin
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}' | \
  grep -o '"token":"[^"]*"' | cut -d'"' -f4)

echo "Admin Token: $ADMIN_TOKEN"
```

### Limpar Cache Específico
```bash
curl -X DELETE http://localhost:8080/api/anagrams/cache/abc \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Resposta Esperada:**
```json
{
  "message": "Anagramas removidos do cache com sucesso",
  "letters": "abc"
}
```

### Limpar Todo o Cache
```bash
curl -X DELETE http://localhost:8080/api/anagrams/cache \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Resposta Esperada:**
```json
{
  "message": "Cache de anagramas limpo com sucesso"
}
```

### Teste de Acesso Negado (Usuário USER tentando limpar cache)
```bash
# Fazer login como user
USER_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "user123"}' | \
  grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# Tentar limpar cache (deve retornar 401 Unauthorized)
curl -X DELETE http://localhost:8080/api/anagrams/cache \
  -H "Authorization: Bearer $USER_TOKEN"
```

## 🚫 Teste 5: Acesso Negado sem Autenticação

### Tentar Acessar Endpoint Protegido sem Token
```bash
curl -X POST http://localhost:8080/api/anagrams/generate \
  -H "Content-Type: application/json" \
  -d '{"letters": "abc"}'
```

**Resposta Esperada:**
```json
{
  "timestamp": "2024-08-16T00:24:25.612",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/anagrams/generate"
}
```

### Tentar Acessar Endpoint Protegido com Token Inválido
```bash
curl -X POST http://localhost:8080/api/anagrams/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer invalid-token" \
  -d '{"letters": "abc"}'
```

## 🔄 Teste 6: Cache Inteligente (COM AUTENTICAÇÃO)

### Primeira Chamada - Cache Miss
```bash
curl -X POST http://localhost:8080/api/anagrams/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"letters": "test"}'
```

**Resposta Esperada:**
```json
{
  "originalLetters": "test",
  "anagrams": ["test", "tets", "tset", "tste", "ttes", "ttse", "estt", "etst", "etts", "sett", "stet", "stte"],
  "totalAnagrams": 12,
  "fromCache": false,
  "processingTimeMs": 25
}
```

### Segunda Chamada - Cache Hit
```bash
curl -X POST http://localhost:8080/api/anagrams/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"letters": "test"}'
```

**Resposta Esperada:**
```json
{
  "originalLetters": "test",
  "anagrams": ["test", "tets", "tset", "tste", "ttes", "ttse", "estt", "etst", "etts", "sett", "stet", "stte"],
  "totalAnagrams": 12,
  "fromCache": true,
  "processingTimeMs": 2
}
```

### Teste de Cache Inteligente - Mesma Composição
```bash
# "tets" tem a mesma composição de letras que "test"
curl -X POST http://localhost:8080/api/anagrams/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"letters": "tets"}'
```

**Resposta Esperada:**
```json
{
  "originalLetters": "tets",
  "anagrams": ["test", "tets", "tset", "tste", "ttes", "ttse", "estt", "etst", "etts", "sett", "stet", "stte"],
  "totalAnagrams": 12,
  "fromCache": true,
  "processingTimeMs": 1
}
```

## 📝 Teste 7: Registro de Novos Usuários

### Registrar Novo Usuário
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "password123",
    "email": "newuser@example.com",
    "fullName": "Novo Usuário",
    "role": "USER"
  }'
```

**Resposta Esperada:**
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

### Testar Login com Novo Usuário
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "newuser", "password": "password123"}'
```

## ⚡ Teste 8: Performance (COM AUTENTICAÇÃO)

### Medir Tempo de Resposta
```bash
# Teste de performance
time curl -X POST http://localhost:8080/api/anagrams/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"letters": "hello"}' > /dev/null
```

### Comparar Performance com e sem Cache
```bash
# Primeira chamada (sem cache)
echo "Primeira chamada (sem cache):"
time curl -X POST http://localhost:8080/api/anagrams/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"letters": "hello"}' > /dev/null

# Segunda chamada (com cache)
echo "Segunda chamada (com cache):"
time curl -X POST http://localhost:8080/api/anagrams/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"letters": "hello"}' > /dev/null
```

## 🧪 Teste 9: Validação de Entrada (COM AUTENTICAÇÃO)

### Teste com Entrada Vazia
```bash
curl -X POST http://localhost:8080/api/anagrams/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"letters": ""}'
```

**Resposta Esperada:**
```json
{
  "timestamp": "2024-08-16T00:24:25.612",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for argument [0] in public org.springframework.http.ResponseEntity<com.lucas.text_processing_api.dto.AnagramResponse> com.lucas.text_processing_api.controller.AnagramController.generateAnagrams(com.lucas.text_processing_api.dto.AnagramRequest): [Field error in object 'anagramRequest' on field 'letters'; rejected value []; codes [NotBlank.anagramRequest.letters,NotBlank.letters,NotBlank.java.lang.String,NotBlank]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [anagramRequest.letters,letters]; arguments []; default message [letters]]; default message [Username é obrigatório]]",
  "path": "/api/anagrams/generate"
}
```

### Teste com Números
```bash
curl -X POST http://localhost:8080/api/anagrams/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"letters": "123"}'
```

### Teste com Caracteres Especiais
```bash
curl -X POST http://localhost:8080/api/anagrams/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"letters": "a@b"}'
```

## 🔍 Teste 10: Casos de Borda

### Teste com Uma Letra
```bash
curl -X POST http://localhost:8080/api/anagrams/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"letters": "a"}'
```

### Teste com Letras Repetidas
```bash
curl -X POST http://localhost:8080/api/anagrams/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"letters": "aab"}'
```

### Teste com Palavra Longa (Pode ser lento)
```bash
curl -X POST http://localhost:8080/api/anagrams/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"letters": "abcdef"}'
```

## 📋 Script de Teste Completo

### Script Bash para Linux/Mac
```bash
#!/bin/bash

echo "🧪 TESTE COMPLETO DA API COM JWT"
echo "================================="

# 1. Login
echo "1. Fazendo login..."
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}' | \
  grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "❌ Falha no login"
    exit 1
fi

echo "✅ Login realizado com sucesso"
echo "Token: ${TOKEN:0:20}..."

# 2. Health Check
echo "2. Verificando saúde da API..."
curl -s http://localhost:8080/api/anagrams/health | grep -q '"status":"UP"' && echo "✅ API saudável" || echo "❌ API não saudável"

# 3. Geração de Anagramas
echo "3. Testando geração de anagramas..."
RESPONSE=$(curl -s -X POST http://localhost:8080/api/anagrams/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"letters": "abc"}')

echo "$RESPONSE" | grep -q '"totalAnagrams":6' && echo "✅ Geração OK" || echo "❌ Geração falhou"

# 4. Cache
echo "4. Testando cache..."
curl -s -X POST http://localhost:8080/api/anagrams/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"letters": "abc"}' | grep -q '"fromCache":true' && echo "✅ Cache funcionando" || echo "❌ Cache falhou"

echo "🎉 Teste completo finalizado!"
```

### Executar Script
```bash
chmod +x test-complete.sh
./test-complete.sh
```

## 💡 Dicas de Uso

### 1. **Sempre Incluir o Header de Autorização**
```bash
-H "Authorization: Bearer $TOKEN"
```

### 2. **Verificar Respostas de Erro**
- `401 Unauthorized`: Token inválido, expirado ou endpoint protegido sem autenticação
- `403 Forbidden`: Usuário autenticado mas sem permissão para o endpoint (não aplicável na configuração atual)
- `400 Bad Request`: Dados de entrada inválidos

### 3. **Renovar Token quando Necessário**
```bash
# Se receber 401, fazer novo login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}' | \
  grep -o '"token":"[^"]*"' | cut -d'"' -f4)
```

### 4. **Usar Variáveis para Tokens Diferentes**
```bash
# Token de admin para operações de cache
ADMIN_TOKEN="..."

# Token de user para operações básicas
USER_TOKEN="..."
```

## 🚨 Troubleshooting

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

---

## 📚 Recursos Adicionais

- **README.md**: Documentação completa do projeto
- **test-api.sh**: Script de teste automatizado para Linux/Mac
- **test-api.ps1**: Script de teste automatizado para Windows
- **Console H2**: `http://localhost:8080/h2-console` para visualizar banco de dados

**🎯 A API está pronta para uso com segurança JWT!** 🔐✨
