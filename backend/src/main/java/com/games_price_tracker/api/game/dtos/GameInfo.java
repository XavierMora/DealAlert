package com.games_price_tracker.api.game.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.games_price_tracker.api.price.dtos.PriceInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GameInfo(Long id, Long steamId, String name, PriceInfo priceInfo, String img, String steamUrl, Boolean isInPriceAlert) {
    
}
