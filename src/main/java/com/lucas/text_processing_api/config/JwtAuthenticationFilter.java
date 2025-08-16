package com.lucas.text_processing_api.config;

import com.lucas.text_processing_api.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro para autenticação JWT
 * 
 * Este filtro intercepta todas as requisições HTTP e valida
 * tokens JWT para autenticar usuários.
 * 
 * @author Lucas
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        try {
            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String username;
            
            // Verifica se o header Authorization está presente e começa com "Bearer "
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }
            
            // Extrai o token JWT (remove "Bearer " do início)
            jwt = authHeader.substring(7);
            username = jwtService.extractUsername(jwt);
            
            // Se o username foi extraído e não há autenticação atual
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                // Verifica se o token é válido para o usuário
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.debug("Usuário autenticado via JWT: {}", username);
                } else {
                    log.warn("Token JWT inválido para usuário: {}", username);
                }
            }
            
        } catch (Exception e) {
            log.error("Erro no filtro JWT: {}", e.getMessage());
            // Não interrompe a cadeia de filtros em caso de erro
        }
        
        filterChain.doFilter(request, response);
    }
}
