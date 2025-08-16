package com.lucas.text_processing_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO para requisições de geração de anagramas
 * 
 * Esta classe representa a entrada do usuário para geração de anagramas,
 * incluindo validações para garantir que apenas letras sejam aceitas.
 * 
 * @author Lucas
 * @version 1.0
 */
@Data
public class AnagramRequest {

    /**
     * String contendo as letras para geração de anagramas
     * Deve conter apenas letras (maiúsculas ou minúsculas)
     */
    @NotBlank(message = "As letras não podem estar vazias")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Apenas letras são permitidas")
    private String letters;
}
