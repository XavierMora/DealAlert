package com.games_price_tracker.api.price_notification;

import java.util.Objects;

import com.games_price_tracker.api.game.Game;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class PriceNotification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int targetPrice;
    private String email;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_id")
    private Game game;

    public PriceNotification(int targetPrice, String email, Game game){;
        this.targetPrice = targetPrice;
        this.email = email;
        this.game = game;
    }

    public Long getId() {
        return id;
    }

    public int getTargetPrice() {
        return targetPrice;
    }

    public String getEmail() {
        return email;
    }

    public Game getGame() {
        return game;
    }

    public void setTargetPrice(int targetPrice){
        this.targetPrice = targetPrice;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;

        if(obj == null || !getClass().equals(obj.getClass())) return false;

        PriceNotification priceNotification = (PriceNotification) obj;
        
        return id != null && priceNotification.id.equals(this.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, targetPrice, email);
    }

    @Override
    public String toString() {
        return String.format("[id=%d, targetPrice=%d, email=%s]", id, targetPrice, email);
    }
}
