package com.games_price_tracker.api.price;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.game.GameRepository;
import com.games_price_tracker.api.price.dtos.PriceInfo;

@SpringBootTest
public class PriceTest {
    @Autowired
    private PriceMapper priceMapper;
    @Autowired
    private PriceService priceService;
    @Autowired
    private GameRepository gameRepository;

    private Game game;

    @BeforeEach
    void loadGameForTest(){
        game = gameRepository.save(new Game(0L, "test"));
    }

    @Test
    void shouldRefreshLastUpdate() throws NoSuchElementException{
        Price price = priceService.createPrice(2, 2, game);
        Long id = price.getId();
        Instant oldLastUpdate = price.getLastUpdate();
        
        priceService.refreshLastUpdate(id);
        Instant newLastUpdate = priceService.getPriceById(id).getLastUpdate();

        assertFalse(oldLastUpdate.equals(newLastUpdate));
    }

    @Test
    void shouldMapToPriceInfo(){
        Price price = new Price(1049, 702, game);

        PriceInfo priceInfo = priceMapper.toPriceInfo(price);

        assertNotNull(priceInfo);
        assertEquals(1049, priceInfo.initialPrice());
        assertEquals(702, priceInfo.finalPrice());
        assertEquals(33, priceInfo.discount());
    }
}
