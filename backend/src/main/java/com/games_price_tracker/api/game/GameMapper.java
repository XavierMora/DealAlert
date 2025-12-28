package com.games_price_tracker.api.game;

import org.springframework.stereotype.Component;

import com.games_price_tracker.api.game.dtos.GameInfo;
import com.games_price_tracker.api.price.PriceMapper;
import com.games_price_tracker.api.price.dtos.PriceInfo;
import com.games_price_tracker.api.steam.AppSteam;

@Component
public class GameMapper {
    private final PriceMapper priceMapper;

    GameMapper(PriceMapper priceMapper){
        this.priceMapper = priceMapper;
    }
    
    public Game fromAppSteam(AppSteam appSteam){
        return new Game(
            appSteam.getSteamId(), 
            appSteam.getName()
        );
    }

    public GameInfo toGameInfo(Game game){
        PriceInfo priceInfo = null;
        
        if(game.getPrice() != null) priceInfo = priceMapper.toPriceInfo(game.getPrice());
        
        return new GameInfo(
            game.getId(), 
            game.getSteamId(),
            game.getName(),
            priceInfo,
            builderUrlImg(game.getSteamId())
        );
    }

    private String builderUrlImg(Long steamId){
        return String.format("https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/%d/header.jpg", steamId);
    }
}
