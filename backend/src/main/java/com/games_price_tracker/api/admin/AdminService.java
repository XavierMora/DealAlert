package com.games_price_tracker.api.admin;

import java.time.Instant;
import java.util.List;

import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.games_price_tracker.api.game.GameService;
import com.games_price_tracker.api.steam.AppSteam;
import com.games_price_tracker.api.steam.SteamClient;
import com.games_price_tracker.api.tracking.enqueue_games.EnqueueGamesTaskHandler;

@Service
public class AdminService {
    private final SteamClient steamClient;
    private final TaskExecutor saveGamesTaskExecutor;
    private final GameService gameService;
    private final EnqueueGamesTaskHandler enqueueGamesTaskHandler;
    private boolean startEnqueueGames = true;

    AdminService(GameService gameService, SteamClient steamClient, TaskExecutor saveGamesTaskExecutor, EnqueueGamesTaskHandler enqueueGamesTaskHandler){
        this.enqueueGamesTaskHandler = enqueueGamesTaskHandler;
        this.saveGamesTaskExecutor = saveGamesTaskExecutor;
        this.gameService = gameService;
        this.steamClient = steamClient;
    }

    public void saveAppList(int maxGames) {
        List<AppSteam> games = steamClient.getAppList(maxGames);

        final int sizeGamesBlock=100;
        for (int i=0; i<games.size(); i+=sizeGamesBlock) {
            final int start = i;
            final int end = games.size() < i+sizeGamesBlock ? games.size() : i+sizeGamesBlock;

            saveGamesTaskExecutor.execute(() -> {
                try {
                    gameService.saveGames(games.subList(start, end));                    
                }finally{
                    if(startEnqueueGames && end == games.size()){
                        enqueueGamesTaskHandler.nextExecution(Instant.now());
                        startEnqueueGames = false;
                    }
                }
            });
        }
    }
}
