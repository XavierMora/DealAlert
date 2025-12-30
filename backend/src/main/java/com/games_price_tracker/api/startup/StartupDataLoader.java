package com.games_price_tracker.api.startup;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.game.GameMapper;
import com.games_price_tracker.api.game.GameRepository;
import com.games_price_tracker.api.steam.SteamClient;

@Component
@Profile("dev")
public class StartupDataLoader implements ApplicationRunner {
    @Value("${startup.DB}")
    private boolean startupDB;
    private final GameRepository gameRepository;
    private final SteamClient steamClient;
    private final GameMapper gameMapper;

    StartupDataLoader(GameRepository gameRepository, SteamClient steamClient, GameMapper gameMapper){
        this.gameRepository = gameRepository;
        this.steamClient = steamClient;
        this.gameMapper = gameMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if(startupDB){
            List<Game> games = steamClient.getAppList().stream().map((appSteam) -> gameMapper.fromAppSteam(appSteam)).toList();
            
            gameRepository.saveAll(games);
        }
    }
}
