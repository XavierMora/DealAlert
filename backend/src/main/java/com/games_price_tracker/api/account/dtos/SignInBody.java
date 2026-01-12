package com.games_price_tracker.api.account.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignInBody(
    @NotBlank(message = "El email es obligatorio") 
    @Email(message = "Formato de email inválido") 
    String email
){   
    public SignInBody(String email){
        this.email = email==null ? null : email.trim();
    }
}
