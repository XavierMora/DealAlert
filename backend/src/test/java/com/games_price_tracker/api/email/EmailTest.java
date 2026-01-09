package com.games_price_tracker.api.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
        email.setVerificationTokenExpiration(Instant.now().minus(1L, ChronoUnit.DAYS));
        email = emailRepository.save(email);

        EmailVerificationStatus status = emailService.emailVerification(email.getAddress());
        email = emailRepository.findByAddress(email.getAddress()).get();

        assertEquals(EmailVerificationStatus.VERIFICATION_EMAIL_SENT_NOW, status);
        assertNotEquals("test_token", email.getVerificationToken());
    }
}
