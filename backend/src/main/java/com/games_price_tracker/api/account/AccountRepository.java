package com.games_price_tracker.api.account;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AccountRepository extends JpaRepository<Account, Long>{
    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("""
       update Account a
       set a.lastSignInCodeSentAt = ?2     
       where a.email = ?1
    """)
    void updateLastSignInCodeSentAtByEmail(String email, Instant date);
}
