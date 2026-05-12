package com.games_price_tracker.api.account;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.games_price_tracker.api.price_change_alert.PriceChangeAlert;
import com.games_price_tracker.api.session_token.SessionToken;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "account")
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "account", cascade = {CascadeType.PERSIST}, orphanRemoval = true) // orphanRemoval hace que si se elimina una entidad de la lista se borre tambien de la bd
    @OrderBy("expiration DESC")
    private List<SessionToken> sessionTokens = new ArrayList<SessionToken>();

    @OneToMany(mappedBy = "account")
    private List<PriceChangeAlert> priceAlerts = new ArrayList<PriceChangeAlert>();

    public Account(){}

    public Account(String email){
        this.email = email;
    }
    
    public void addToken(SessionToken token, int maxTokens){
        if(sessionTokens.size() >= maxTokens){
            sessionTokens.removeLast();
        };

        sessionTokens.add(token);
    }

    public String getEmail() {
        return email;
    }

    public Long getId() {
        return id;
    }

    public List<SessionToken> getSessionTokens() {
        return sessionTokens;
    }

    public List<PriceChangeAlert> getPriceAlerts() {
        return priceAlerts;
    }

    public void setPriceAlerts(List<PriceChangeAlert> priceAlerts) {
        this.priceAlerts = priceAlerts;
    }

    public void setSessionTokens(List<SessionToken> sessionTokens) {
        this.sessionTokens = sessionTokens;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;

        if(obj == null || !getClass().equals(obj.getClass())) return false;

        Account user = (Account) obj;

        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return String.format("[id=%d, email=%s]", id, email);
    }
}
