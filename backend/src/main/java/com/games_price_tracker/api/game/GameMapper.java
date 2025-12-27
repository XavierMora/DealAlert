package com.games_price_tracker.api.game;

import org.springframework.stereotype.Component;

import com.games_price_tracker.api.steam.AppSteam;

@Component
public class GameMapper {
    GameMapper(){}
    
    public Game fromAppSteam(AppSteam appSteam){
        return new Game(
            appSteam.getSteamId(), 
            appSteam.getName()
        );
    }
}
