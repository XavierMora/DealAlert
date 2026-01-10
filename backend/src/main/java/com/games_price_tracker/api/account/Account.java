package com.games_price_tracker.api.account;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private boolean emailVerified;

    @Column(unique = true)
    private String emailVerificationToken;
    private Instant emailVerificationTokenExpiration;
    private Instant lastVerificationEmailSentAt;

    public Account(){}

    public Account(String email){
        this.email = email;
        this.emailVerified = false;
    }

    public String getEmail() {
        return email;
    }

    public Long getId() {
        return id;
    }

    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }

    public Instant getEmailVerificationTokenExpiration() {
        return emailVerificationTokenExpiration;
    }

    public boolean getEmailVerified(){
        return emailVerified;
    }

    public Instant getLastVerificationEmailSentAt() {
        return lastVerificationEmailSentAt;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmailVerificationToken(String verificationToken) {
        this.emailVerificationToken = verificationToken;
    }

    public void setEmailVerificationTokenExpiration(Instant verificationTokenExpiration) {
        this.emailVerificationTokenExpiration = verificationTokenExpiration;
    }

    public void setEmailVerified(boolean verified) {
        this.emailVerified = verified;
    }

    public void setLastVerificationEmailSentAt(Instant lastVerificationEmailSentAt) {
        this.lastVerificationEmailSentAt = lastVerificationEmailSentAt;
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
        return String.format("[id=%d, email=%s, verified=%b]", id, email, emailVerified);
    }
}
