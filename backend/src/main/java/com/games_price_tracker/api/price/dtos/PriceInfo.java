package com.games_price_tracker.api.price.dtos;

import java.time.Instant;

public record PriceInfo(Long id, int initialPrice, int finalPrice, int discount, Instant lastUpdate) {
    
}
