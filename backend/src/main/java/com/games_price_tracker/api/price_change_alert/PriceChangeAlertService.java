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
import com.games_price_tracker.api.account.AccountRateLimit;
import com.games_price_tracker.api.core.exceptions.ResourceNotFoundException;
import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.game.GameService;
import com.games_price_tracker.api.price.dtos.ChangePriceResult;
import com.games_price_tracker.api.telegram_bot.client.TelegramClient;

@Service
public class PriceChangeAlertService {
    private final PriceChangeAlertRepository priceChangeAlertRepository;
    private final GameService gameService;
    private final TelegramClient telegramClient;
    private final Logger log = LoggerFactory.getLogger(PriceChangeAlertService.class);
    private final AccountRateLimit accountRateLimit;

    PriceChangeAlertService(PriceChangeAlertRepository priceChangeAlertRepository, GameService gameService, TelegramClient telegramClient, AccountRateLimit accountRateLimit){
        this.accountRateLimit = accountRateLimit;
        this.priceChangeAlertRepository = priceChangeAlertRepository;
        this.gameService = gameService;
        this.telegramClient = telegramClient;
    }

    @Transactional
    public Optional<PriceChangeAlert> createAlert(Account account, Long gameId){
        accountRateLimit.checkAccountRequestLimit(account.getEmail());
        Optional<PriceChangeAlert> optionalPriceAlert = priceChangeAlertRepository.findByAccountIdAndGameId(account.getId(), gameId);

        if(optionalPriceAlert.isPresent()) return Optional.empty();

        Game game = gameService.getGameById(gameId);

        PriceChangeAlert priceAlert = new PriceChangeAlert(account, game);
        Optional<PriceChangeAlert> newAlert = Optional.of(priceChangeAlertRepository.save(priceAlert));

        return newAlert;
    }

    public Page<PriceChangeAlert> getAlerts(Account account, Pageable pageable){
        accountRateLimit.checkAccountRequestLimit(account.getEmail());
        return priceChangeAlertRepository.findAllByAccountId(account.getId(), pageable);
    }

    @Transactional
    public void deleteAlert(Long gameId, Account account) throws ResourceNotFoundException{
        accountRateLimit.checkAccountRequestLimit(account.getEmail());
        boolean alertDeleted = priceChangeAlertRepository.deleteByAccountIdAndGameId(account.getId(), gameId) > 0;
        
        if(!alertDeleted) throw new ResourceNotFoundException("La alerta no existe.");        
    }

    public void notifyPriceChange(Game game, ChangePriceResult result){
        List<PriceChangeAlert> alerts = priceChangeAlertRepository.findAllByGameId(game.getId());

        if(alerts.isEmpty()) return;
        
        if(result.newPrice().initialPrice() <= result.newPrice().finalPrice()){
            log.info("No game deal notification with id={} sent because price didn't drop", game.getId());
            return;
        }

        List<Long> recipients = alerts.stream()
        .filter(alert -> alert.getAccount().getTelegramUserId() != null)
        .map(alert -> alert.getAccount().getTelegramUserId())
        .toList();
        
        if(recipients.isEmpty()){
            log.info("Recipients for game deal notification with id={} don't have telegram account linked", game.getId());
            return;
        }

        try {
            log.info("Starting send of game deal notification with id={} to {} recipients", game.getId(), recipients.size());

            telegramClient.sendDealNotification(game, result, recipients);
        } catch (Exception e) {
            log.error("Failed to create game deal notification with id={}", game.getId(), e);
        }
    }
}
