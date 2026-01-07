package com.games_price_tracker.api.tracking.update_game_price;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.price.PriceService;
import com.games_price_tracker.api.steam.AppDetailsSteam;

@Component
@Profile("!test")
public class UpdateGamesPricesTasksHandler {
    private final PriceService priceService;
    private final TaskExecutor taskExecutor;
    
    public UpdateGamesPricesTasksHandler(PriceService priceService, TaskExecutor taskExecutor){
        this.priceService = priceService;
        this.taskExecutor = taskExecutor;
    }
    
    public void createTask(List<Game> gamesList, List<AppDetailsSteam> appsList){
        taskExecutor.execute(new UpdateGamesPricesTask(priceService, gamesList, appsList));
    }
}
