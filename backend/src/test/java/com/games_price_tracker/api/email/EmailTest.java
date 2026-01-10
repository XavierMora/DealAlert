package com.games_price_tracker.api.email;

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

import com.games_price_tracker.api.email.email_verification_enums.EmailVerificationResult;
import com.games_price_tracker.api.email.email_verification_enums.EmailVerificationStatus;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
public class EmailTest {
    private final EmailService emailService;
    private final EmailRepository emailRepository;

    @Autowired
    EmailTest(EmailService emailService, EmailRepository emailRepository){
        this.emailService = emailService;
        this.emailRepository = emailRepository;
    }

    @Test
    void emailVerificationWithoutToken(){
        String testEmail = "test@email";

        EmailVerificationStatus status = emailService.emailVerification(testEmail);

        assertEquals(EmailVerificationStatus.VERIFICATION_EMAIL_SENT_NOW, status);

        Email email = emailRepository.findByAddress(testEmail).get();

        assertNotNull(email.getVerificationToken());
        assertNotNull(email.getVerificationTokenExpiration());
        assertNotNull(email.getLastVerificationEmailSentAt());
        assertFalse(email.getVerified());
    }

    @Test
    void emailVerificationWithEmailSent(){
        Email email = new Email("test@email");
        emailRepository.save(email);
        emailService.emailVerification(email.getAddress());
        email = emailRepository.findByAddress(email.getAddress()).get();

        EmailVerificationStatus status = emailService.emailVerification(email.getAddress());
        assertEquals(EmailVerificationStatus.VERIFICATION_EMAIL_ALREADY_SENT, status);
    }

    @Test
    void emailVerificationWithTokenExpired(){
        Email email = new Email("test@email");
        email.setVerificationToken("test_token");
        email.setVerificationTokenExpiration(Instant.now().minus(Duration.ofDays(1)));
        email = emailRepository.save(email);

        EmailVerificationStatus status = emailService.emailVerification(email.getAddress());
        email = emailRepository.findByAddress(email.getAddress()).get();

        assertEquals(EmailVerificationStatus.VERIFICATION_EMAIL_SENT_NOW, status);
        assertNotEquals("test_token", email.getVerificationToken());
    }

    @Test
    void verifyEmailShouldReturnTokenNotFound(){
        EmailVerificationResult result = emailService.verifyEmail("test");
        assertEquals(EmailVerificationResult.TOKEN_NOT_FOUND, result);
    }

    @Test
    void verifyEmailShouldReturnTokenExpired(){
        Email email = new Email("test");
        email.setVerificationToken("test_token");
        email.setVerificationTokenExpiration(Instant.now().minus(Duration.ofDays(1)));
        emailRepository.save(email);

        EmailVerificationResult result = emailService.verifyEmail("test_token");
        assertEquals(EmailVerificationResult.TOKEN_EXPIRED, result);
    }

    @Test
    void verifyEmailShouldReturnVerified(){
        Email email = new Email("test");
        email.setVerificationToken("test_token");
        email.setVerificationTokenExpiration(Instant.now().plus(Duration.ofDays(1)));
        emailRepository.save(email);

        EmailVerificationResult result = emailService.verifyEmail("test_token");
        assertEquals(EmailVerificationResult.VERIFIED, result);

        email = emailRepository.findByAddress(email.getAddress()).get();
        assertNull(email.getVerificationToken());
        assertNull(email.getVerificationTokenExpiration());
    }
}
