package com.games_price_tracker.api.price;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        game = gameRepository.saveAndFlush(new Game(0L, "test"));
    }

    @Test
    void changePriceShouldCreatePrice() throws NoSuchElementException{
        priceService.changePrice(2, 1, game);

        game = gameRepository.findById(game.getId()).orElseThrow();

        assertNotNull(game.getPrice());
        assertEquals(2, game.getPrice().getInitialPrice());
        assertEquals(1, game.getPrice().getFinalPrice());
    }

    @Test
    void changePriceShouldJustRefreshLastUpdate(){
        priceService.changePrice(2, 1, game);
        game = gameRepository.findById(game.getId()).orElseThrow();
        Price firstPrice = game.getPrice();

        priceService.changePrice(2, 1, game);
        game = gameRepository.findById(game.getId()).orElseThrow();
        Price priceLastUpdateChanged = game.getPrice();

        assertTrue(firstPrice.getLastUpdate().isBefore(priceLastUpdateChanged.getLastUpdate()));
        assertEquals(2, priceLastUpdateChanged.getInitialPrice());
        assertEquals(1, priceLastUpdateChanged.getFinalPrice());

        priceService.changePrice(1, 1, game);
        game = gameRepository.findById(game.getId()).orElseThrow();
        Price priceChanged = game.getPrice();

        assertTrue(priceLastUpdateChanged.getLastUpdate().isBefore(priceChanged.getLastUpdate()));
        assertEquals(1, priceChanged.getInitialPrice());
        assertEquals(1, priceChanged.getFinalPrice());
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
