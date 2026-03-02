package com.games_price_tracker.api.price;

import org.springframework.stereotype.Component;

import com.games_price_tracker.api.price.dtos.PriceInfo;

@Component
public class PriceMapper {
    public PriceInfo toPriceInfo(Price price){
        return new PriceInfo(
            price.getId(), 
            price.getInitialPrice(), 
            price.getFinalPrice(), 
            calcDiscount(price.getInitialPrice(), price.getFinalPrice()),
            price.getLastUpdate()
        );
    }

    public PriceInfo toPriceInfo(int initialPrice, int finalPrice){
        return new PriceInfo(
            null, 
            initialPrice, 
            finalPrice, 
            calcDiscount(initialPrice, finalPrice),
            null
        );
    }

    private static int calcDiscount(int initialPrice, int finalPrice){
        if(initialPrice > finalPrice) return (initialPrice-finalPrice)*100/initialPrice;
        else return 0;
    }
}
