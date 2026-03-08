package com.games_price_tracker.api.price_change_alert;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.games_price_tracker.api.account.Account;
import com.games_price_tracker.api.account.AccountService;
import com.games_price_tracker.api.common.exceptions.ResourceNotFoundException;
import com.games_price_tracker.api.email.SendEmailService;
import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.game.GameService;
import com.games_price_tracker.api.price.dtos.ChangePriceResult;

@Service
public class PriceChangeAlertService {
    private final PriceChangeAlertRepository priceChangeAlertRepository;
    private final GameService gameService;
    private final SendEmailService sendEmailService;
    private final PriceChangeAlertCacheService priceChangeAlertCacheService;
    private final Logger log = LoggerFactory.getLogger(PriceChangeAlertService.class);
    private final AccountService accountService;

    PriceChangeAlertService(PriceChangeAlertRepository priceChangeAlertRepository, GameService gameService, SendEmailService sendEmailService, PriceChangeAlertCacheService priceChangeAlertCacheService, AccountService accountService){
        this.accountService = accountService;
        this.priceChangeAlertRepository = priceChangeAlertRepository;
        this.gameService = gameService;
        this.sendEmailService = sendEmailService;
        this.priceChangeAlertCacheService = priceChangeAlertCacheService;
    }

    @Transactional
    public Optional<PriceChangeAlert> createAlert(Account account, Long gameId){
        accountService.verifyRateLimit(account.getEmail());
        Optional<PriceChangeAlert> optionalPriceAlert = priceChangeAlertRepository.findByAccountIdAndGameId(account.getId(), gameId);

        if(optionalPriceAlert.isPresent()) return Optional.empty();

        Game game = gameService.getGameById(gameId);

        PriceChangeAlert priceAlert = new PriceChangeAlert(account, game);
        Optional<PriceChangeAlert> newAlert = Optional.of(priceChangeAlertRepository.save(priceAlert));

        priceChangeAlertCacheService.evictAlertsCache(account.getId());

        return newAlert;
    }

    public Page<PriceChangeAlert> getAlerts(Account account, Pageable pageable){
        accountService.verifyRateLimit(account.getEmail());
        return priceChangeAlertCacheService.getAlerts(account.getId(), pageable);
    }

    @Transactional
    public void deleteAlert(Long gameId, Account account) throws ResourceNotFoundException{
        accountService.verifyRateLimit(account.getEmail());
        boolean alertDeleted = priceChangeAlertRepository.deleteByAccountIdAndGameId(account.getId(), gameId) > 0;
        
        if(!alertDeleted) throw new ResourceNotFoundException("La alerta no existe.");
        
        priceChangeAlertCacheService.evictAlertsCache(account.getId());
    }

    public boolean notifyPriceChange(Game game, ChangePriceResult result){
        Optional<List<PriceChangeAlert>> optionalAlerts = priceChangeAlertRepository.findAllByGameId(game.getId());

        if(optionalAlerts.isEmpty()){
            log.info("No alerts for the game with id={}", game.getId());
            return false;
        }

        if(
            result.newPrice().initialPrice() > result.newPrice().finalPrice()
        ){
            List<String> emails = optionalAlerts.get().stream().map(alert -> alert.getAccount().getEmail()).toList();
        
            try {
                sendEmailService.dealEmail(game, result, emails);
                return true;
            } catch (Exception e) {
                log.error("Failed to create deal notification for the game with id={}", game.getId(), e);
                return false;
            }
        }else{
            log.info("No deal notification sent for the game with id={}, price didn't drop", game.getId());
            return false;
        }
    }
}
