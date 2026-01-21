package com.games_price_tracker.api.tracking.update_game_price;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        
        for (int i = 0; i < apps.size(); i++) {
            app = apps.get(i);
            game = games.get(i);

            // Si hubo exito en la request obteniendo la app: actualizar el precio
            if(app.getSuccess()){
                Optional<ChangePriceResult> changeResult = priceService.changePrice(app.getInitialPrice(), app.getFinalPrice(), game);
            
                if(changeResult.isPresent()) priceChangeAlertService.notifyPriceChange(game, changeResult.get());
            }
        }
    }
}
