package com.games_price_tracker.api.price;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.games_price_tracker.api.game.Game;

import org.springframework.transaction.annotation.Transactional;

@Service
public class PriceService {
    private final PriceRepository priceRepository;

    public PriceService(PriceRepository priceRepository){
        this.priceRepository = priceRepository;
    }

    public boolean pricesChanged(Price oldPrice, int newInitialPrice, int newFinalPrice){
        return oldPrice.getInitialPrice() != newInitialPrice || oldPrice.getFinalPrice() != newFinalPrice;
    }

    @Transactional
    public void changePrice(int initialPrice, int finalPrice, Game game) throws IllegalArgumentException{
        if(game == null || game.getId() == null) throw new IllegalArgumentException(game == null ? "Game is null" : "Game id is null");

        Optional<Price> optionalPrice = priceRepository.findByGameId(game.getId());

        // Si no existe, se crea
        if(optionalPrice.isEmpty()){
            priceRepository.save(new Price(initialPrice, finalPrice, game));
            return;
        }

        Price price = optionalPrice.get();

        // Si el precio existe se decide que cambiar
        if(pricesChanged(price, initialPrice, finalPrice)){
            priceRepository.updatePrice(price.getId(), initialPrice, finalPrice, Instant.now());
        }else{
            priceRepository.refreshLastUpdate(price.getId(), Instant.now());
        }
    }

    public Price getPriceById(Long id) throws NoSuchElementException{
        return priceRepository.findById(id).orElseThrow();
    }
}
