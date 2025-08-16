package com.lucas.text_processing_api.repository;

import com.lucas.text_processing_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório para operações de banco de dados com usuários
 * 
 * @author Lucas
 * @version 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Busca um usuário pelo username
     * 
     * @param username nome do usuário
     * @return Optional contendo o usuário se encontrado
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Verifica se existe um usuário com o username especificado
     * 
     * @param username nome do usuário
     * @return true se existir, false caso contrário
     */
    boolean existsByUsername(String username);
    
    /**
     * Verifica se existe um usuário com o email especificado
     * 
     * @param email email do usuário
     * @return true se existir, false caso contrário
     */
    boolean existsByEmail(String email);
}
