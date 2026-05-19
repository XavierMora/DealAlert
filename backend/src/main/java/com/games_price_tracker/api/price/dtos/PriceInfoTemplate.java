package com.games_price_tracker.api.price.dtos;

public record PriceInfoTemplate(int initialPrice, String initialPriceFormatted, int finalPrice, String finalPriceFormatted, int discount) {
    
}
