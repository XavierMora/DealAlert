package com.games_price_tracker.api.tracking.enqueue_games;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final EnqueueGamesTaskHandler enqueueGamesTaskHandler;
    private final int maxPagesPerEnqueue; 
    private final int gamesPerRequest;
    private int actualPage = 0;
    private final Logger log = LoggerFactory.getLogger(EnqueueGamesNeedingPriceUpdate.class);

    EnqueueGamesNeedingPriceUpdate(GameService gameService, FetchAppDetailsTasksHandler fetchAppDetailsTasksHandler, EnqueueGamesTaskHandler enqueueGamesTaskHandler, SteamApiProperties steamApiProperties){
        this.enqueueGamesTaskHandler = enqueueGamesTaskHandler;
        this.gameService = gameService;
        this.fetchAppDetailsTasksHandler = fetchAppDetailsTasksHandler;
        this.gamesPerRequest = steamApiProperties.getAppdetails().getGamesPerRequest();
        this.maxPagesPerEnqueue = steamApiProperties.getAppdetails().getMaxPagesPerEnqueue();
    }

    @Override
    public void run() {
        Page<Game> page;

        do {
            Pageable pageable = PageRequest.of(actualPage, gamesPerRequest);
            page = gameService.getGames(pageable);
            log.info("Starting enqueue of {} games from page {}", page.getContent().size(), actualPage);
            
            if(page.getContent().isEmpty()){
                enqueueGamesTaskHandler.nextExecution(true);
                return;
            }

            List<Game> gamesToUpdate = page.getContent().stream().filter((game) -> gameService.gamePriceNeedsUpdate(game)).toList();
            log.info("{} games to update", gamesToUpdate.size());
            
            try {
                if(!gamesToUpdate.isEmpty()) fetchAppDetailsTasksHandler.createTask(gamesToUpdate); 
                   
                actualPage++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Failed to create FetchAppDetailsTask", e);
                enqueueGamesTaskHandler.nextExecution(false);
                return;
            }
        } while(!page.isLast() && actualPage%maxPagesPerEnqueue!=0);

        enqueueGamesTaskHandler.nextExecution(page.isLast() || actualPage%maxPagesPerEnqueue==0);
    }

    public void resetActualPage() {
        this.actualPage = 0;
    }
}