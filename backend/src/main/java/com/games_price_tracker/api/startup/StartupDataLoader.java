package com.games_price_tracker.api.startup;

import java.time.Instant;
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
import com.games_price_tracker.api.tracking.enqueue_games.EnqueueGamesTaskHandler;

@Component
@Profile("dev")
public class StartupDataLoader implements ApplicationRunner {
    @Value("${startup.DB}")
    private boolean startupDB;
    private final GameRepository gameRepository;
    private final SteamClient steamClient;
    private final GameMapper gameMapper;
    private final EnqueueGamesTaskHandler gamePriceCheckScheduler;

    StartupDataLoader(GameRepository gameRepository, SteamClient steamClient, GameMapper gameMapper, EnqueueGamesTaskHandler gamePriceCheckScheduler){
        this.gameRepository = gameRepository;
        this.steamClient = steamClient;
        this.gameMapper = gameMapper;
        this.gamePriceCheckScheduler = gamePriceCheckScheduler;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if(startupDB){
            List<Game> games = steamClient.getAppList().stream().map((appSteam) -> gameMapper.fromAppSteam(appSteam)).toList();
            
            gameRepository.saveAll(games);
        }

        gamePriceCheckScheduler.nextExecution(Instant.now());
    }
}
