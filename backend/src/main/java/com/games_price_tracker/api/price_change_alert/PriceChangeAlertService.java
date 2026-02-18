package com.games_price_tracker.api.price_change_alert;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.games_price_tracker.api.account.Account;
import com.games_price_tracker.api.common.exceptions.ResourceNotFoundException;
import com.games_price_tracker.api.common.exceptions.TooManyRequestsException;
import com.games_price_tracker.api.email.SendEmailService;
import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.game.GameService;
import com.games_price_tracker.api.price.dtos.ChangePriceResult;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

@Service
public class PriceChangeAlertService {
    private final PriceChangeAlertRepository priceChangeAlertRepository;
    private final GameService gameService;
    private final SendEmailService sendEmailService;
    private final PriceChangeAlertCacheService priceChangeAlertCacheService;

    PriceChangeAlertService(PriceChangeAlertRepository priceChangeAlertRepository, GameService gameService, SendEmailService sendEmailService, PriceChangeAlertCacheService priceChangeAlertCacheService){
        this.priceChangeAlertRepository = priceChangeAlertRepository;
        this.gameService = gameService;
        this.sendEmailService = sendEmailService;
        this.priceChangeAlertCacheService = priceChangeAlertCacheService;
    }

    private void verifyRateLimit(Account account){
        Bucket bucket = priceChangeAlertCacheService.getBucket(account.getEmail());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if(!probe.isConsumed()) throw new TooManyRequestsException(probe.getNanosToWaitForRefill(), TimeUnit.NANOSECONDS);
    }

    @Transactional
    public Optional<PriceChangeAlert> createAlert(Account account, Long gameId){
        verifyRateLimit(account);
        Optional<PriceChangeAlert> optionalPriceAlert = priceChangeAlertRepository.findByAccountIdAndGameId(account.getId(), gameId);

        if(optionalPriceAlert.isPresent()) return Optional.empty();

        Game game = gameService.getGameById(gameId);

        PriceChangeAlert priceAlert = new PriceChangeAlert(account, game);
        Optional<PriceChangeAlert> newAlert = Optional.of(priceChangeAlertRepository.save(priceAlert));

        priceChangeAlertCacheService.evictAlertsCache(account.getId());

        return newAlert;
    }

    public Page<PriceChangeAlert> getAlerts(Account account, Pageable pageable){
        verifyRateLimit(account);
        return priceChangeAlertCacheService.getAlerts(account.getId(), pageable);
    }

    @Transactional
    public void deleteAlert(Long gameId, Account account) throws ResourceNotFoundException{
        verifyRateLimit(account);
        boolean alertDeleted = priceChangeAlertRepository.deleteByAccountIdAndGameId(account.getId(), gameId) > 0;
        
        if(!alertDeleted) throw new ResourceNotFoundException("La alerta no existe.");
        
        priceChangeAlertCacheService.evictAlertsCache(account.getId());
    }

    public void notifyPriceChange(Game game, ChangePriceResult result){
        Optional<List<PriceChangeAlert>> optionalAlerts = priceChangeAlertRepository.findAllByGameId(game.getId());

        if(optionalAlerts.isEmpty()) return;

        List<String> emails = optionalAlerts.get().stream().map(alert -> alert.getAccount().getEmail()).toList();
        sendEmailService.priceChangeEmail(game, result, emails);
    }
}
