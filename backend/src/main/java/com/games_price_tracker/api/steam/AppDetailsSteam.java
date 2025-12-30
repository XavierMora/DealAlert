package com.games_price_tracker.api.steam;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AppDetailsSteam{
    private int initialPrice=0;
    private int finalPrice=0;
    
    AppDetailsSteam(){}

    @SuppressWarnings("unchecked")
    @JsonProperty("data")
    public void parseData(Map<String, Object> data){
        boolean isFree = (boolean) data.get("is_free");
        
        // Si esta gratis, el price overview no esta
        if(!isFree){
            Map<String, Object> priceOverview = (Map<String, Object>) data.get("price_overview");
            initialPrice = (int) priceOverview.get("initial");
            finalPrice = (int) priceOverview.get("final");
        }
    }

    public int getFinalPrice() {
        return finalPrice;
    }

    public int getInitialPrice() {
        return initialPrice;
    }

    public void setFinalPrice(int finalPrice) {
        this.finalPrice = finalPrice;
    }

    public void setInitialPrice(int initialPrice) {
        this.initialPrice = initialPrice;
    }

    @Override
    public String toString() {
        return String.format("[initialPrice=%s, finalPrice=%s]", initialPrice, finalPrice);
    }
}