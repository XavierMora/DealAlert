package com.games_price_tracker.api.account.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyCodeBody(
    @NotBlank(message = "El email es obligatorio.") 
    @Email(message = "Formato de email inválido.") 
    String email, 
    
    @NotBlank(message = "El código es obligatorio.")
    String code
){   
}
