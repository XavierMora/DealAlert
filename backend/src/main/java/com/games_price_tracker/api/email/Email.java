package com.games_price_tracker.api.email;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Email {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;
    private boolean verified;

    @Column(unique = true)
    private String verificationToken;
    private Instant verificationTokenExpiration;
    private Instant lastVerificationEmailSentAt;

    public Email(){}

    public Email(String address){
        this.address = address;
        this.verified = false;
    }

    public String getAddress() {
        return address;
    }

    public Long getId() {
        return id;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public Instant getVerificationTokenExpiration() {
        return verificationTokenExpiration;
    }

    public boolean getVerified(){
        return verified;
    }

    public Instant getLastVerificationEmailSentAt() {
        return lastVerificationEmailSentAt;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public void setVerificationTokenExpiration(Instant verificationTokenExpiration) {
        this.verificationTokenExpiration = verificationTokenExpiration;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void setLastVerificationEmailSentAt(Instant lastVerificationEmailSentAt) {
        this.lastVerificationEmailSentAt = lastVerificationEmailSentAt;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;

        if(obj == null || !getClass().equals(obj.getClass())) return false;

        Email email = (Email) obj;

        return id != null && id.equals(email.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, address);
    }

    @Override
    public String toString() {
        return String.format("[id=%d, address=%s, verified=%b]", id, address, verified);
    }
}
