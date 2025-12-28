package com.games_price_tracker.api.price;

import java.time.Instant;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.games_price_tracker.api.game.Game;

import jakarta.transaction.Transactional;

@Service
public class PriceService {
    private final PriceRepository priceRepository;

    public PriceService(PriceRepository priceRepository){
        this.priceRepository = priceRepository;
    }

    boolean pricesChanged(Price oldPrice, int newInitialPrice, int newFinalPrice){
        return oldPrice.getInitialPrice() != newInitialPrice && oldPrice.getFinalPrice() != newFinalPrice;
    }

    @Transactional
    Price createPrice(int initialPrice, int finalPrice, Game game){
        return priceRepository.save(new Price(initialPrice, finalPrice, game));
    }

    Price getPrice(Long id) throws NoSuchElementException{
        return priceRepository.findById(id).orElseThrow();
    }
}
