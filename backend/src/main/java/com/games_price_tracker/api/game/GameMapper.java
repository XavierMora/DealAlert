package com.games_price_tracker.api.game;

import org.springframework.stereotype.Component;

import com.games_price_tracker.api.game.dtos.GameData;
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

    private GameInfo createGameInfo(Game game, Boolean isInPriceAlert){
        PriceInfo priceInfo = null;
        
        if(game.getPrice() != null) priceInfo = priceMapper.toPriceInfo(game.getPrice());
        
        return new GameInfo(
            game.getId(), 
            game.getSteamId(),
            game.getName(),
            priceInfo,
            steamUrlBuilder.appImageUrl(game.getSteamId()).toString(),
            steamUrlBuilder.appUrl(game.getSteamId(), game.getName()).toString(),
            isInPriceAlert
        );
    }

    public GameInfo toGameInfo(Game game){
        return createGameInfo(game, null);
    }

    public GameInfo toGameInfo(GameData gameData){
        return createGameInfo(gameData.game(), gameData.isInPriceAlert());
    }
}
