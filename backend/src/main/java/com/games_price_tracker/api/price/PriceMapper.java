package com.games_price_tracker.api.price;

import java.text.DecimalFormat;

import org.springframework.stereotype.Component;

import com.games_price_tracker.api.price.dtos.PriceInfo;
import com.games_price_tracker.api.price.dtos.PriceInfoEmail;

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

    public PriceInfoEmail fromPriceInfoToPriceInfoEmail(PriceInfo priceInfo){
        return new PriceInfoEmail(
            priceInfo.initialPrice(), 
            formatPrice(priceInfo.initialPrice()), 
            priceInfo.finalPrice(), 
            formatPrice(priceInfo.finalPrice()),
            priceInfo.discount()
        );
    }

    private String formatPrice(int price){
        return new DecimalFormat("$##.## USD").format(price/100.0);
    }

    private static int calcDiscount(int initialPrice, int finalPrice){
        if(initialPrice > finalPrice) return (initialPrice-finalPrice)*100/initialPrice;
        else return 0;
    }
}
