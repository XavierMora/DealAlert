package com.games_price_tracker.api.tracking.fetch_appdetails;

import java.util.List;

import org.springframework.web.client.ResourceAccessException;

import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.steam.AppDetailsSteam;
import com.games_price_tracker.api.steam.SteamClient;
import com.games_price_tracker.api.tracking.update_game_price.UpdateGamesPricesTasksHandler;

public class FetchAppDetailsTask implements Runnable{
    private final List<Game> games;
    private final SteamClient steamClient;
    private final FetchAppDetailsTasksHandler fetchAppDetailsTasksHandler;
    private final UpdateGamesPricesTasksHandler updateGamesPricesTasksHandler;
    private boolean success = true;

    public FetchAppDetailsTask(List<Game> games, SteamClient steamClient, UpdateGamesPricesTasksHandler updateGamesPricesTasksHandler, FetchAppDetailsTasksHandler fetchAppDetailsTasksHandler){
        this.games = games;
        this.steamClient = steamClient;
        this.fetchAppDetailsTasksHandler = fetchAppDetailsTasksHandler;
        this.updateGamesPricesTasksHandler = updateGamesPricesTasksHandler;
    }

    public List<Game> getGames() {
        return games;
    }

    public boolean getSuccess(){
        return success;
    }

    @Override
    public void run() {
        List<Long> steamIds = games.stream().map(game -> game.getSteamId()).toList();
        
        try {
            List<AppDetailsSteam> appsDetailsSteam = steamClient.getMultipleAppDetails(steamIds); // se hace la request
            updateGamesPricesTasksHandler.createTask(games, appsDetailsSteam);
        } catch (ResourceAccessException e) { // puede producirse por error de timeout en request
            success = false;
        }finally{
            fetchAppDetailsTasksHandler.nextTask(this);
        }
    }    
}
