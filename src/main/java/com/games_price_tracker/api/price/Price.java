package com.games_price_tracker.api.price;

import java.time.Instant;
import java.util.Objects;

import com.games_price_tracker.api.game.Game;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class Price {
    @Id @GeneratedValue
    private Long id;
    private int initialPrice;
    private int finalPrice;
    private Instant timestamp;
    private Instant lastUpdate;

    @OneToOne(optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    public Price(){}

    public Price(int initialPrice, int finalPrice, Game game){
        this.initialPrice = initialPrice;
        this.finalPrice = finalPrice;
        this.timestamp = Instant.now();
        this.lastUpdate = Instant.now();
        this.game = game;
    }

    public Long getId() {
        return id;
    }

    public Game getGame() {
        return game;
    }

    public int getFinalPrice() {
        return finalPrice;
    }

    public int getInitialPrice() {
        return initialPrice;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public void setFinalPrice(int finalPrice) {
        this.finalPrice = finalPrice;
    }
    
    public void setGame(Game game) {
        this.game = game;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setInitialPrice(int initialPrice) {
        this.initialPrice = initialPrice;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;

        if(obj == null || obj.getClass() != getClass()) return false;

        Price price = (Price) obj;

        return id != null && id.equals(price.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, initialPrice, finalPrice, timestamp);
    }

    @Override
    public String toString() {
        return String.format("[id=%d, initialPrice=%d, finalPrice=%d, timestamp=%s]", id, initialPrice, finalPrice, timestamp.toString());
    }
}
