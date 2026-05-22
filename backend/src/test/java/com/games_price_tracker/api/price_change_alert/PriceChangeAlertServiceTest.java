package com.games_price_tracker.api.price_change_alert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.games_price_tracker.api.account.Account;
import com.games_price_tracker.api.account.AccountRateLimit;
import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.game.GameService;
import com.games_price_tracker.api.price.dtos.ChangePriceResult;
import com.games_price_tracker.api.price.dtos.PriceInfo;
import com.games_price_tracker.api.telegram_bot.client.TelegramClient;

@ExtendWith(MockitoExtension.class)
public class PriceChangeAlertServiceTest {
    @Mock PriceChangeAlertRepository priceChangeAlertRepository;
    @Mock GameService gameService;
    @Mock TelegramClient telegramClient;
    @Mock AccountRateLimit accountRateLimit;
    @InjectMocks private PriceChangeAlertService priceChangeAlertService;

    @Test
    void shouldCreatePriceAlert(){
        Account account = new Account();
        account.setId(1L);
        Game game = new Game();
        game.setId(1L);
        given(priceChangeAlertRepository.findByAccountIdAndGameId(account.getId(), game.getId())).willReturn(Optional.empty());
        given(gameService.getGameById(game.getId())).willReturn(game);
        PriceChangeAlert priceChangeAlert = new PriceChangeAlert(account, game);
        given(priceChangeAlertRepository.save(any(PriceChangeAlert.class))).willReturn(priceChangeAlert);
        
        Optional<PriceChangeAlert> alertCreated = priceChangeAlertService.createAlert(account, game.getId());

        ArgumentCaptor<PriceChangeAlert> priceChangeAlertCaptor = ArgumentCaptor.forClass(PriceChangeAlert.class);
        verify(priceChangeAlertRepository).save(priceChangeAlertCaptor.capture());
        
        PriceChangeAlert alertArgCaptured = priceChangeAlertCaptor.getValue();

        assertTrue(alertCreated.isPresent());
        assertEquals(alertCreated.get().getAccount(), alertArgCaptured.getAccount());
        assertEquals(alertCreated.get().getGame(), alertArgCaptured.getGame());
    }
    
    @Test
    void shouldNotCreatePriceAlert(){
        Account account = new Account();
        account.setId(1L);
        Game game = new Game();
        game.setId(1L);
        given(priceChangeAlertRepository.findByAccountIdAndGameId(account.getId(), game.getId())).willReturn(Optional.of(new PriceChangeAlert(account, game)));

        assertTrue(priceChangeAlertService.createAlert(account, game.getId()).isEmpty());
        verifyNoInteractions(gameService);
        verify(priceChangeAlertRepository, times(0)).save(any(PriceChangeAlert.class));
    }

    @Test
    void shouldStartNotification(){
        Game game = new Game(1L, "test", "test");
        game.setId(1L);
        Account account = new Account();
        account.setTelegramUserId(1L);
        given(priceChangeAlertRepository.findAllByGameId(game.getId())).willReturn(List.of(new PriceChangeAlert(account, game)));
        ChangePriceResult alertCreated = new ChangePriceResult(
            new PriceInfo(null, 10, 10, 0, null),
            new PriceInfo(null, 10, 7, 0, null)
        );

        priceChangeAlertService.notifyPriceChange(game, alertCreated);

        verify(telegramClient).sendDealNotification(eq(game), eq(alertCreated), eq(List.of(account.getTelegramUserId())));
    }

    @Test 
    void shouldNotStartNotificationWhenPriceDidNotDrop(){
        Game game = new Game(1L, "test", "test");
        game.setId(1L);
        given(priceChangeAlertRepository.findAllByGameId(game.getId())).willReturn(List.of(new PriceChangeAlert()));

        priceChangeAlertService.notifyPriceChange(game, new ChangePriceResult(
            new PriceInfo(null, 10, 10, 0, null),
            new PriceInfo(null, 10, 10, 0, null)
        ));

        verifyNoInteractions(telegramClient);
    }

    @Test 
    void shouldNotStartNotificationWhenRecipientsDontHaveTelegramLinked(){
        Game game = new Game(1L, "test", "test");
        game.setId(1L);
        Account account = new Account();
        account.setTelegramUserId(null);
        given(priceChangeAlertRepository.findAllByGameId(game.getId())).willReturn(List.of(new PriceChangeAlert(account, game)));

        priceChangeAlertService.notifyPriceChange(game, new ChangePriceResult(
            new PriceInfo(null, 10, 10, 0, null),
            new PriceInfo(null, 10, 7, 0, null)
        ));

        verifyNoInteractions(telegramClient);
    }
}
