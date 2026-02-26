package com.games_price_tracker.api.tracking.update_game_price;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.price.PriceService;
import com.games_price_tracker.api.price.dtos.ChangePriceResult;
import com.games_price_tracker.api.price_change_alert.PriceChangeAlertService;
import com.games_price_tracker.api.steam.AppDetailsSteam;

public class UpdateGamesPricesTask implements Runnable {
    private final PriceService priceService;
    private final ArrayList<Game> games;
    private final ArrayList<AppDetailsSteam> apps;
    private final PriceChangeAlertService priceChangeAlertService;
    private final Logger log = LoggerFactory.getLogger(UpdateGamesPricesTask.class);

    public UpdateGamesPricesTask(PriceService priceService, List<Game> gamesList, List<AppDetailsSteam> appsList, PriceChangeAlertService priceChangeAlertService){
        this.priceService = priceService;
        this.apps = new ArrayList<AppDetailsSteam>(appsList);
        this.games = new ArrayList<Game>(gamesList);
        this.priceChangeAlertService = priceChangeAlertService;
    }

    @Override
    public void run() {
        AppDetailsSteam app;
        Game game;
        
        int pricesChanged=0, pricesNotChanged=0, pricesCreated=0;
        for (int i = 0; i < apps.size(); i++) {
            app = apps.get(i);
            game = games.get(i);

            if(app != null && app.getSuccess()){
                try {
                    Optional<ChangePriceResult> changeResult = priceService.changePrice(app.getInitialPrice(), app.getFinalPrice(), game);
                
                    if(changeResult.isPresent() && changeResult.get().oldPrice() != null){
                        pricesChanged++;
                        priceChangeAlertService.notifyPriceChange(game, changeResult.get());
                    }else if(changeResult.isEmpty()){
                        pricesNotChanged++;
                    }else{
                        pricesCreated++;
                    }
                } catch (Exception e) {
                    log.error("Failed to change price of game with id={}", game.getId(), e);
                }
            }
        }

        int appDetailsFails = apps.size()-(pricesChanged+pricesNotChanged+pricesCreated);
        log.info("Update games prices task completed. {} prices created, {} prices changed, {} unchanged, {} games appdetails failures", pricesCreated, pricesChanged, pricesNotChanged, appDetailsFails);
    }
}
