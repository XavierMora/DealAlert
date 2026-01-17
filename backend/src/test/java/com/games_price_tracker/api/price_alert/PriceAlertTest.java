package com.games_price_tracker.api.price_alert;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.games_price_tracker.api.account.Account;
import com.games_price_tracker.api.account.AccountRepository;
import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.game.GameRepository;
import com.games_price_tracker.api.game.exceptions.GameNotFoundException;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PriceAlertTest {
    private final PriceAlertService priceAlertService;
    private final PriceAlertRepository priceAlertRepository;
    private final GameRepository gameRepository;
    private final AccountRepository accountRepository;
    private Game gameTest;
    private Account accountTest;

    @Autowired
    PriceAlertTest(PriceAlertService priceAlertService, PriceAlertRepository priceAlertRepository, EntityManager entityManager, GameRepository gameRepository, AccountRepository accountRepository){
        this.priceAlertService = priceAlertService;
        this.priceAlertRepository = priceAlertRepository;
        this.gameRepository = gameRepository;
        this.accountRepository = accountRepository;
    }
    
    @BeforeEach
    void setup(){
        gameTest = gameRepository.save(new Game(1L, "test"));
        accountTest = accountRepository.save(new Account("test@test"));
    }

    @Test
    void shouldCreatePriceAlert(){
        priceAlertService.createPriceAlert(accountTest, gameTest.getId());

        PriceAlert priceAlert = priceAlertRepository.findByAccountIdAndGameId(accountTest.getId(), gameTest.getId()).get();
        
        assertNotNull(priceAlert);
        assertNotNull(priceAlert.getAccount());
        assertNotNull(priceAlert.getGame());
    }
    
    @Test
    void shouldNotCreatePriceAlert(){
        assertThrows(GameNotFoundException.class, () -> priceAlertService.createPriceAlert(accountTest, 10L));

        assertTrue(priceAlertRepository.findByAccountIdAndGameId(accountTest.getId(), 10L).isEmpty());
    }
}
