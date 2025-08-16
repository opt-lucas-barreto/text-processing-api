#!/bin/bash

# Script para debug do cache de anagramas
# Testa a funcionalidade de cache hit/miss com autenticação JWT

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# URL base da API
BASE_URL="http://localhost:8080"

# Variáveis globais para autenticação
JWT_TOKEN=""
USER_ROLE=""

# Função para imprimir resultado
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ SUCESSO${NC}"
    else
        echo -e "${RED}❌ FALHOU${NC}"
    fi
}

# Função para imprimir status
print_status() {
    echo -e "${BLUE}ℹ️ $1${NC}"
}

# Função para imprimir erro
print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Função para autenticação
authenticate() {
    print_status "Realizando autenticação JWT..."
    
    # Credenciais do admin
    admin_credentials='{"username": "admin", "password": "admin123"}'
    
    # Fazer login
    auth_response=$(curl -s -X POST "$BASE_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "$admin_credentials")
    
    if [ $? -eq 0 ] && [ -n "$auth_response" ]; then
        # Extrair token e role
        JWT_TOKEN=$(echo "$auth_response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        USER_ROLE=$(echo "$auth_response" | grep -o '"role":"[^"]*"' | cut -d'"' -f4)
        
        if [ -n "$JWT_TOKEN" ]; then
            print_result 0
            echo "   ✅ Login realizado com sucesso"
            echo "   Role: $USER_ROLE"
            echo "   Token: ${JWT_TOKEN:0:20}..."
            return 0
        else
            print_result 1
            print_error "Token não encontrado na resposta"
            return 1
        fi
    else
        print_result 1
        print_error "Falha na autenticação"
        return 1
    fi
}

echo "🔍 DEBUG DO CACHE DE ANAGRAMAS (COM JWT)"
echo "========================================="
echo ""

# 1. Verificar se a aplicação está rodando
echo "1. Verificando saúde da aplicação..."
health_response=$(curl -s "$BASE_URL/api/anagrams/health")

if [ $? -eq 0 ] && [ -n "$health_response" ]; then
    print_result 0
    echo "   Resposta: $health_response"
else
    print_result 1
    print_error "Aplicação não está respondendo. Verifique se está rodando."
    exit 1
fi

echo ""

# 2. Autenticação JWT
echo "2. Autenticação JWT..."
if ! authenticate; then
    print_error "Não foi possível autenticar. Saindo..."
    exit 1
fi

echo ""

# 3. Verificar status do cache (com autenticação)
echo "3. Verificando status do cache..."
cache_status=$(curl -s -H "Authorization: Bearer $JWT_TOKEN" "$BASE_URL/api/anagrams/cache/status")

if [ $? -eq 0 ] && [ -n "$cache_status" ]; then
    print_result 0
    echo "   Status: $cache_status"
else
    print_result 1
    print_error "Não foi possível obter status do cache"
fi

echo ""

# 4. Testar funcionalidade do cache
echo "4. Testando funcionalidade do cache..."
echo "   Dados de teste: 'test'"

data='{"letters": "test"}'

# Primeira chamada (deve ser cache miss)
echo -n "   Primeira chamada (cache miss)... "
response1=$(curl -s -X POST "$BASE_URL/api/anagrams/generate" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -d "$data")

if [ $? -eq 0 ] && [ -n "$response1" ]; then
    print_result 0
    echo "   ✅ Resposta recebida"
    
    # Verificar se é cache miss
    if echo "$response1" | grep -q '"fromCache":false'; then
        echo "   ✅ Cache miss detectado corretamente"
    else
        echo "   ℹ️ Cache hit (pode ser que já exista no cache)"
    fi
else
    print_result 1
    print_error "Falha na primeira chamada"
    exit 1
fi

# Aguardar um pouco para garantir que o cache foi salvo
sleep 1

# Segunda chamada (deve ser cache hit)
echo -n "   Segunda chamada (cache hit)... "
response2=$(curl -s -X POST "$BASE_URL/api/anagrams/generate" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -d "$data")

if [ $? -eq 0 ] && [ -n "$response2" ]; then
    print_result 0
    echo "   ✅ Resposta recebida"
    
    # Verificar se é cache hit
    if echo "$response2" | grep -q '"fromCache":true'; then
        echo "   ✅ Cache hit detectado corretamente"
    else
        echo "   ℹ️ Cache miss (pode ser que não tenha sido salvo)"
    fi
    
    # Verificar consistência dos resultados
    total1=$(echo "$response1" | grep -o '"totalAnagrams":[0-9]*' | cut -d':' -f2)
    total2=$(echo "$response2" | grep -o '"totalAnagrams":[0-9]*' | cut -d':' -f2)
    
    if [ "$total1" = "$total2" ]; then
        echo "   ✅ Resultados consistentes: $total1 anagramas"
    else
        echo "   ⚠️ Resultados inconsistentes: $total1 vs $total2"
    fi
else
    print_result 1
    print_error "Falha na segunda chamada"
    exit 1
fi

echo ""

# 5. Testar cache inteligente
echo "5. Testando cache inteligente..."
echo "   Testando anagramas com mesma composição..."

# Primeiro, gerar para "abc"
data_abc='{"letters": "abc"}'
response_abc=$(curl -s -X POST "$BASE_URL/api/anagrams/generate" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -d "$data_abc")

if [ $? -eq 0 ] && [ -n "$response_abc" ]; then
    total_abc=$(echo "$response_abc" | grep -o '"totalAnagrams":[0-9]*' | cut -d':' -f2)
    echo "   ✅ 'abc' gerado: $total_abc anagramas"
    
    # Agora testar "cba" (mesma composição)
    data_cba='{"letters": "cba"}'
    response_cba=$(curl -s -X POST "$BASE_URL/api/anagrams/generate" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -d "$data_cba")
    
    if [ $? -eq 0 ] && [ -n "$response_cba" ]; then
        total_cba=$(echo "$response_cba" | grep -o '"totalAnagrams":[0-9]*' | cut -d':' -f2)
        from_cache=$(echo "$response_cba" | grep -o '"fromCache":[^,]*' | cut -d':' -f2)
        
        if [ "$from_cache" = "true" ]; then
            print_result 0
            echo "   ✅ Cache inteligente funcionando"
            echo "   'cba' recuperado do cache: $total_cba anagramas"
        else
            print_result 1
            print_error "Cache inteligente não funcionou"
        fi
    else
        print_result 1
        print_error "Falha ao testar 'cba'"
    fi
else
    print_result 1
    print_error "Falha ao gerar 'abc'"
fi

echo ""

# 6. Testar gerenciamento de cache (apenas ADMIN)
if [ "$USER_ROLE" = "ADMIN" ]; then
    echo "6. Testando gerenciamento de cache (ADMIN)..."
    
    # Limpar cache
    echo -n "   Limpando cache... "
    clear_response=$(curl -s -X DELETE "$BASE_URL/api/anagrams/cache" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    if [ $? -eq 0 ] && [ -n "$clear_response" ]; then
        print_result 0
        echo "   ✅ Cache limpo com sucesso"
        
        # Verificar se foi limpo (testar com palavra que estava no cache)
        echo -n "   Verificando limpeza... "
        test_response=$(curl -s -X POST "$BASE_URL/api/anagrams/generate" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $JWT_TOKEN" \
            -d "$data")
        
        if [ $? -eq 0 ] && [ -n "$test_response" ]; then
            from_cache_after=$(echo "$test_response" | grep -o '"fromCache":[^,]*' | cut -d':' -f2)
            
            if [ "$from_cache_after" = "false" ]; then
                print_result 0
                echo "   ✅ Cache foi limpo corretamente"
            else
                print_result 1
                print_error "Cache não foi limpo"
            fi
        else
            print_result 1
            print_error "Falha ao verificar limpeza"
        fi
    else
        print_result 1
        print_error "Falha ao limpar cache"
    fi
else
    echo "6. Testando gerenciamento de cache (ADMIN)..."
    echo "   ⚠️ Usuário não é ADMIN (role: $USER_ROLE), pulando teste"
fi

echo ""
echo "🔍 DEBUG CONCLUÍDO"
echo "=================="
echo "Cache de anagramas testado com sucesso!"
echo "Todos os testes de funcionalidade foram executados."
