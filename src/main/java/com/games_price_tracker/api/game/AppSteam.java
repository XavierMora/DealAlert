package com.games_price_tracker.api.game;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AppSteam{
    @JsonProperty("appid")
    private Long steamId;
    private String name;

    AppSteam(){}

    AppSteam(Long steamId, String name){
        this.steamId = steamId;
        this.name = name;
    }

    public Long getSteamId() {
        return steamId;
    }

    public String getName() {
        return name;
    }

    public void setSteamId(Long steamId) {
        this.steamId = steamId;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("[steamId=%d, name=%s]", steamId, name);
    }
}