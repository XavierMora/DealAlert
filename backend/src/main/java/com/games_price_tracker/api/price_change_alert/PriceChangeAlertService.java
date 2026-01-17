package com.games_price_tracker.api.price_change_alert;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.games_price_tracker.api.account.Account;
import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.game.GameService;

@Service
public class PriceChangeAlertService {
    private final PriceChangeAlertRepository priceAlertRepository;
    private final GameService gameService;

    PriceChangeAlertService(PriceChangeAlertRepository priceAlertRepository, GameService gameService){
        this.priceAlertRepository = priceAlertRepository;
        this.gameService = gameService;
    }

    @Transactional
    public boolean createPriceAlert(Account account, Long gameId){
        Optional<PriceChangeAlert> optionalPriceAlert = priceAlertRepository.findByAccountIdAndGameId(account.getId(), gameId);

        if(optionalPriceAlert.isPresent()) return false;

        Game game = gameService.getGameById(gameId);

        PriceChangeAlert priceAlert = new PriceChangeAlert(account, game);
        priceAlertRepository.save(priceAlert);
        return true;
    }
}
