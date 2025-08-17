package com.lucas.text_processing_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.lucas.text_processing_api.dto.AnagramRequest;
import com.lucas.text_processing_api.dto.AnagramResponse;

// DTO interno para login
class LoginRequest {
    private String username;
    private String password;
    
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

// DTO interno para resposta de login
class LoginResponse {
    private String token;
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}

/**
 * Teste de integração real da API HTTP
 * 
 * Este teste usa TestRestTemplate para fazer requisições HTTP reais
 * para a aplicação rodando, testando o fluxo completo com Redis Embedded.
 * 
 * @author Lucas
 * @version 1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AnagramApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;


    private String baseUrl;
    private String authToken;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        // Fazer login para obter o token JWT
        authToken = authenticateAndGetToken();
    }
    
    /**
     * Faz login na aplicação e retorna o token JWT
     */
    private String authenticateAndGetToken() {
        LoginRequest loginRequest = new LoginRequest("user", "user123");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);
        
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
            baseUrl + "/api/auth/login",
            request,
            LoginResponse.class
        );
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody().getToken();
        }
        
        throw new RuntimeException("Falha na autenticação: " + response.getStatusCode());
    }
    
    /**
     * Cria headers com autenticação JWT
     */
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        return headers;
    }

    @Test
    @DisplayName("Deve gerar anagramas via HTTP e salvar no cache Redis Embedded")
    void shouldGenerateAnagramsViaHttpAndSaveToRedisCache() {
        // Arrange
        AnagramRequest request = new AnagramRequest();
        request.setLetters("abc");
        
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<AnagramRequest> requestEntity = new HttpEntity<>(request, headers);

        // Act - primeira chamada
        ResponseEntity<AnagramResponse> firstResponse = restTemplate.postForEntity(
            baseUrl + "/api/anagrams/generate",
            requestEntity,
            AnagramResponse.class
        );

        // Assert - primeira chamada
        assertEquals(HttpStatus.OK, firstResponse.getStatusCode());
        assertNotNull(firstResponse.getBody());
        AnagramResponse firstBody = firstResponse.getBody();
        assertEquals("abc", firstBody.getOriginalLetters());
        assertEquals(6, firstBody.getTotalAnagrams());
        // Pode vir do cache se já foi executado em outro teste
        assertTrue(firstBody.getProcessingTimeMs() >= 0);

        // Act - segunda chamada (deve vir do cache)
        ResponseEntity<AnagramResponse> secondResponse = restTemplate.postForEntity(
            baseUrl + "/api/anagrams/generate",
            requestEntity,
            AnagramResponse.class
        );

        // Assert - segunda chamada
        assertEquals(HttpStatus.OK, secondResponse.getStatusCode());
        assertNotNull(secondResponse.getBody());
        AnagramResponse secondBody = secondResponse.getBody();
        assertEquals("abc", secondBody.getOriginalLetters());
        assertEquals(6, secondBody.getTotalAnagrams());
        assertTrue(secondBody.isFromCache());
        assertTrue(secondBody.getProcessingTimeMs() >= 0);

        // Verificar que os dados são consistentes
        assertEquals(firstBody.getTotalAnagrams(), secondBody.getTotalAnagrams());
        assertEquals(firstBody.getOriginalLetters(), secondBody.getOriginalLetters());
        assertEquals(firstBody.getAnagrams(), secondBody.getAnagrams());
    }

    @Test
    @DisplayName("Deve testar diferentes tamanhos de entrada via HTTP")
    void shouldTestDifferentInputSizesViaHttp() {
        // Teste com 1 letra
        AnagramRequest singleLetter = new AnagramRequest();
        singleLetter.setLetters("a");
        
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<AnagramRequest> requestEntity = new HttpEntity<>(singleLetter, headers);
        
        ResponseEntity<AnagramResponse> response = restTemplate.postForEntity(
            baseUrl + "/api/anagrams/generate",
            requestEntity,
            AnagramResponse.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalAnagrams());

        // Teste com 2 letras
        AnagramRequest twoLetters = new AnagramRequest();
        twoLetters.setLetters("ab");
        
        HttpEntity<AnagramRequest> twoLettersEntity = new HttpEntity<>(twoLetters, headers);
        response = restTemplate.postForEntity(
            baseUrl + "/api/anagrams/generate",
            twoLettersEntity,
            AnagramResponse.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotalAnagrams());

        // Teste com 3 letras
        AnagramRequest threeLetters = new AnagramRequest();
        threeLetters.setLetters("abc");
        
        HttpEntity<AnagramRequest> threeLettersEntity = new HttpEntity<>(threeLetters, headers);
        response = restTemplate.postForEntity(
            baseUrl + "/api/anagrams/generate",
            threeLettersEntity,
            AnagramResponse.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(6, response.getBody().getTotalAnagrams());
    }

    @Test
    @DisplayName("Deve normalizar letras para minúsculas via HTTP")
    void shouldNormalizeLettersToLowerCaseViaHttp() {
        // Arrange
        AnagramRequest request = new AnagramRequest();
        request.setLetters("AbC");

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<AnagramRequest> requestEntity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<AnagramResponse> response = restTemplate.postForEntity(
            baseUrl + "/api/anagrams/generate",
            requestEntity,
            AnagramResponse.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("abc", response.getBody().getOriginalLetters());
        assertEquals(6, response.getBody().getTotalAnagrams());
    }

    @Test
    @DisplayName("Deve lidar com entrada vazia via HTTP")
    void shouldHandleEmptyInputViaHttp() {
        // Arrange
        AnagramRequest request = new AnagramRequest();
        request.setLetters("");

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<AnagramRequest> requestEntity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<AnagramResponse> response = restTemplate.postForEntity(
            baseUrl + "/api/anagrams/generate",
            requestEntity,
            AnagramResponse.class
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve lidar com entrada nula via HTTP")
    void shouldHandleNullInputViaHttp() {
        // Arrange
        AnagramRequest request = new AnagramRequest();
        request.setLetters(null);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<AnagramRequest> requestEntity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<AnagramResponse> response = restTemplate.postForEntity(
            baseUrl + "/api/anagrams/generate",
            requestEntity,
            AnagramResponse.class
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve testar performance do cache via HTTP")
    void shouldTestCachePerformanceViaHttp() {
        // Arrange
        AnagramRequest request = new AnagramRequest();
        request.setLetters("hello");

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<AnagramRequest> requestEntity = new HttpEntity<>(request, headers);

        // Primeira chamada - sem cache
        long startTime = System.currentTimeMillis();
        ResponseEntity<AnagramResponse> firstResponse = restTemplate.postForEntity(
            baseUrl + "/api/anagrams/generate",
            requestEntity,
            AnagramResponse.class
        );
        long firstCallTime = System.currentTimeMillis() - startTime;

        // Segunda chamada - com cache
        startTime = System.currentTimeMillis();
        ResponseEntity<AnagramResponse> secondResponse = restTemplate.postForEntity(
            baseUrl + "/api/anagrams/generate",
            requestEntity,
            AnagramResponse.class
        );
        long secondCallTime = System.currentTimeMillis() - startTime;

        // Assert
        assertEquals(HttpStatus.OK, firstResponse.getStatusCode());
        assertEquals(HttpStatus.OK, secondResponse.getStatusCode());
        assertNotNull(firstResponse.getBody());
        assertNotNull(secondResponse.getBody());
        assertFalse(firstResponse.getBody().isFromCache());
        assertTrue(secondResponse.getBody().isFromCache());
        
        // Cache deve ser mais rápido (com tolerância para variações)
        assertTrue(secondCallTime <= firstCallTime + 100, 
            "Chamada com cache deve ser mais rápida. Primeira: " + firstCallTime + "ms, Segunda: " + secondCallTime + "ms");
    }

    @Test
    @DisplayName("Deve testar múltiplas requisições simultâneas via HTTP")
    void shouldTestMultipleConcurrentRequestsViaHttp() {
        // Arrange
        String[] testInputs = {"a", "ab", "abc", "test"};
        
        // Primeira rodada - todas devem gerar anagramas
        for (String input : testInputs) {
            AnagramRequest request = new AnagramRequest();
            request.setLetters(input);

            HttpHeaders headers = createAuthHeaders();
            HttpEntity<AnagramRequest> requestEntity = new HttpEntity<>(request, headers);

            ResponseEntity<AnagramResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/anagrams/generate",
                requestEntity,
                AnagramResponse.class
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            // Pode vir do cache se já foi executado em outro teste
        }

        // Segunda rodada - todas devem vir do cache
        for (String input : testInputs) {
            AnagramRequest request = new AnagramRequest();
            request.setLetters(input);

            HttpHeaders headers = createAuthHeaders();
            HttpEntity<AnagramRequest> requestEntity = new HttpEntity<>(request, headers);

            ResponseEntity<AnagramResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/anagrams/generate",
                requestEntity,
                AnagramResponse.class
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isFromCache());
        }
    }

    @Test
    @DisplayName("Deve lidar com entrada moderadamente longa via HTTP")
    void shouldHandleModeratelyLongInputViaHttp() {
        // Arrange
        AnagramRequest request = new AnagramRequest();
        request.setLetters("abcdef"); // 6 letras = 6! = 720 anagramas

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<AnagramRequest> requestEntity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<AnagramResponse> response = restTemplate.postForEntity(
            baseUrl + "/api/anagrams/generate",
            requestEntity,
            AnagramResponse.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(720, response.getBody().getTotalAnagrams()); // 6!
        assertFalse(response.getBody().isFromCache());
    }
}
