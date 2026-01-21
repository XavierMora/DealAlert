package com.games_price_tracker.api.price;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.price.dtos.ChangePriceResult;

import org.springframework.transaction.annotation.Transactional;

@Service
public class PriceService {
    private final PriceRepository priceRepository;
    private final PriceMapper priceMapper;

    public PriceService(PriceRepository priceRepository, PriceMapper priceMapper){
        this.priceRepository = priceRepository;
        this.priceMapper = priceMapper;
    }

    @Transactional
    public Optional<ChangePriceResult> changePrice(int initialPrice, int finalPrice, Game game) throws IllegalArgumentException{
        if(game == null || game.getId() == null) throw new IllegalArgumentException(game == null ? "Game is null" : "Game id is null");

        Optional<Price> optionalPrice = priceRepository.findByGameId(game.getId());

        if(optionalPrice.isEmpty()){
            priceRepository.save(new Price(initialPrice, finalPrice, game));
            return Optional.empty();
        }

        Price price = optionalPrice.get();

        if(price.getInitialPrice() == initialPrice && price.getFinalPrice() == finalPrice){ // Los valores del precio no cambiaron
            priceRepository.refreshLastUpdate(price.getId(), Instant.now());
            return Optional.empty();
        }

        priceRepository.updatePrice(price.getId(), initialPrice, finalPrice, Instant.now());
        
        // Se crea el resultado con los valores del precio viejo y el nuevo
        return Optional.of(new ChangePriceResult(
            priceMapper.toPriceInfo(price.getInitialPrice(), price.getFinalPrice()),
            priceMapper.toPriceInfo(initialPrice, finalPrice)
        ));
    }

    public Price getPriceById(Long id) throws NoSuchElementException{
        return priceRepository.findById(id).orElseThrow();
    }
}
