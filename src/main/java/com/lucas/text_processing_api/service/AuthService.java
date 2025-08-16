package com.lucas.text_processing_api.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.lucas.text_processing_api.dto.AuthRequest;
import com.lucas.text_processing_api.dto.AuthResponse;
import com.lucas.text_processing_api.entity.User;
import com.lucas.text_processing_api.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço de autenticação e gerenciamento de usuários
 * 
 * @author Lucas
 * @version 2.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    /**
     * Autentica um usuário e retorna um token JWT
     * 
     * @param request dados de autenticação
     * @return resposta com token JWT
     */
    public AuthResponse authenticate(AuthRequest request) {
        log.info("Tentativa de autenticação para usuário: {}", request.getUsername());
        
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Senha incorreta para usuário: {}", request.getUsername());
            throw new RuntimeException("Credenciais inválidas");
        }
        
        if (!user.isActive()) {
            log.warn("Usuário inativo: {}", request.getUsername());
            throw new RuntimeException("Usuário inativo");
        }
        
        String token = jwtService.generateToken(user);
        log.info("Usuário autenticado com sucesso: {}", request.getUsername());
        
        return new AuthResponse(token, "Bearer", user.getUsername(), user.getRole(), "Login realizado com sucesso");
    }
    
    /**
     * Cria um novo usuário
     * 
     * @param request dados do usuário
     * @return resposta com token JWT
     */
    public AuthResponse createUser(AuthRequest request) {
        log.info("Criando novo usuário: {}", request.getUsername());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Nome de usuário já existe");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getUsername() + "@example.com");
        user.setFullName(request.getUsername());
        user.setActive(true);
        user.setRole("USER");
        
        userRepository.save(user);
        log.info("Usuário criado com sucesso: {}", request.getUsername());
        
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, "Bearer", user.getUsername(), user.getRole(), "Usuário criado com sucesso");
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Carregando usuário: {}", username);
        
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }
    
    /**
     * Inicializa usuários de teste na aplicação
     */
    @PostConstruct
    public void initializeTestUsers() {
        log.info("Inicializando usuários de teste...");
        
        // Verificar se já existem usuários
        if (userRepository.count() > 0) {
            log.info("Usuários já existem, pulando inicialização");
            return;
        }
        
        // Criar usuário admin
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@example.com");
        admin.setFullName("Administrador");
        admin.setActive(true);
        admin.setRole("ADMIN");
        
        // Criar usuário normal
        User user = new User();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("user123"));
        user.setEmail("user@example.com");
        user.setFullName("Usuário Normal");
        user.setActive(true);
        user.setRole("USER");
        
        // Criar usuário teste
        User test = new User();
        test.setUsername("test");
        test.setPassword(passwordEncoder.encode("test123"));
        test.setEmail("test@example.com");
        test.setFullName("Usuário Teste");
        test.setActive(true);
        test.setRole("USER");
        
        userRepository.saveAll(List.of(admin, user, test));
        log.info("Usuários de teste criados com sucesso: admin, user, test");
    }
}
