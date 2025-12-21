package com.games_price_tracker.api.game;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Game {
    @Id
    @GeneratedValue
    private Long id;
    private Long steamId;
    private String name;

    Game(){}

    Game(Long steamId, String name){
        this.steamId = steamId;
        this.name = name;
    }

    public Long getId() {
        return id;
    }
    
    public Long getSteamId() {
        return steamId;
    }

    public String getName() {
        return name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSteamId(Long steamId) {
        this.steamId = steamId;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;

        if(obj == null || obj.getClass() != getClass()) return false;

        Game game = (Game) obj;

        return id != null && id.equals(game.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return this.id+"";
    }
}
