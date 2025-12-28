package com.games_price_tracker.api.price;

import org.springframework.stereotype.Component;

import com.games_price_tracker.api.price.dtos.PriceInfo;

@Component
public class PriceMapper {
    public PriceInfo toPriceInfo(Price price){
        int initialPrice = price.getInitialPrice();
        int finalPrice = price.getFinalPrice();
        int discount = 0;
        
        if(initialPrice != finalPrice) discount = (initialPrice-finalPrice)*100/initialPrice;

        return new PriceInfo(price.getId(), initialPrice, finalPrice, discount);
    }
}
