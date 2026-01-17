package com.games_price_tracker.api.price_alert.dtos;

import jakarta.validation.constraints.NotNull;

public record CreatePriceAlertBody(
    @NotNull(message = "Game ID es obligatorio")
    Long gameId
){    
}
