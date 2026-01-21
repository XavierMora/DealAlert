package com.games_price_tracker.api.game;

import org.springframework.stereotype.Component;

import com.games_price_tracker.api.game.dtos.GameInfo;
import com.games_price_tracker.api.price.PriceMapper;
import com.games_price_tracker.api.price.dtos.PriceInfo;
import com.games_price_tracker.api.steam.AppSteam;
import com.games_price_tracker.api.steam.SteamUrlBuilder;

@Component
public class GameMapper {
    private final SteamUrlBuilder steamUrlBuilder;
    private final PriceMapper priceMapper;

    GameMapper(PriceMapper priceMapper, SteamUrlBuilder steamUrlBuilder){
        this.priceMapper = priceMapper;
        this.steamUrlBuilder = steamUrlBuilder;
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
            steamUrlBuilder.appImageUrl(game.getSteamId()).toString(),
            steamUrlBuilder.appUrl(game.getSteamId(), game.getName()).toString()
        );
    }
}
