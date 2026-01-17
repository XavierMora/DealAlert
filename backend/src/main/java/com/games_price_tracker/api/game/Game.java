package com.games_price_tracker.api.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.games_price_tracker.api.price.Price;
import com.games_price_tracker.api.price_alert.PriceAlert;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

    @OneToMany(mappedBy = "game")
    private List<PriceAlert> priceAlerts = new ArrayList<PriceAlert>();
    
    public Game(){}

    public Game(Long steamId, String name){
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

    public List<PriceAlert> getPriceAlerts() {
        return priceAlerts;
    }

    public void setPriceAlerts(List<PriceAlert> priceAlerts) {
        this.priceAlerts = priceAlerts;
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
