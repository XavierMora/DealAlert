package com.games_price_tracker.api.tracking.enqueue_games;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;

import com.games_price_tracker.api.game.GameService;
import com.games_price_tracker.api.steam.config.SteamApiProperties;
import com.games_price_tracker.api.tracking.fetch_appdetails.FetchAppDetailsTasksHandler;

public class EnqueueGamesTaskHandler {
    private final TaskScheduler taskScheduler;
    private final EnqueueGamesNeedingPriceUpdate task;
    private final Duration delayBetweenRequests;
    private final Duration minIntervalGamePriceUpdate;
    private final int maxPagesPerEnqueue; 
    private final Logger log = LoggerFactory.getLogger(EnqueueGamesTaskHandler.class);

    public EnqueueGamesTaskHandler(GameService gameService, FetchAppDetailsTasksHandler fetchAppDetailsTasksHandler, TaskScheduler taskScheduler, SteamApiProperties steamApiProperties, @Value("${price.min-interval-update}") Duration minIntervalGamePriceUpdate){
        this.taskScheduler = taskScheduler;
        this.task = new EnqueueGamesNeedingPriceUpdate(gameService, fetchAppDetailsTasksHandler, this, steamApiProperties);
        this.delayBetweenRequests = steamApiProperties.getAppdetails().getDelayBetweenRequests();
        this.minIntervalGamePriceUpdate = minIntervalGamePriceUpdate;
        this.maxPagesPerEnqueue = steamApiProperties.getAppdetails().getMaxPagesPerEnqueue();
    }

    public void start(){
        taskScheduler.schedule(task, Instant.now());
    }

    public void nextExecution(boolean allGamesChecked){
        if(allGamesChecked){
            task.resetActualPage();
            ZonedDateTime dateEnqueue = LocalDateTime.now().plus(minIntervalGamePriceUpdate).withHour(6).withMinute(0).atZone(ZoneId.of("America/Argentina/Buenos_Aires"));
            taskScheduler.schedule(task, dateEnqueue.toInstant());

            log.info("Enqueue completed. Next: {}", dateEnqueue.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }else{
            long delayNextExecution = delayBetweenRequests.getSeconds()*maxPagesPerEnqueue+20;
            taskScheduler.schedule(task, Instant.now().plus(Duration.ofSeconds(delayNextExecution)));
        }
    }
}
    
