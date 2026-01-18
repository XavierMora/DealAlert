package com.games_price_tracker.api.price_change_alert;

import org.springframework.stereotype.Component;

import com.games_price_tracker.api.game.GameMapper;
import com.games_price_tracker.api.price_change_alert.dtos.PriceChangeAlertInfo;

@Component
public class PriceChangeAlertMapper {
    private final GameMapper gameMapper;

    PriceChangeAlertMapper(GameMapper gameMapper){
        this.gameMapper = gameMapper;
    }

    public PriceChangeAlertInfo toPriceChangeAlertInfo(PriceChangeAlert alert){
        return new PriceChangeAlertInfo(
            alert.getId(),
            alert.getCreatedAt(), 
            gameMapper.toGameInfo(alert.getGame())
        );
    }
}
