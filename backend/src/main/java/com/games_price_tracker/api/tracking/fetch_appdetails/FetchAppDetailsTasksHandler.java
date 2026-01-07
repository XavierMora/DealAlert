package com.games_price_tracker.api.tracking.fetch_appdetails;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.steam.SteamApiProperties;
import com.games_price_tracker.api.steam.SteamClient;
import com.games_price_tracker.api.tracking.update_game_price.UpdateGamesPricesTasksHandler;

@Component
@Profile("!test")
public class FetchAppDetailsTasksHandler {
    private final SteamClient steamClient;
    private final TaskScheduler taskScheduler;
    private final ArrayBlockingQueue<FetchAppDetailsTask> pendingTasks;
    private final UpdateGamesPricesTasksHandler updateGamesPricesTasksHandler;
    private final Duration delayBetweenRequests;
    private final AtomicBoolean canStartTask = new AtomicBoolean(true);

    FetchAppDetailsTasksHandler(SteamClient steamClient, TaskScheduler taskScheduler, UpdateGamesPricesTasksHandler updateGamesPricesTasksHandler, SteamApiProperties steamApiProperties){
        this.steamClient = steamClient;
        this.taskScheduler = taskScheduler;
        this.updateGamesPricesTasksHandler = updateGamesPricesTasksHandler;
        this.delayBetweenRequests = steamApiProperties.getAppdetails().getDelayBetweenRequests();
        this.pendingTasks = new ArrayBlockingQueue<FetchAppDetailsTask>(steamApiProperties.getAppdetails().getMaxPagesPerEnqueue()*2);
    }

    public void createTask(List<Game> games) throws InterruptedException{
        pendingTasks.put(new FetchAppDetailsTask(games, steamClient, updateGamesPricesTasksHandler, this));
    
        startTask();
    }
    
    private void startTask(){
        if(pendingTasks.isEmpty() || !canStartTask.compareAndExchange(true, false)) return;
                
        FetchAppDetailsTask fetchAppDetailsTask = pendingTasks.poll();
        
        Instant time = Instant.now().plus(delayBetweenRequests);
        
        taskScheduler.schedule(fetchAppDetailsTask, time);
    }

    public void nextTask(FetchAppDetailsTask previousTask){        
        canStartTask.set(true);
        startTask();

        if(previousTask.getSuccess()) return; 
        
        pendingTasks.offer(new FetchAppDetailsTask(previousTask.getGames(), steamClient, updateGamesPricesTasksHandler, this)); // Se intenta poner de vuelta la tarea en caso de fallo.
    }
}
