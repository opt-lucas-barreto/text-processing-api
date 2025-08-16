#!/bin/bash

# 🧪 Script de Teste Automatizado - Text Processing API
# Este script testa todos os endpoints da API de anagramas com autenticação JWT

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

# Função para imprimir cabeçalho
print_header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  🧪 TESTE DA API DE ANAGRAMAS (JWT)  ${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
}

# Função para imprimir seção
print_section() {
    echo -e "${YELLOW}$1${NC}"
    echo "----------------------------------------"
}

# Função para imprimir resultado
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ SUCESSO${NC}"
    else
        echo -e "${RED}❌ FALHOU${NC}"
    fi
}

# Função para testar endpoint
test_endpoint() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    local auth_header=$5
    
    echo -n "Testando: $description... "
    
    local curl_cmd="curl -s -w \"%{http_code}\" -X $method \"$BASE_URL$endpoint\""
    
    if [ ! -z "$data" ]; then
        curl_cmd="$curl_cmd -H \"Content-Type: application/json\" -d \"$data\""
    fi
    
    if [ ! -z "$auth_header" ]; then
        curl_cmd="$curl_cmd -H \"Authorization: Bearer $auth_header\""
    fi
    
    response=$(eval $curl_cmd)
    
    http_code="${response: -3}"
    body="${response%???}"
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        print_result 0
        echo "   Resposta: $body"
    else
        print_result 1
        echo "   HTTP Code: $http_code"
        echo "   Resposta: $body"
    fi
    echo ""
}

# Função para testar autenticação
test_authentication() {
    echo -e "${YELLOW}Testando Autenticação JWT${NC}"
    echo "----------------------------------------"
    
    local login_data="{\"username\": \"admin\", \"password\": \"admin123\"}"
    
    echo -n "Testando login admin... "
    
    response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "$login_data")
    
    http_code="${response: -3}"
    body="${response%???}"
    
    if [ "$http_code" -eq 200 ]; then
        # Extrair token da resposta JSON (simples)
        JWT_TOKEN=$(echo "$body" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        USER_ROLE=$(echo "$body" | grep -o '"role":"[^"]*"' | cut -d'"' -f4)
        
        if [ ! -z "$JWT_TOKEN" ]; then
            print_result 0
            echo "   ✅ Login realizado com sucesso"
            echo "   Role: $USER_ROLE"
            echo "   Token: ${JWT_TOKEN:0:20}..."
        else
            print_result 1
            echo "   ❌ Token não encontrado na resposta"
            return 1
        fi
    else
        print_result 1
        echo "   ❌ Falha no login (HTTP Code: $http_code)"
        echo "   Resposta: $body"
        return 1
    fi
    
    echo ""
    return 0
}

# Função para testar acesso negado sem autenticação
test_unauthorized_access() {
    echo -e "${YELLOW}Testando Acesso Negado sem Autenticação${NC}"
    echo "----------------------------------------"
    
    local data="{\"letters\": \"abc\"}"
    
    echo -n "Testando acesso sem token... "
    
    response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/api/anagrams/generate" \
        -H "Content-Type: application/json" \
        -d "$data")
    
    http_code="${response: -3}"
    body="${response%???}"
    
    if [ "$http_code" -eq 401 ]; then
        print_result 0
        echo "   ✅ Acesso negado corretamente (401 Unauthorized)"
    else
        print_result 1
        echo "   ❌ Acesso deveria ter sido negado (HTTP Code: $http_code)"
    fi
    
    echo ""
}

# Função para testar validação de token
test_token_validation() {
    echo -e "${YELLOW}Testando Validação de Token JWT${NC}"
    echo "----------------------------------------"
    
    if [ -z "$JWT_TOKEN" ]; then
        echo "❌ Token JWT não disponível"
        return 1
    fi
    
    test_endpoint "GET" "/api/auth/validate" "" "Validação de token" "$JWT_TOKEN"
}

# Função para testar validação (deve falhar)
test_validation() {
    local data=$1
    local description=$2
    
    echo -n "Testando validação: $description... "
    
    if [ -z "$JWT_TOKEN" ]; then
        echo "❌ Token JWT não disponível"
        return 1
    fi
    
    response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/api/anagrams/generate" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -d "$data")
    
    http_code="${response: -3}"
    body="${response%???}"
    
    if [ "$http_code" -eq 400 ]; then
        print_result 0
        echo "   ✅ Validação funcionou (400 Bad Request)"
    else
        print_result 1
        echo "   ❌ Validação falhou (HTTP Code: $http_code)"
    fi
    echo ""
}

# Função para testar cache
test_cache() {
    local letters=$1
    local data="{\"letters\": \"$letters\"}"
    
    if [ -z "$JWT_TOKEN" ]; then
        echo "❌ Token JWT não disponível"
        return 1
    fi
    
    echo -e "${YELLOW}Testando Cache para: $letters${NC}"
    echo "----------------------------------------"
    
    # Primeira chamada (deve ser cache miss)
    echo -n "Primeira chamada (cache miss)... "
    response1=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/api/anagrams/generate" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -d "$data")
    
    http_code1="${response1: -3}"
    body1="${response1%???}"
    
    if [ "$http_code1" -eq 200 ]; then
        print_result 0
        from_cache1=$(echo "$body1" | grep -o '"fromCache":[^,]*' | cut -d':' -f2 | tr -d ' ')
        echo "   From Cache: $from_cache1"
    else
        print_result 1
        echo "   HTTP Code: $http_code1"
        return 1
    fi
    
    # Aguardar um pouco
    sleep 1
    
    # Segunda chamada (deve ser cache hit)
    echo -n "Segunda chamada (cache hit)... "
    response2=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/api/anagrams/generate" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -d "$data")
    
    http_code2="${response2: -3}"
    body2="${response2%???}"
    
    if [ "$http_code2" -eq 200 ]; then
        print_result 0
        from_cache2=$(echo "$body2" | grep -o '"fromCache":[^,]*' | cut -d':' -f2 | tr -d ' ')
        echo "   From Cache: $from_cache2"
        
        if [ "$from_cache2" = "true" ]; then
            echo "   ✅ Cache hit detectado"
        else
            echo "   ℹ️ Cache miss (pode ser que não tenha sido salvo)"
        fi
    else
        print_result 1
        echo "   HTTP Code: $http_code2"
        return 1
    fi
    
    echo ""
}

# Função para testar gerenciamento de cache (apenas ADMIN)
test_cache_management() {
    echo -e "${YELLOW}Testando Gerenciamento de Cache (apenas ADMIN)${NC}"
    echo "----------------------------------------"
    
    if [ -z "$JWT_TOKEN" ]; then
        echo "❌ Token JWT não disponível"
        return 1
    fi
    
    if [ "$USER_ROLE" != "ADMIN" ]; then
        echo "❌ Usuário não é ADMIN (role: $USER_ROLE)"
        return 1
    fi
    
    # Verificar status do cache
    test_endpoint "GET" "/api/anagrams/cache/status" "" "Status do cache" "$JWT_TOKEN"
    
    # Limpar cache
    test_endpoint "DELETE" "/api/anagrams/cache" "" "Limpar cache" "$JWT_TOKEN"
    
    echo ""
}

# Função para testar performance
test_performance() {
    echo -e "${YELLOW}Testando Performance${NC}"
    echo "----------------------------------------"
    
    if [ -z "$JWT_TOKEN" ]; then
        echo "❌ Token JWT não disponível"
        return 1
    fi
    
    local data="{\"letters\": \"hello\"}"
    
    echo -n "Testando performance com 'hello'... "
    
    start_time=$(date +%s%N)
    
    response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/api/anagrams/generate" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -d "$data")
    
    end_time=$(date +%s%N)
    
    http_code="${response: -3}"
    body="${response%???}"
    
    if [ "$http_code" -eq 200 ]; then
        elapsed=$(( (end_time - start_time) / 1000000 ))
        total_anagrams=$(echo "$body" | grep -o '"totalAnagrams":[0-9]*' | cut -d':' -f2)
        
        print_result 0
        echo "   Tempo de resposta: ${elapsed}ms"
        echo "   Total de anagramas: $total_anagrams"
        
        if [ $elapsed -lt 1000 ]; then
            echo "   ✅ Performance OK (< 1s)"
        else
            echo "   ⚠️ Performance lenta (> 1s)"
        fi
    else
        print_result 1
        echo "   HTTP Code: $http_code"
        return 1
    fi
    
    echo ""
}

# Função principal
main() {
    print_header
    
    local total_tests=0
    local passed_tests=0
    
    # 1. Teste de saúde da API
    print_section "1. TESTE DE SAÚDE DA API"
    test_endpoint "GET" "/api/anagrams/health" "" "Health check da API"
    if [ $? -eq 0 ]; then ((passed_tests++)); fi
    ((total_tests++))
    
    # 2. Teste de saúde da autenticação
    print_section "2. TESTE DE SAÚDE DA AUTENTICAÇÃO"
    test_endpoint "GET" "/api/auth/health" "" "Health check da autenticação"
    if [ $? -eq 0 ]; then ((passed_tests++)); fi
    ((total_tests++))
    
    # 3. Teste de autenticação
    print_section "3. TESTE DE AUTENTICAÇÃO JWT"
    test_authentication
    if [ $? -eq 0 ]; then ((passed_tests++)); fi
    ((total_tests++))
    
    # 4. Teste de acesso negado sem autenticação
    print_section "4. TESTE DE ACESSO NEGADO SEM AUTENTICAÇÃO"
    test_unauthorized_access
    if [ $? -eq 0 ]; then ((passed_tests++)); fi
    ((total_tests++))
    
    # 5. Teste de validação de token
    print_section "5. TESTE DE VALIDAÇÃO DE TOKEN"
    test_token_validation
    if [ $? -eq 0 ]; then ((passed_tests++)); fi
    ((total_tests++))
    
    # 6. Teste de geração de anagramas (com autenticação)
    print_section "6. TESTE DE GERAÇÃO DE ANAGRAMAS (COM AUTENTICAÇÃO)"
    test_endpoint "POST" "/api/anagrams/generate" "{\"letters\": \"abc\"}" "Geração de anagramas para 'abc'" "$JWT_TOKEN"
    if [ $? -eq 0 ]; then ((passed_tests++)); fi
    ((total_tests++))
    
    test_endpoint "POST" "/api/anagrams/generate" "{\"letters\": \"test\"}" "Geração de anagramas para 'test'" "$JWT_TOKEN"
    if [ $? -eq 0 ]; then ((passed_tests++)); fi
    ((total_tests++))
    
    # 7. Teste de cache (com autenticação)
    print_section "7. TESTE DE CACHE (COM AUTENTICAÇÃO)"
    test_cache "abc"
    if [ $? -eq 0 ]; then ((passed_tests++)); fi
    ((total_tests++))
    
    # 8. Teste de validação (com autenticação)
    print_section "8. TESTE DE VALIDAÇÃO (COM AUTENTICAÇÃO)"
    test_validation "{\"letters\": \"\"}" "Letras vazias"
    if [ $? -eq 0 ]; then ((passed_tests++)); fi
    ((total_tests++))
    
    test_validation "{\"letters\": \"123\"}" "Letras com números"
    if [ $? -eq 0 ]; then ((passed_tests++)); fi
    ((total_tests++))
    
    # 9. Teste de gerenciamento de cache (apenas ADMIN)
    print_section "9. TESTE DE GERENCIAMENTO DE CACHE (APENAS ADMIN)"
    test_cache_management
    if [ $? -eq 0 ]; then ((passed_tests++)); fi
    ((total_tests++))
    
    # 10. Teste de performance (com autenticação)
    print_section "10. TESTE DE PERFORMANCE (COM AUTENTICAÇÃO)"
    test_performance
    if [ $? -eq 0 ]; then ((passed_tests++)); fi
    ((total_tests++))
    
    # Resumo final
    echo -e "${BLUE}📊 RESUMO DOS TESTES${NC}"
    echo "===================="
    echo "Total de testes: $total_tests"
    echo -e "Testes aprovados: ${GREEN}$passed_tests${NC}"
    echo -e "Testes reprovados: ${RED}$((total_tests - passed_tests))${NC}"
    
    if [ $passed_tests -eq $total_tests ]; then
        echo ""
        echo -e "${GREEN}🎉 TODOS OS TESTES PASSARAM!${NC}"
        echo -e "${GREEN}A API está funcionando perfeitamente com autenticação JWT!${NC}"
    else
        echo ""
        echo -e "${YELLOW}⚠️ ALGUNS TESTES FALHARAM${NC}"
        echo -e "${YELLOW}Verifique os logs acima para identificar os problemas.${NC}"
    fi
    
    echo ""
    echo -e "${BLUE}💡 Dica: Execute este script sempre que quiser verificar se a API está funcionando corretamente.${NC}"
    echo -e "${BLUE}🔐 A API agora requer autenticação JWT para a maioria dos endpoints.${NC}"
}

# Executar função principal
main
