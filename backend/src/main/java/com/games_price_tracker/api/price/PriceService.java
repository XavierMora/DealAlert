package com.games_price_tracker.api.price;

import jakarta.transaction.Transactional;

public class PriceService {
    private final PriceRepository priceRepository;

    public PriceService(PriceRepository priceRepository){
        this.priceRepository = priceRepository;
    }

    boolean pricesChanged(Price oldPrice, int newInitialPrice, int newFinalPrice){
        return oldPrice.getInitialPrice() != newInitialPrice && oldPrice.getFinalPrice() != newFinalPrice;
    }
}
