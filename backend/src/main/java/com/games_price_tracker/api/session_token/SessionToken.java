package com.games_price_tracker.api.session_token;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.games_price_tracker.api.account.Account;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class SessionToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID token;
    private Instant expiration;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id")
    private Account account;

    public SessionToken(){}

    public SessionToken(Account account){
        this.account = account;
    }

    public void assignToken(UUID token, Duration durationValidToken){
        this.token = token;
        this.expiration = Instant.now().plus(durationValidToken);
    }

    public boolean expired(){
        return expiration == null || expiration.isBefore(Instant.now());
    }

    public UUID getToken() {
        return token;
    }

    public Instant getExpiration() {
        return expiration;
    }

    public Long getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setExpiration(Instant expiration) {
        this.expiration = expiration;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setToken(UUID token) {
        this.token = token;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;

        if(obj == null || !getClass().equals(obj.getClass())) return false;

        SessionToken sessionToken = (SessionToken) obj;

        return id != null && id.equals(sessionToken.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, token);
    }

    @Override
    public String toString() {
        return String.format("[id=%d, token=%s]", id, token);
    }
}
