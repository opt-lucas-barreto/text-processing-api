package com.lucas.text_processing_api.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Serviço para geração e validação de tokens JWT
 * 
 * @author Lucas
 * @version 1.0
 */
@Service
@Slf4j
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    /**
     * Gera um token JWT para o usuário
     * 
     * @param userDetails detalhes do usuário
     * @return token JWT gerado
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }
    
    /**
     * Gera um token JWT com claims extras
     * 
     * @param extraClaims claims extras para incluir no token
     * @param userDetails detalhes do usuário
     * @return token JWT gerado
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            
            return Jwts.builder()
                    .claims(extraClaims)
                    .subject(userDetails.getUsername())
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(key, Jwts.SIG.HS256)
                    .compact();
        } catch (Exception e) {
            log.error("Erro ao gerar token JWT: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar token JWT", e);
        }
    }
    
    /**
     * Extrai o username do token JWT
     * 
     * @param token token JWT
     * @return username extraído
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extrai a data de expiração do token JWT
     * 
     * @param token token JWT
     * @return data de expiração
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Extrai um claim específico do token JWT
     * 
     * @param token token JWT
     * @param claimsResolver função para resolver o claim
     * @return valor do claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Extrai todos os claims do token JWT
     * 
     * @param token token JWT
     * @return todos os claims
     */
    private Claims extractAllClaims(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.error("Erro ao extrair claims do token JWT: {}", e.getMessage());
            throw new JwtException("Token JWT inválido");
        }
    }
    
    /**
     * Verifica se o token JWT é válido para o usuário
     * 
     * @param token token JWT
     * @param userDetails detalhes do usuário
     * @return true se válido, false caso contrário
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    /**
     * Verifica se o token JWT está expirado
     * 
     * @param token token JWT
     * @return true se expirado, false caso contrário
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * Valida se o token JWT é válido (sem verificar usuário específico)
     * 
     * @param token token JWT
     * @return true se válido, false caso contrário
     */
    public boolean isTokenValid(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return !isTokenExpired(token);
        } catch (JwtException e) {
            log.warn("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }
}
