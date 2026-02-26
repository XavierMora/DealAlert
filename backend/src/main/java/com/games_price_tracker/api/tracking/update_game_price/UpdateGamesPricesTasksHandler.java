package com.games_price_tracker.api.tracking.update_game_price;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.price.PriceService;
import com.games_price_tracker.api.price_change_alert.PriceChangeAlertService;
import com.games_price_tracker.api.steam.AppDetailsSteam;

@Component
public class UpdateGamesPricesTasksHandler {
    private final PriceService priceService;
    private final TaskExecutor taskExecutor;
    private final PriceChangeAlertService priceChangeAlertService;
    private final Logger log = LoggerFactory.getLogger(UpdateGamesPricesTasksHandler.class);

    public UpdateGamesPricesTasksHandler(PriceService priceService, TaskExecutor taskExecutor, PriceChangeAlertService priceChangeAlertService){
        this.priceService = priceService;
        this.taskExecutor = taskExecutor;
        this.priceChangeAlertService = priceChangeAlertService;
    }
    
    public void createTask(List<Game> gamesList, List<AppDetailsSteam> appsList){
        taskExecutor.execute(new UpdateGamesPricesTask(priceService, gamesList, appsList, priceChangeAlertService));
        log.info("Update games prices task created");
    }
}
