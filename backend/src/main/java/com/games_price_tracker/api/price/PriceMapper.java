package com.games_price_tracker.api.price;

import org.springframework.stereotype.Component;

import com.games_price_tracker.api.price.dtos.PriceInfo;

@Component
public class PriceMapper {
    public PriceInfo toPriceInfo(Price price){
        int initialPrice = price.getInitialPrice();
        int finalPrice = price.getFinalPrice();
        int discount = 0;
        
        if(initialPrice != finalPrice) discount = (price.getInitialPrice()-price.getFinalPrice())*100/price.getInitialPrice();

        return new PriceInfo(price.getInitialPrice(), price.getFinalPrice(), discount);
    }
}
