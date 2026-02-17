package com.games_price_tracker.api.game.dtos;

import com.games_price_tracker.api.game.Game;

public record GameData(Game game, Boolean isInPriceAlert){
}
