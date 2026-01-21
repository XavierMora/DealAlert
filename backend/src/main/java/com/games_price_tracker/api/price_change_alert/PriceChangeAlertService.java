package com.games_price_tracker.api.price_change_alert;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.games_price_tracker.api.account.Account;
import com.games_price_tracker.api.email.SendEmailService;
import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.game.GameService;
import com.games_price_tracker.api.price.dtos.ChangePriceResult;

@Service
public class PriceChangeAlertService {
    private final PriceChangeAlertRepository priceChangeAlertRepository;
    private final GameService gameService;
    private final SendEmailService sendEmailService;

    PriceChangeAlertService(PriceChangeAlertRepository priceChangeAlertRepository, GameService gameService, SendEmailService sendEmailService){
        this.priceChangeAlertRepository = priceChangeAlertRepository;
        this.gameService = gameService;
        this.sendEmailService = sendEmailService;
    }

    @Transactional
    public Optional<PriceChangeAlert> createAlert(Account account, Long gameId){
        Optional<PriceChangeAlert> optionalPriceAlert = priceChangeAlertRepository.findByAccountIdAndGameId(account.getId(), gameId);

        if(optionalPriceAlert.isPresent()) return Optional.empty();

        Game game = gameService.getGameById(gameId);

        PriceChangeAlert priceAlert = new PriceChangeAlert(account, game);
        return Optional.of(priceChangeAlertRepository.save(priceAlert));
    }

    public Page<PriceChangeAlert> getAlerts(Account account, Pageable pageable){
        return priceChangeAlertRepository.findAllByAccountId(account.getId(), pageable);
    }

    @Transactional
    public boolean deleteAlert(Long alertId, Account account){
        return priceChangeAlertRepository.deleteByIdAndAccountId(alertId, account.getId()) > 0;
    }

    public void notifyPriceChange(Game game, ChangePriceResult result){
        Optional<List<PriceChangeAlert>> optionalAlerts = priceChangeAlertRepository.findAllByGameId(game.getId());

        if(optionalAlerts.isEmpty()) return;

        List<String> emails = optionalAlerts.get().stream().map(alert -> alert.getAccount().getEmail()).toList();
        sendEmailService.priceChangeEmail(game, result, emails);
    }
}
