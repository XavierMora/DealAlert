package com.games_price_tracker.api.tracking.enqueue_games;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;

import com.games_price_tracker.api.game.GameService;
import com.games_price_tracker.api.steam.SteamApiProperties;
import com.games_price_tracker.api.tracking.fetch_appdetails.FetchAppDetailsTasksHandler;

public class EnqueueGamesTaskHandler {
    private final TaskScheduler taskScheduler;
    private final EnqueueGamesNeedingPriceUpdate task;

    public EnqueueGamesTaskHandler(GameService gameService, FetchAppDetailsTasksHandler fetchAppDetailsTasksHandler, TaskScheduler taskScheduler, SteamApiProperties steamApiProperties, @Value("${price.min-interval-update}") Duration priceMinIntervalUpdate){
        this.taskScheduler = taskScheduler;
        this.task = new EnqueueGamesNeedingPriceUpdate(gameService, fetchAppDetailsTasksHandler, this, steamApiProperties, priceMinIntervalUpdate);
    }

    // La primera ejecucion la hace el startup
    public void nextExecution(Instant time){
        taskScheduler.schedule(task, time);
    }
}
    
