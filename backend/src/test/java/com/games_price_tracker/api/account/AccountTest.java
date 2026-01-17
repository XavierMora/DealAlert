package com.games_price_tracker.api.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.games_price_tracker.api.account.dtos.VerifyCodeBody;
import com.games_price_tracker.api.session_token.SessionToken;
import com.games_price_tracker.api.session_token.SessionTokenService;

import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional // Hace que para cada método que modifica la bd se haga rollback y asi no quedan los datos
public class AccountTest {
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final SessionTokenService sessionTokenService;
    private final EntityManager entityManager;

    @Autowired
    AccountTest(AccountService accountService, AccountRepository accountRepository, SessionTokenService sessionTokenService, EntityManager entityManager){
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.sessionTokenService = sessionTokenService;
        this.entityManager = entityManager;
    }
    
    @Test
    void verifyCodeShouldReturnToken(){
        // Carga de account
        Account account = new Account("test@test");
        account.setSignInCode("1");
        account.setSignInCodeExpiration(Instant.now().plus(Duration.ofMinutes(5)));
        account.setLastDeviceIdAssignedCode("10");
    
        for (int i = 0; i < accountService.getMaxTokens(); i++) {
            SessionToken token = sessionTokenService.createSessionToken(account);
            token.setExpiration(Instant.now().plus(Duration.ofMinutes(i+1)));
            account.getSessionTokens().add(token);
        }
        accountRepository.saveAndFlush(account);
        entityManager.clear(); // Limpia lo que esta en memoria entonces si se accede a la lista de tokens permite que se consulte a la bd y venga ordenado

        VerifyCodeBody verifyCodeBody = new VerifyCodeBody("test@test", "1");
        SessionToken token = accountService.verifyCode(verifyCodeBody.email(), verifyCodeBody.code(), "10");

        assertNotNull(token);
        
        account = accountRepository.findByEmail("test@test").get();

        assertNull(account.getSignInCode());
        assertNull(account.getSignInCodeExpiration());
        assertEquals(accountService.getMaxTokens(), account.getSessionTokens().size());
        List<UUID> tokens = account.getSessionTokens().stream().map(t -> t.getToken()).toList();
        assertTrue(tokens.contains(token.getToken()));
    }
}
