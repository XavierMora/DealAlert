package com.games_price_tracker.api.account;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long>{
    Optional<Account> findByEmail(String email);

    Optional<Account> findByEmailAndSignInCodeAndLastDeviceIdAssignedCode(String email, String code, UUID deviceId);

    boolean existsByEmail(String email);
}
