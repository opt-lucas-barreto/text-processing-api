# Script de Teste Automatizado para Text Processing API - Windows PowerShell
# Testa todos os endpoints da API de anagramas com autenticação JWT

# Configurações de segurança e boas práticas
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Configurações
$script:BASE_URL = "http://localhost:8080"
$script:TEST_LETTERS = @("abc", "test", "xyz", "a", "hello")

# Variáveis globais para autenticação
$script:JWT_TOKEN = ""
$script:USER_ROLE = ""

# Função para imprimir resultado colorido
function Write-Result {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [bool]$Success,
        
        [Parameter(Mandatory = $true)]
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
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$Message
    )
    Write-Host "[INFO] $Message" -ForegroundColor Blue
}

# Função para imprimir erro
function Write-Error {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$Message
    )
    Write-Host "[ERRO] $Message" -ForegroundColor Red
}

# Função para imprimir sucesso
function Write-Success {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$Message
    )
    Write-Host "[OK] $Message" -ForegroundColor Green
}

# Função para imprimir título
function Write-Title {
    [CmdletBinding()]
    param([string]$Title)
    Write-Host ""
    Write-Host "[TESTE] $Title" -ForegroundColor Cyan
    Write-Host ("=" * ($Title.Length + 8)) -ForegroundColor Cyan
}

# Função para autenticação
function Test-Authentication {
    Write-Status "Testando autenticação JWT..."
    
    # Testar login com usuário admin
    $adminCredentials = @{
        username = "admin"
        password = "admin123"
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$($script:BASE_URL)/api/auth/login" -Method Post -Body $adminCredentials -ContentType "application/json"
        
        if ($response.token) {
            $script:JWT_TOKEN = $response.token
            $script:USER_ROLE = $response.role
            Write-Result -Success $true -Message "Login admin realizado com sucesso"
            Write-Host "   Role: $($response.role)" -ForegroundColor Yellow
            Write-Host "   Token: $($response.token.Substring(0, [Math]::Min(20, $response.token.Length)))..." -ForegroundColor Yellow
            return $true
        } else {
            Write-Result -Success $false -Message "Resposta de login inválida"
            return $false
        }
    } catch {
        Write-Result -Success $false -Message "Falha no login: $($_.Exception.Message)"
        return $false
    }
}

# Função para testar endpoint de saúde
function Test-Health {
    Write-Status "Testando endpoint de saúde..."
    
    try {
        $response = Invoke-RestMethod -Uri "$($script:BASE_URL)/api/anagrams/health" -Method Get
        if ($response.status -eq "UP") {
            Write-Result -Success $true -Message "API está funcionando"
            return $true
        } else {
            Write-Result -Success $false -Message "API não está saudável"
            return $false
        }
    } catch {
        Write-Result -Success $false -Message "Falha ao conectar na API: $($_.Exception.Message)"
        return $false
    }
}

# Função para testar endpoint de saúde da autenticação
function Test-AuthHealth {
    Write-Status "Testando endpoint de saúde da autenticação..."
    
    try {
        $response = Invoke-RestMethod -Uri "$($script:BASE_URL)/api/auth/health" -Method Get
        if ($response.status -eq "UP") {
            Write-Result -Success $true -Message "Serviço de autenticação está funcionando"
            return $true
        } else {
            Write-Result -Success $false -Message "Serviço de autenticação não está saudável"
            return $false
        }
    } catch {
        Write-Result -Success $false -Message "Falha ao conectar no serviço de autenticação: $($_.Exception.Message)"
        return $false
    }
}

# Função para testar geração de anagramas (com autenticação)
function Test-GenerateAnagrams {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Letters
    )
    
    Write-Status "Testando geração de anagramas para '$Letters'..."
    
    if (-not $script:JWT_TOKEN) {
        Write-Result -Success $false -Message "Token JWT não disponível"
        return $false
    }
    
    $body = @{
        letters = $Letters
    } | ConvertTo-Json
    
    $headers = @{
        "Authorization" = "Bearer $($script:JWT_TOKEN)"
    }
    
    try {
        $response = Invoke-RestMethod -Uri "$($script:BASE_URL)/api/anagrams/generate" -Method Post -Body $body -ContentType "application/json" -Headers $headers
        
        if ($response.originalLetters -and $response.anagrams -and $response.totalAnagrams -ge 0) {
            Write-Result -Success $true -Message "Anagramas gerados com sucesso"
            Write-Host "   Total: $($response.totalAnagrams) anagramas" -ForegroundColor Yellow
            Write-Host "   From Cache: $($response.fromCache)" -ForegroundColor Yellow
            return $true
        } else {
            Write-Result -Success $false -Message "Resposta inválida"
            return $false
        }
    } catch {
        Write-Result -Success $false -Message "Falha na geração: $($_.Exception.Message)"
        return $false
    }
}

# Função para testar acesso negado sem autenticação
function Test-UnauthorizedAccess {
    Write-Status "Testando acesso negado sem autenticação..."
    
    $body = @{
        letters = "abc"
    } | ConvertTo-Json
    
    try {
        $null = Invoke-RestMethod -Uri "$($script:BASE_URL)/api/anagrams/generate" -Method Post -Body $body -ContentType "application/json"
        Write-Result -Success $false -Message "Acesso deveria ter sido negado"
        return $false
    } catch {
        if ($_.Exception.Response.StatusCode -eq 401) {
            Write-Result -Success $true -Message "Acesso negado corretamente (401 - Unauthorized)"
            return $true
        } else {
            Write-Result -Success $false -Message "Erro inesperado: $($_.Exception.Message)"
            return $false
        }
    }
}

# Função para testar cache (com autenticação)
function Test-Cache {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Letters
    )
    
    Write-Status "Testando cache para '$Letters'..."
    
    if (-not $script:JWT_TOKEN) {
        Write-Result -Success $false -Message "Token JWT não disponível"
        return $false
    }
    
    $body = @{
        letters = $Letters
    } | ConvertTo-Json
    
    $headers = @{
        "Authorization" = "Bearer $($script:JWT_TOKEN)"
    }
    
    # Primeira chamada (deve ser cache miss)
    Write-Host "   Primeira chamada (cache miss)..." -NoNewline
    try {
        $response1 = Invoke-RestMethod -Uri "$($script:BASE_URL)/api/anagrams/generate" -Method Post -Body $body -ContentType "application/json" -Headers $headers
        Write-Result -Success $true -Message "OK"
        
        if ($response1.fromCache -eq $false) {
            Write-Host "   [OK] Cache miss detectado" -ForegroundColor Green
        } else {
            Write-Host "   [INFO] Cache hit (dados já existem)" -ForegroundColor Yellow
        }
    } catch {
        Write-Result -Success $false -Message "Falha"
        return $false
    }
    
    # Aguardar um pouco
    Start-Sleep -Seconds 1
    
    # Segunda chamada (deve ser cache hit)
    Write-Host "   Segunda chamada (cache hit)..." -NoNewline
    try {
        $response2 = Invoke-RestMethod -Uri "$($script:BASE_URL)/api/anagrams/generate" -Method Post -Body $body -ContentType "application/json" -Headers $headers
        Write-Result -Success $true -Message "OK"
        
        if ($response2.fromCache -eq $true) {
            Write-Host "   [OK] Cache hit detectado" -ForegroundColor Green
        } else {
            Write-Host "   [INFO] Cache miss (pode ser que não tenha sido salvo)" -ForegroundColor Yellow
        }
        
        # Verificar consistência
        if ($response1.totalAnagrams -eq $response2.totalAnagrams) {
            Write-Host "   [OK] Resultados consistentes" -ForegroundColor Green
        } else {
            Write-Host "   [AVISO] Resultados inconsistentes" -ForegroundColor Red
        }
        
        return $true
    } catch {
        Write-Result -Success $false -Message "Falha"
        return $false
    }
}

# Função para testar validação (com autenticação)
function Test-Validation {
    Write-Status "Testando validação de entrada..."
    
    if (-not $script:JWT_TOKEN) {
        Write-Result -Success $false -Message "Token JWT não disponível"
        return $false
    }
    
    $headers = @{
        "Authorization" = "Bearer $($script:JWT_TOKEN)"
    }
    
    $invalidInputs = @(
        @{ letters = ""; expectedError = "não pode estar vazio" },
        @{ letters = "123"; expectedError = "apenas letras" },
        @{ letters = "a@b"; expectedError = "apenas letras" },
        @{ letters = "   "; expectedError = "não pode estar vazio" }
    )
    
    $successCount = 0
    
    foreach ($testInput in $invalidInputs) {
        $body = @{
            letters = $testInput.letters
        } | ConvertTo-Json
        
        try {
            $null = Invoke-RestMethod -Uri "$($script:BASE_URL)/api/anagrams/generate" -Method Post -Body $body -ContentType "application/json" -Headers $headers
            Write-Result -Success $false -Message "Validação falhou para '$($testInput.letters)' - deveria ter erro"
        } catch {
            if ($_.Exception.Response.StatusCode -eq 400) {
                Write-Result -Success $true -Message "Validação OK para '$($testInput.letters)'"
                $successCount++
            } else {
                Write-Result -Success $false -Message "Erro inesperado para '$($testInput.letters)': $($_.Exception.Message)"
            }
        }
    }
    
    return $successCount -eq $invalidInputs.Count
}

# Função para testar cache inteligente (com autenticação)
function Test-SmartCache {
    Write-Status "Testando cache inteligente (anagramas com mesma composição)..."
    
    if (-not $script:JWT_TOKEN) {
        Write-Result -Success $false -Message "Token JWT não disponível"
        return $false
    }
    
    $headers = @{
        "Authorization" = "Bearer $($script:JWT_TOKEN)"
    }
    
    # Primeiro, gerar anagramas para "test"
    $body1 = @{ letters = "test" } | ConvertTo-Json
    try {
        $response1 = Invoke-RestMethod -Uri "$($script:BASE_URL)/api/anagrams/generate" -Method Post -Body $body1 -ContentType "application/json" -Headers $headers
        Write-Host "   'test' gerado: $($response1.totalAnagrams) anagramas" -ForegroundColor Yellow
    } catch {
        Write-Result -Success $false -Message "Falha ao gerar 'test'"
        return $false
    }
    
    # Agora testar "tets" (mesma composição, ordem diferente)
    $body2 = @{ letters = "tets" } | ConvertTo-Json
    try {
        $response2 = Invoke-RestMethod -Uri "$($script:BASE_URL)/api/anagrams/generate" -Method Post -Body $body2 -ContentType "application/json" -Headers $headers
        
        if ($response2.fromCache -eq $true) {
            Write-Result -Success $true -Message "Cache inteligente funcionando"
            Write-Host "   'tets' recuperado do cache: $($response2.totalAnagrams) anagramas" -ForegroundColor Yellow
        } else {
            Write-Result -Success $false -Message "Cache inteligente não funcionou"
        }
        
        return $response2.fromCache -eq $true
    } catch {
        Write-Result -Success $false -Message "Falha ao testar cache inteligente"
        return $false
    }
}

# Função para testar gerenciamento de cache (apenas para ADMIN)
function Test-CacheManagement {
    Write-Status "Testando gerenciamento de cache (apenas ADMIN)..."
    
    if (-not $script:JWT_TOKEN) {
        Write-Result -Success $false -Message "Token JWT não disponível"
        return $false
    }
    
    if ($script:USER_ROLE -ne "ADMIN") {
        Write-Result -Success $false -Message "Usuário não é ADMIN (role: $($script:USER_ROLE))"
        return $false
    }
    
    $headers = @{
        "Authorization" = "Bearer $($script:JWT_TOKEN)"
    }
    
    # Verificar status do cache
    try {
        $status = Invoke-RestMethod -Uri "$($script:BASE_URL)/api/anagrams/cache/status" -Method Get -Headers $headers
        Write-Host "   Status: $($status.message)" -ForegroundColor Yellow
    } catch {
        Write-Result -Success $false -Message "Falha ao obter status do cache"
        return $false
    }
    
    # Limpar cache
    try {
        $null = Invoke-RestMethod -Uri "$($script:BASE_URL)/api/anagrams/cache" -Method Delete -Headers $headers
        Write-Result -Success $true -Message "Cache limpo com sucesso"
    } catch {
        Write-Result -Success $false -Message "Falha ao limpar cache"
        return $false
    }
    
    # Verificar se cache foi limpo (testar com palavra que estava no cache)
    $body = @{ letters = "test" } | ConvertTo-Json
    try {
        $response = Invoke-RestMethod -Uri "$($script:BASE_URL)/api/anagrams/generate" -Method Post -Body $body -ContentType "application/json" -Headers $headers
        
        if ($response.fromCache -eq $false) {
            Write-Result -Success $true -Message "Cache foi limpo corretamente"
        } else {
            Write-Result -Success $false -Message "Cache não foi limpo"
        }
        
        return $response.fromCache -eq $false
    } catch {
        Write-Result -Success $false -Message "Falha ao verificar limpeza do cache"
        return $false
    }
}

# Função para testar performance (com autenticação)
function Test-Performance {
    Write-Status "Testando performance..."
    
    if (-not $script:JWT_TOKEN) {
        Write-Result -Success $false -Message "Token JWT não disponível"
        return $false
    }
    
    $headers = @{
        "Authorization" = "Bearer $($script:JWT_TOKEN)"
    }
    
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    
    # Testar com palavra de 5 letras
    $body = @{ letters = "hello" } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$($script:BASE_URL)/api/anagrams/generate" -Method Post -Body $body -ContentType "application/json" -Headers $headers
        $stopwatch.Stop()
        
        $elapsed = $stopwatch.ElapsedMilliseconds
        Write-Host "   Tempo de resposta: ${elapsed}ms" -ForegroundColor Yellow
        Write-Host "   Total de anagramas: $($response.totalAnagrams)" -ForegroundColor Yellow
        
        if ($elapsed -lt 1000) {
            Write-Result -Success $true -Message "Performance OK (< 1s)"
            return $true
        } else {
            Write-Result -Success $false -Message "Performance lenta (> 1s)"
            return $false
        }
    } catch {
        Write-Result -Success $false -Message "Falha no teste de performance"
        return $false
    }
}

# Função para testar validação de token
function Test-TokenValidation {
    Write-Status "Testando validação de token JWT..."
    
    if (-not $script:JWT_TOKEN) {
        Write-Result -Success $false -Message "Token JWT não disponível"
        return $false
    }
    
    $headers = @{
        "Authorization" = "Bearer $($script:JWT_TOKEN)"
    }
    
    try {
        $response = Invoke-RestMethod -Uri "$($script:BASE_URL)/api/auth/validate" -Method Get -Headers $headers
        if ($response.status -eq "AUTHORIZED") {
            Write-Result -Success $true -Message "Token JWT válido"
            return $true
        } else {
            Write-Result -Success $false -Message "Token JWT inválido"
            return $false
        }
    } catch {
        Write-Result -Success $false -Message "Falha na validação do token: $($_.Exception.Message)"
        return $false
    }
}

# Função principal de teste
function Start-TestSuite {
    Write-Host ""
    Write-Host "[TESTE] TESTE AUTOMATIZADO - TEXT PROCESSING API (COM JWT)" -ForegroundColor Magenta
    Write-Host "=====================================================" -ForegroundColor Magenta
    Write-Host ""
    
    $totalTests = 0
    $passedTests = 0
    
    # 1. Teste de saúde
    Write-Title -Title "1. TESTE DE SAUDE DA API"
    if (Test-Health) { $passedTests++ }
    $totalTests++
    
    # 2. Teste de saúde da autenticação
    Write-Title -Title "2. TESTE DE SAUDE DA AUTENTICACAO"
    if (Test-AuthHealth) { $passedTests++ }
    $totalTests++
    
    # 3. Teste de autenticação
    Write-Title -Title "3. TESTE DE AUTENTICACAO JWT"
    if (Test-Authentication) { $passedTests++ }
    $totalTests++
    
    # 4. Teste de acesso negado sem autenticação
    Write-Title -Title "4. TESTE DE ACESSO NEGADO SEM AUTENTICACAO"
    if (Test-UnauthorizedAccess) { $passedTests++ }
    $totalTests++
    
    # 5. Teste de validação de token
    Write-Title -Title "5. TESTE DE VALIDACAO DE TOKEN"
    if (Test-TokenValidation) { $passedTests++ }
    $totalTests++
    
    # 6. Teste de geração básica (com autenticação)
    Write-Title -Title "6. TESTE DE GERACAO BASICA (COM AUTENTICACAO)"
    foreach ($letters in $script:TEST_LETTERS) {
        if (Test-GenerateAnagrams -Letters $letters) { $passedTests++ }
        $totalTests++
    }
    
    # 7. Teste de cache (com autenticação)
    Write-Title -Title "7. TESTE DE CACHE (COM AUTENTICACAO)"
    if (Test-Cache -Letters "abc") { $passedTests++ }
    $totalTests++
    
    # 8. Teste de validação (com autenticação)
    Write-Title -Title "8. TESTE DE VALIDACAO (COM AUTENTICACAO)"
    if (Test-Validation) { $passedTests++ }
    $totalTests++
    
    # 9. Teste de cache inteligente (com autenticação)
    Write-Title -Title "9. TESTE DE CACHE INTELIGENTE (COM AUTENTICACAO)"
    if (Test-SmartCache) { $passedTests++ }
    $totalTests++
    
    # 10. Teste de gerenciamento de cache (apenas ADMIN)
    Write-Title -Title "10. TESTE DE GERENCIAMENTO DE CACHE (APENAS ADMIN)"
    if (Test-CacheManagement) { $passedTests++ }
    $totalTests++
    
    # 11. Teste de performance (com autenticação)
    Write-Title -Title "11. TESTE DE PERFORMANCE (COM AUTENTICACAO)"
    if (Test-Performance) { $passedTests++ }
    $totalTests++
    
    # Resumo final
    Write-Host ""
    Write-Host "[RESUMO] RESUMO DOS TESTES" -ForegroundColor Cyan
    Write-Host "====================" -ForegroundColor Cyan
    Write-Host "Total de testes: $totalTests" -ForegroundColor White
    Write-Host "Testes aprovados: $passedTests" -ForegroundColor Green
    Write-Host "Testes reprovados: $($totalTests - $passedTests)" -ForegroundColor Red
    
    if ($passedTests -eq $totalTests) {
        Write-Host ""
        Write-Host "[SUCESSO] TODOS OS TESTES PASSARAM!" -ForegroundColor Green
        Write-Host "A API está funcionando perfeitamente com autenticação JWT!" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "[AVISO] ALGUNS TESTES FALHARAM" -ForegroundColor Yellow
        Write-Host "Verifique os logs acima para identificar os problemas." -ForegroundColor Yellow
    }
    
    Write-Host ""
    Write-Host "[DICA] Execute este script sempre que quiser verificar se a API está funcionando corretamente." -ForegroundColor Blue
    Write-Host "[SEGURANCA] A API agora requer autenticação JWT para a maioria dos endpoints." -ForegroundColor Blue
}

# Executar suite de testes
Start-TestSuite
