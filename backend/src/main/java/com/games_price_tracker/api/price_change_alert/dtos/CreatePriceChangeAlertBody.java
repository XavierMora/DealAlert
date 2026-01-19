package com.games_price_tracker.api.price_change_alert.dtos;

import jakarta.validation.constraints.NotNull;

public record CreatePriceChangeAlertBody(
    @NotNull(message = "gameId es obligatorio.")
    Long gameId
){    
}
