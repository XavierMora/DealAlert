package com.games_price_tracker.api.tracking.enqueue_games;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.games_price_tracker.api.game.GameService;
import com.games_price_tracker.api.steam.config.SteamApiProperties;
import com.games_price_tracker.api.tracking.enqueue_games.enums.CancelEnqueueResult;
import com.games_price_tracker.api.tracking.enqueue_games.enums.StartEnqueueResult;
import com.games_price_tracker.api.tracking.fetch_appdetails.FetchAppDetailsTasksHandler;

@Component
public class EnqueueGamesTaskHandler {
    private final TaskScheduler taskScheduler;
    private final EnqueueGamesNeedingPriceUpdate task;
    private final Duration delayBetweenRequests;
    private final Duration minIntervalGamePriceUpdate;
    private final int maxPagesPerEnqueue; 
    private final Logger log = LoggerFactory.getLogger(EnqueueGamesTaskHandler.class);
    private AtomicReference<ScheduledFuture<?>> currentTaskScheduled = new AtomicReference<>(null);

    public EnqueueGamesTaskHandler(GameService gameService, FetchAppDetailsTasksHandler fetchAppDetailsTasksHandler, TaskScheduler taskScheduler, SteamApiProperties steamApiProperties, @Value("${price.min-interval-update}") Duration minIntervalGamePriceUpdate){
        this.taskScheduler = taskScheduler;
        this.task = new EnqueueGamesNeedingPriceUpdate(gameService, fetchAppDetailsTasksHandler, this, steamApiProperties);
        this.delayBetweenRequests = steamApiProperties.getAppdetails().getDelayBetweenRequests();
        this.minIntervalGamePriceUpdate = minIntervalGamePriceUpdate;
        this.maxPagesPerEnqueue = steamApiProperties.getAppdetails().getMaxPagesPerEnqueue();
    }

    public StartEnqueueResult start(int gamesPerRequest){
        boolean canStart = currentTaskScheduled.get() == null;

        if(canStart){
            task.setGamesPerRequest(gamesPerRequest);
            currentTaskScheduled.set(taskScheduler.schedule(task, Instant.now()));
        }else{
            log.error("Can't start enqueue because there is already one scheduled");
        }

        return canStart ? StartEnqueueResult.STARTED : StartEnqueueResult.ENQUEUE_ALREADY_SCHEDULED;
    }

    public CancelEnqueueResult cancel(){
        if(currentTaskScheduled.get() == null){
            log.error("Cancel enqueue failed because no enqueue is scheduled");
            return CancelEnqueueResult.NO_ENQUEUE_SCHEDULED;
        }

        currentTaskScheduled.getAndUpdate(taskScheduled -> {
            boolean canceled = taskScheduled.cancel(false);
            
            if(canceled) log.info("Enqueue canceled");
            else log.error("Current enqueue couldn't be canceled");
            
            return canceled ? null : taskScheduled;
        });

        return currentTaskScheduled.get() == null ? CancelEnqueueResult.CANCELED : CancelEnqueueResult.CANCEL_FAILED;
    }

    public void nextExecution(boolean allGamesChecked){
        Instant schedulingTime;

        if(allGamesChecked){
            task.resetActualPage();

            ZonedDateTime dateEnqueue = LocalDateTime.now().plus(minIntervalGamePriceUpdate).withHour(6).withMinute(0).atZone(ZoneId.of("America/Argentina/Buenos_Aires"));

            schedulingTime = dateEnqueue.toInstant();

            log.info("Enqueue completed. Next: {}", dateEnqueue.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }else{
            long delayNextExecution = delayBetweenRequests.getSeconds()*maxPagesPerEnqueue+20;
            schedulingTime = Instant.now().plus(Duration.ofSeconds(delayNextExecution));
        }

        currentTaskScheduled.getAndUpdate((taskScheduled) -> {
            if(taskScheduled == null) return taskScheduled;

            log.info("Scheduling next enqueue task");
            return taskScheduler.schedule(task, schedulingTime);
        });
    }
}
    
