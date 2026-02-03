package com.games_price_tracker.api.account.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyCodeBody(
    @NotBlank(message = "El email es obligatorio.") 
    @Email(message = "Formato de email inválido.") 
    String email, 
    
    @NotBlank(message = "El código es obligatorio.")
    @Size(min = 6, max = 6, message = "El código es inválido")
    String code
){   
}
