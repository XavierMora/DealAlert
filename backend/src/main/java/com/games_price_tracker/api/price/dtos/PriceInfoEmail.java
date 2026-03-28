package com.games_price_tracker.api.price.dtos;

public record PriceInfoEmail(int initialPrice, String initialPriceFormatted, int finalPrice, String finalPriceFormatted, int discount) {
    
}
