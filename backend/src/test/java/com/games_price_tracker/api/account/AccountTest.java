package com.games_price_tracker.api.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.games_price_tracker.api.account.email_verification_enums.EmailVerificationResult;
import com.games_price_tracker.api.account.email_verification_enums.EmailVerificationStatus;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
public class AccountTest {
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    @Autowired
    AccountTest(AccountService accountService, AccountRepository accountRepository){
        this.accountService = accountService;
        this.accountRepository = accountRepository;
    }

    @Test
    void emailVerificationWithoutToken(){
        String testEmail = "test@account";

        EmailVerificationStatus status = accountService.emailVerification(testEmail);

        assertEquals(EmailVerificationStatus.VERIFICATION_EMAIL_SENT_NOW, status);

        Account account = accountRepository.findByEmail(testEmail).get();

        assertNotNull(account.getEmailVerificationToken());
        assertNotNull(account.getEmailVerificationTokenExpiration());
        assertNotNull(account.getLastVerificationEmailSentAt());
        assertFalse(account.getEmailVerified());
    }

    @Test
    void emailVerificationWithEmailSent(){
        Account account = new Account("test@account");
        accountRepository.save(account);
        accountService.emailVerification(account.getEmail());
        account = accountRepository.findByEmail(account.getEmail()).get();

        EmailVerificationStatus status = accountService.emailVerification(account.getEmail());
        assertEquals(EmailVerificationStatus.VERIFICATION_EMAIL_ALREADY_SENT, status);
    }

    @Test
    void emailVerificationWithTokenExpired(){
        Account account = new Account("test@account");
        account.setEmailVerificationToken("test_token");
        account.setEmailVerificationTokenExpiration(Instant.now().minus(Duration.ofDays(1)));
        account = accountRepository.save(account);

        EmailVerificationStatus status = accountService.emailVerification(account.getEmail());
        account = accountRepository.findByEmail(account.getEmail()).get();

        assertEquals(EmailVerificationStatus.VERIFICATION_EMAIL_SENT_NOW, status);
        assertNotEquals("test_token", account.getEmailVerificationToken());
    }

    @Test
    void verifyEmailShouldReturnTokenNotFound(){
        EmailVerificationResult result = accountService.verifyEmail("test");
        assertEquals(EmailVerificationResult.TOKEN_NOT_FOUND, result);
    }

    @Test
    void verifyEmailShouldReturnTokenExpired(){
        Account account = new Account("test");
        account.setEmailVerificationToken("test_token");
        account.setEmailVerificationTokenExpiration(Instant.now().minus(Duration.ofDays(1)));
        accountRepository.save(account);

        EmailVerificationResult result = accountService.verifyEmail("test_token");
        assertEquals(EmailVerificationResult.TOKEN_EXPIRED, result);
    }

    @Test
    void verifyEmailShouldReturnVerified(){
        Account account = new Account("test");
        account.setEmailVerificationToken("test_token");
        account.setEmailVerificationTokenExpiration(Instant.now().plus(Duration.ofDays(1)));
        accountRepository.save(account);

        EmailVerificationResult result = accountService.verifyEmail("test_token");
        assertEquals(EmailVerificationResult.VERIFIED, result);

        account = accountRepository.findByEmail(account.getEmail()).get();
        assertNull(account.getEmailVerificationToken());
        assertNull(account.getEmailVerificationTokenExpiration());
    }
}
