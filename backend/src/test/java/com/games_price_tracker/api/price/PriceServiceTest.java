package com.games_price_tracker.api.price;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.Instant;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.game.GameRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PriceServiceTest {
    @Autowired
    private PriceService priceService;
    @Autowired
    private GameRepository gameRepository;

    @Test
    void shouldRefreshLastUpdate() throws NoSuchElementException{
        Game game = gameRepository.save(new Game(0L, "test"));

        Price price = priceService.createPrice(2, 2, game);
        Long id = price.getId();
        Instant oldLastUpdate = price.getLastUpdate();
        
        priceService.refreshLastUpdate(id);
        Instant newLastUpdate = priceService.getPrice(id).getLastUpdate();

        assertFalse(oldLastUpdate.equals(newLastUpdate));
    }
}
