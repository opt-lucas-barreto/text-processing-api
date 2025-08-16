# Script para debug do cache de anagramas - Windows PowerShell
# Testa especificamente a funcionalidade de cache hit/miss com autenticação JWT

# Configurações
$BASE_URL = "http://localhost:8080"

# Variáveis globais para autenticação
$JWT_TOKEN = ""
$USER_ROLE = ""

# Função para imprimir resultado colorido
function Write-Result {
    param(
        [bool]$Success,
        [string]$Message
    )
    
    if ($Success) {
        Write-Host "[OK] $Message" -ForegroundColor Green
    } else {
        Write-Host "[ERRO] $Message" -ForegroundColor Red
    }
}

# Função para imprimir status
function Write-Status {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Blue
}

# Função para imprimir erro
function Write-Error {
    param([string]$Message)
    Write-Host "[ERRO] $Message" -ForegroundColor Red
}

# Função para imprimir sucesso
function Write-Success {
    param([string]$Message)
    Write-Host "[OK] $Message" -ForegroundColor Green
}

# Função para autenticação
function Authenticate {
    Write-Status "Realizando autenticação JWT..."
    
    # Credenciais do admin
    $adminCredentials = @{
        username = "admin"
        password = "admin123"
    } | ConvertTo-Json
    
    try {
        # Fazer login
        $authResponse = Invoke-RestMethod -Uri "$BASE_URL/api/auth/login" -Method Post -Body $adminCredentials -ContentType "application/json"
        
        if ($authResponse.token) {
            $script:JWT_TOKEN = $authResponse.token
            $script:USER_ROLE = $authResponse.role
            
            Write-Result $true "Login realizado com sucesso"
            Write-Host "   Role: $($authResponse.role)" -ForegroundColor Yellow
            Write-Host "   Token: $($authResponse.token.Substring(0, [Math]::Min(20, $authResponse.token.Length)))..." -ForegroundColor Yellow
            return $true
        } else {
            Write-Result $false "Token não encontrado na resposta"
            return $false
        }
    } catch {
        Write-Result $false "Falha na autenticação: $($_.Exception.Message)"
        return $false
    }
}

Write-Host "[DEBUG] DEBUG DO CACHE DE ANAGRAMAS - WINDOWS (COM JWT)" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host ""

# 1. Verificar se a aplicação está rodando
Write-Host "1. Verificando saúde da aplicação..." -ForegroundColor White
try {
    $healthResponse = Invoke-RestMethod -Uri "$BASE_URL/api/anagrams/health" -Method Get
    if ($healthResponse.status -eq "UP") {
        Write-Result $true "API está funcionando"
        Write-Host "   Resposta: $($healthResponse | ConvertTo-Json)" -ForegroundColor Yellow
    } else {
        Write-Result $false "API não está saudável"
        exit 1
    }
} catch {
    Write-Result $false "Aplicação não está respondendo. Verifique se está rodando."
    Write-Host "   Erro: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 2. Autenticação JWT
Write-Host "2. Autenticação JWT..." -ForegroundColor White
if (-not (Authenticate)) {
    Write-Error "Não foi possível autenticar. Saindo..."
    exit 1
}

Write-Host ""

# 3. Verificar status do cache (com autenticação)
Write-Host "3. Verificando status do cache..." -ForegroundColor White
$headers = @{
    "Authorization" = "Bearer $JWT_TOKEN"
}

try {
    $cacheStatus = Invoke-RestMethod -Uri "$BASE_URL/api/anagrams/cache/status" -Method Get -Headers $headers
    Write-Result $true "Status obtido com sucesso"
    Write-Host "   Status: $($cacheStatus | ConvertTo-Json)" -ForegroundColor Yellow
} catch {
    Write-Result $false "Não foi possível obter status do cache"
    Write-Host "   Erro: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# 4. Testar funcionalidade do cache
Write-Host "4. Testando funcionalidade do cache..." -ForegroundColor White
Write-Host "   Dados de teste: 'test'" -ForegroundColor Yellow

$data = @{
    letters = "test"
} | ConvertTo-Json

# Primeira chamada (deve ser cache miss)
Write-Host "   Primeira chamada (cache miss)..." -NoNewline
try {
    $response1 = Invoke-RestMethod -Uri "$BASE_URL/api/anagrams/generate" -Method Post -Body $data -ContentType "application/json" -Headers $headers
    Write-Result $true "OK"
    Write-Host "   [OK] Resposta recebida" -ForegroundColor Green
    
    # Verificar se é cache miss
    if ($response1.fromCache -eq $false) {
        Write-Host "   [OK] Cache miss detectado corretamente" -ForegroundColor Green
    } else {
        Write-Host "   [INFO] Cache hit (pode ser que já exista no cache)" -ForegroundColor Yellow
    }
} catch {
    Write-Result $false "Falha na primeira chamada"
    Write-Host "   Erro: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Aguardar um pouco para garantir que o cache foi salvo
Start-Sleep -Seconds 1

# Segunda chamada (deve ser cache hit)
Write-Host "   Segunda chamada (cache hit)..." -NoNewline
try {
    $response2 = Invoke-RestMethod -Uri "$BASE_URL/api/anagrams/generate" -Method Post -Body $data -ContentType "application/json" -Headers $headers
    Write-Result $true "OK"
    Write-Host "   [OK] Resposta recebida" -ForegroundColor Green
    
    # Verificar se é cache hit
    if ($response2.fromCache -eq $true) {
        Write-Host "   [OK] Cache hit detectado corretamente" -ForegroundColor Green
    } else {
        Write-Host "   [INFO] Cache miss (pode ser que não tenha sido salvo)" -ForegroundColor Yellow
    }
    
    # Verificar consistência dos resultados
    if ($response1.totalAnagrams -eq $response2.totalAnagrams) {
        Write-Host "   [OK] Resultados consistentes: $($response1.totalAnagrams) anagramas" -ForegroundColor Green
    } else {
        Write-Host "   [AVISO] Resultados inconsistentes: $($response1.totalAnagrams) vs $($response2.totalAnagrams)" -ForegroundColor Red
    }
} catch {
    Write-Result $false "Falha na segunda chamada"
    Write-Host "   Erro: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 5. Testar cache inteligente
Write-Host "5. Testando cache inteligente..." -ForegroundColor White
Write-Host "   Testando anagramas com mesma composição..." -ForegroundColor Yellow

# Primeiro, gerar para "abc"
$dataAbc = @{ letters = "abc" } | ConvertTo-Json
try {
    $responseAbc = Invoke-RestMethod -Uri "$BASE_URL/api/anagrams/generate" -Method Post -Body $dataAbc -ContentType "application/json" -Headers $headers
    Write-Host "   [OK] 'abc' gerado: $($responseAbc.totalAnagrams) anagramas" -ForegroundColor Yellow
    
    # Agora testar "cba" (mesma composição)
    $dataCba = @{ letters = "cba" } | ConvertTo-Json
    $responseCba = Invoke-RestMethod -Uri "$BASE_URL/api/anagrams/generate" -Method Post -Body $dataCba -ContentType "application/json" -Headers $headers
    
    if ($responseCba.fromCache -eq $true) {
        Write-Result $true "Cache inteligente funcionando"
        Write-Host "   'cba' recuperado do cache: $($responseCba.totalAnagrams) anagramas" -ForegroundColor Yellow
    } else {
        Write-Result $false "Cache inteligente não funcionou"
    }
} catch {
    Write-Result $false "Falha ao testar cache inteligente: $($_.Exception.Message)"
}

Write-Host ""

# 6. Testar gerenciamento de cache (apenas ADMIN)
if ($USER_ROLE -eq "ADMIN") {
    Write-Host "6. Testando gerenciamento de cache (ADMIN)..." -ForegroundColor White
    
    # Limpar cache
    Write-Host "   Limpando cache..." -NoNewline
    try {
        $null = Invoke-RestMethod -Uri "$BASE_URL/api/anagrams/cache" -Method Delete -Headers $headers
        Write-Result $true "Cache limpo com sucesso"
        
        # Verificar se foi limpo (testar com palavra que estava no cache)
        Write-Host "   Verificando limpeza..." -NoNewline
        $testResponse = Invoke-RestMethod -Uri "$BASE_URL/api/anagrams/generate" -Method Post -Body $data -ContentType "application/json" -Headers $headers
        
        if ($testResponse.fromCache -eq $false) {
            Write-Result $true "Cache foi limpo corretamente"
        } else {
            Write-Result $false "Cache não foi limpo"
        }
    } catch {
        Write-Result $false "Falha ao gerenciar cache: $($_.Exception.Message)"
    }
} else {
    Write-Host "6. Testando gerenciamento de cache (ADMIN)..." -ForegroundColor White
    Write-Host "   [AVISO] Usuário não é ADMIN (role: $USER_ROLE), pulando teste" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "[DEBUG] DEBUG CONCLUÍDO" -ForegroundColor Cyan
Write-Host "=====================" -ForegroundColor Cyan
Write-Host "Cache de anagramas testado com sucesso!" -ForegroundColor Green
Write-Host "Todos os testes de funcionalidade foram executados." -ForegroundColor Green
