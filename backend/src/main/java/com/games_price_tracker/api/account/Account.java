package com.games_price_tracker.api.account;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

@Entity
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    private String signInCode;
    private Instant signInCodeExpiration;
    private Instant lastSignInCodeSentAt;

    private UUID lastDeviceIdAssignedCode;

    @OneToMany(mappedBy = "account", cascade = {CascadeType.PERSIST}, orphanRemoval = true) // orphanRemoval hace que si se elimina una entidad de la lista se borre tambien de la bd
    @OrderBy("expiration DESC")
    private List<SessionToken> sessionTokens = new ArrayList<SessionToken>();

    @OneToMany(mappedBy = "account")
    private List<PriceChangeAlert> priceAlerts = new ArrayList<PriceChangeAlert>();

    public Account(){}

    public Account(String email){
        this.email = email;
    }
    
    public void assignSignInCode(String code, Duration validDuration, UUID deviceId){
        this.signInCode = code;
        this.signInCodeExpiration = Instant.now().plus(validDuration);
        this.lastDeviceIdAssignedCode = deviceId;
    }

    public void addToken(SessionToken token, int maxTokens){
        if(sessionTokens.size() >= maxTokens){
            sessionTokens.removeLast();
        };

        sessionTokens.add(token);
    }

    public boolean signInCodeExpired(){
        return signInCodeExpiration == null || signInCodeExpiration.isBefore(Instant.now());
    }

    public String getEmail() {
        return email;
    }

    public Long getId() {
        return id;
    }

    public String getSignInCode() {
        return signInCode;
    }

    public Instant getSignInCodeExpiration() {
        return signInCodeExpiration;
    }

    public Instant getLastSignInCodeSentAt() {
        return lastSignInCodeSentAt;
    }

    public UUID getLastDeviceIdAssignedCode() {
        return lastDeviceIdAssignedCode;
    }

    public List<SessionToken> getSessionTokens() {
        return sessionTokens;
    }

    public void setLastDeviceIdAssignedCode(UUID lastDeviceIdAssignedCode) {
        this.lastDeviceIdAssignedCode = lastDeviceIdAssignedCode;
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

    public void setSignInCode(String signInCode) {
        this.signInCode = signInCode;
    }

    public void setSignInCodeExpiration(Instant signInCodeExpiration) {
        this.signInCodeExpiration = signInCodeExpiration;
    }

    public void setLastSignInCodeSentAt(Instant lastSignInCodeSentAt) {
        this.lastSignInCodeSentAt = lastSignInCodeSentAt;
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
