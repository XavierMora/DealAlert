package com.games_price_tracker.api.price_change_alert;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.games_price_tracker.api.account.Account;
import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.game.GameService;

@Service
public class PriceChangeAlertService {
    private final PriceChangeAlertRepository priceChangeAlertRepository;
    private final GameService gameService;

    PriceChangeAlertService(PriceChangeAlertRepository priceChangeAlertRepository, GameService gameService){
        this.priceChangeAlertRepository = priceChangeAlertRepository;
        this.gameService = gameService;
    }

    @Transactional
    public boolean createAlert(Account account, Long gameId){
        Optional<PriceChangeAlert> optionalPriceAlert = priceChangeAlertRepository.findByAccountIdAndGameId(account.getId(), gameId);

        if(optionalPriceAlert.isPresent()) return false;

        Game game = gameService.getGameById(gameId);

        PriceChangeAlert priceAlert = new PriceChangeAlert(account, game);
        priceChangeAlertRepository.save(priceAlert);
        return true;
    }

    public Page<PriceChangeAlert> getAlerts(Account account, Pageable pageable){
        return priceChangeAlertRepository.findAllByAccountId(account.getId(), pageable);
    }

    @Transactional
    public boolean deleteAlert(Long alertId, Account account){
        return priceChangeAlertRepository.deleteByIdAndAccountId(alertId, account.getId()) > 0;
    }
}
