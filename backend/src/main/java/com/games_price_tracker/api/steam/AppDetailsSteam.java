package com.games_price_tracker.api.steam;

import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.databind.JsonNode;

public class AppDetailsSteam{
    private Long steamId;
    private int initialPrice=0;
    private int finalPrice=0;

    AppDetailsSteam(){}

    @JsonProperty("data")
    void parseData(JsonNode data){
        JsonNode priceOverview = data.get("price_overview");

        if(priceOverview != null && !priceOverview.isNull()){
            initialPrice = priceOverview.get("initial").asInt();
            finalPrice = priceOverview.get("final").asInt();
        }
    }

    public Long getSteamId() {
        return steamId;
    }

    public void setSteamId(Long steamId) {
        this.steamId = steamId;
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
        return String.format("[steamId=%d, initialPrice=%s, finalPrice=%s]", steamId, initialPrice, finalPrice);
    }
}