package com.games_price_tracker.api.game.dtos;

import com.games_price_tracker.api.price.dtos.PriceInfo;

public record GameInfo(Long id, Long SteamId, String name, PriceInfo priceInfo, String img, String steamUrl) {
    
}
