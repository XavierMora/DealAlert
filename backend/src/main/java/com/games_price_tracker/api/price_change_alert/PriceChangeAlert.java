package com.games_price_tracker.api.price_change_alert;

import java.time.Instant;
import java.util.Objects;

import com.games_price_tracker.api.account.Account;
import com.games_price_tracker.api.game.Game;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class PriceChangeAlert {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant createdAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_id")
    private Game game;

    public PriceChangeAlert(){}

    public PriceChangeAlert(Account account, Game game){;
        this.account = account;
        this.game = game;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public Game getGame() {
        return game;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setAccount(Account account) {
        this.account = account;
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

        PriceChangeAlert priceNotification = (PriceChangeAlert) obj;
        
        return id != null && priceNotification.id.equals(this.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[id=%d, accountEmail=%s]", id, account.getEmail());
    }
}
