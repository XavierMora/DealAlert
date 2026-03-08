package com.games_price_tracker.api.admin;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger log = LoggerFactory.getLogger(AdminService.class);

    AdminService(GameService gameService, SteamClient steamClient, TaskExecutor saveGamesTaskExecutor, EnqueueGamesTaskHandler enqueueGamesTaskHandler){
        this.enqueueGamesTaskHandler = enqueueGamesTaskHandler;
        this.saveGamesTaskExecutor = saveGamesTaskExecutor;
        this.gameService = gameService;
        this.steamClient = steamClient;
    }

    public void saveAppList(int maxGames) {
        Optional<List<AppSteam>> optionalGames = steamClient.getAppList(maxGames);

        if(optionalGames.isEmpty()) return;

        List<AppSteam> games = optionalGames.get();

        final int sizeGamesBlock=100;
        final AtomicInteger completedTasks = new AtomicInteger(0);
        final AtomicInteger failedTasks = new AtomicInteger(0);
        final int totalTasks = (int) Math.ceil((double)games.size()/sizeGamesBlock);

        for (int i=0; i<games.size(); i+=sizeGamesBlock) {
            final int start = i;
            final int end = games.size() < i+sizeGamesBlock ? games.size() : i+sizeGamesBlock;

            saveGamesTaskExecutor.execute(() -> {
                try {
                    gameService.saveGames(games.subList(start, end));                    
                } catch (Exception e) {
                    failedTasks.incrementAndGet();
                }finally{                    
                    if(completedTasks.incrementAndGet() == totalTasks){
                        if(startEnqueueGames){
                            startEnqueueGames = false;
                            enqueueGamesTaskHandler.start();
                        }
                        
                        log.info("{} of {} save games tasks failed", failedTasks.get(), totalTasks);
                    }
                }
            });
        }
    }
}
