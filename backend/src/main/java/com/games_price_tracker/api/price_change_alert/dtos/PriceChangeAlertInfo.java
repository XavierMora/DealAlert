package com.games_price_tracker.api.price_change_alert.dtos;

import java.time.Instant;

import com.games_price_tracker.api.game.dtos.GameInfo;

public record PriceChangeAlertInfo(Long id, Instant createdAt, GameInfo game){
    
}
