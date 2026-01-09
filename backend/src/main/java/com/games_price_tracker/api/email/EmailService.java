package com.games_price_tracker.api.email;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class EmailService {
    private final EmailRepository emailRepository;
    private final Duration tokenValidTime = Duration.ofDays(1);
    private final Duration intervalSendEmail = Duration.ofHours(1);
    private final SendEmailService sendEmailService;

    public EmailService(EmailRepository emailRepository, SendEmailService sendEmailService){
        this.emailRepository = emailRepository;
        this.sendEmailService = sendEmailService;
    }

    private String generateVerificationToken(){
        return UUID.randomUUID().toString();
    }

    private void assignVerificationToken(Email email){
        String token = generateVerificationToken();

        email.setVerificationToken(token);
        email.setVerificationTokenExpiration(Instant.now().plus(tokenValidTime));
    }

    @Transactional
    public EmailVerificationStatus emailVerification(String emailAddress){
        Optional<Email> optionalEmail = emailRepository.findByAddress(emailAddress);
        Email email;

        if(optionalEmail.isEmpty()) email = new Email(emailAddress);
        else email = optionalEmail.get();

        if(email.getVerified()) return EmailVerificationStatus.EMAIL_VERIFIED;

        Instant tokenExpiration = email.getVerificationTokenExpiration();
        
        if(tokenExpiration != null && tokenExpiration.isAfter(Instant.now())){ // El token no expiró 
            Instant lastEmailSentAt = email.getLastVerificationEmailSentAt();

            if(lastEmailSentAt.plus(intervalSendEmail).isAfter(Instant.now())) return EmailVerificationStatus.VERIFICATION_EMAIL_ALREADY_SENT;  // El tiempo para reenviar email todavia no paso
        }else{
            assignVerificationToken(email);
            email = emailRepository.save(email);
        }

        sendEmailService.verificationEmail(emailAddress, email.getVerificationToken());
        email.setLastVerificationEmailSentAt(Instant.now());
        emailRepository.save(email);
        return EmailVerificationStatus.VERIFICATION_EMAIL_SENT_NOW;
    }
}
