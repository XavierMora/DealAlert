package com.games_price_tracker.api.startup;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.games_price_tracker.api.game.GameRepository;

@Component
@Profile("dev")
public class StartupDataLoader implements ApplicationRunner {
    @Value("${startup.DB}")
    private boolean startupDB;
    private final GameRepository gameRepository;
    private final SteamAppListClient steamAppListClient;

    StartupDataLoader(GameRepository gameRepository, SteamAppListClient steamAppListClient){
        this.gameRepository = gameRepository;
        this.steamAppListClient = steamAppListClient;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if(startupDB && gameRepository.count() == 0){
            gameRepository.saveAll(steamAppListClient.getGamesFromAppListApi());
        }
    }
}
