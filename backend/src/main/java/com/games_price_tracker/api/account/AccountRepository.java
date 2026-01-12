package com.games_price_tracker.api.account;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long>{
    Optional<Account> findByEmail(String email);

    Optional<Account> findBySignInCode(String code);

    boolean existsByEmail(String email);
}
