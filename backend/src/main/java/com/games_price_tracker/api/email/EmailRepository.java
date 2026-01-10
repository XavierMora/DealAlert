package com.games_price_tracker.api.email;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRepository extends JpaRepository<Email, Long>{
    Optional<Email> findByAddress(String address);

    Optional<Email> findByVerificationToken(String token);
}
