package com.games_price_tracker.api.game;

import java.util.Objects;

import com.games_price_tracker.api.price.Price;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long steamId;
    private String name;

    @OneToOne(mappedBy = "game")
    private Price price;
    
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

    public Price getPrice() {
        return price;
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

    public void setPrice(Price price) {
        this.price = price;
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
        return Objects.hash(id, steamId);
    }

    @Override
    public String toString() {
        return String.format("[id=%d, steamId=%d, name=%s]", id, steamId, name);
    }
}
