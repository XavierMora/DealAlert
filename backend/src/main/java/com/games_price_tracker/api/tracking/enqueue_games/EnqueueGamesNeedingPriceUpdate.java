package com.games_price_tracker.api.tracking.enqueue_games;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.game.GameService;
import com.games_price_tracker.api.steam.config.SteamApiProperties;
import com.games_price_tracker.api.tracking.fetch_appdetails.FetchAppDetailsTasksHandler;

public class EnqueueGamesNeedingPriceUpdate implements Runnable{
    private final GameService gameService;
    private final FetchAppDetailsTasksHandler fetchAppDetailsTasksHandler;
    private final EnqueueGamesTaskHandler gamePriceCheckScheduler;
    private final int maxPagesPerEnqueue; 
    private final int gamesPerRequest;
    private final Duration delayBetweenRequests;
    private final Duration minIntervalGamePriceUpdate;
    private int actualPage = 0;

    EnqueueGamesNeedingPriceUpdate(GameService gameService, FetchAppDetailsTasksHandler fetchAppDetailsTasksHandler, EnqueueGamesTaskHandler gamePriceCheckScheduler, SteamApiProperties steamApiProperties, Duration minIntervalGamePriceUpdate){
        this.gamePriceCheckScheduler = gamePriceCheckScheduler;
        this.gameService = gameService;
        this.fetchAppDetailsTasksHandler = fetchAppDetailsTasksHandler;
        this.gamesPerRequest = steamApiProperties.getAppdetails().getGamesPerRequest();
        this.delayBetweenRequests = steamApiProperties.getAppdetails().getDelayBetweenRequests();
        this.maxPagesPerEnqueue = steamApiProperties.getAppdetails().getMaxPagesPerEnqueue();
        this.minIntervalGamePriceUpdate = minIntervalGamePriceUpdate;
    }

    @Override
    public void run() {
        Page<Game> page;
        int attempts=0;

        do {
            Pageable pageable = PageRequest.of(actualPage, gamesPerRequest);
            page = gameService.getGames(pageable);
            
            List<Game> gamesToUpdate = page.getContent().stream().filter((game) -> gameService.gamePriceNeedsUpdate(game)).toList();
            
            try {
                if(!gamesToUpdate.isEmpty()) fetchAppDetailsTasksHandler.createTask(gamesToUpdate);    
                actualPage++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                attempts++;
                if(attempts == 3) actualPage++;
            }
        } while(!page.isLast() && actualPage%maxPagesPerEnqueue!=0);

        Instant timeNextExecution = getTimeNextExecution(page.isLast());
        gamePriceCheckScheduler.nextExecution(timeNextExecution);
    }

    private Instant getTimeNextExecution(boolean allGamesChecked){
        if(allGamesChecked){
            actualPage = 0;
            return Instant.now().plus(minIntervalGamePriceUpdate);
        }else{
            long delayNextExecution = delayBetweenRequests.getSeconds()*maxPagesPerEnqueue+20;
            return Instant.now().plus(delayNextExecution, ChronoUnit.SECONDS);
        }
    }
}