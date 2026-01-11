package com.games_price_tracker.api.account;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.games_price_tracker.api.account.email_verification_enum.EmailVerificationResult;
import com.games_price_tracker.api.account.email_verification_enum.EmailVerificationStatus;
import com.games_price_tracker.api.email.SendEmailService;

import jakarta.transaction.Transactional;

@Service
public class AccountService {
    private final AccountRepository emailRepository;
    private final Duration tokenValidTime = Duration.ofDays(1);
    private final Duration intervalSendEmail = Duration.ofHours(1);
    private final SendEmailService sendEmailService;

    public AccountService(AccountRepository emailRepository, SendEmailService sendEmailService){
        this.emailRepository = emailRepository;
        this.sendEmailService = sendEmailService;
    }

    private String generateVerificationToken(){
        return UUID.randomUUID().toString();
    }

    private void assignVerificationToken(Account email){
        String token = generateVerificationToken();

        email.setEmailVerificationToken(token);
        email.setEmailVerificationTokenExpiration(Instant.now().plus(tokenValidTime));
    }

    @Transactional
    public EmailVerificationStatus emailVerification(String emailAddress){
        Optional<Account> optionalEmail = emailRepository.findByEmail(emailAddress);
        Account email;

        if(optionalEmail.isEmpty()) email = new Account(emailAddress);
        else email = optionalEmail.get();

        if(email.getEmailVerified()) return EmailVerificationStatus.EMAIL_ALREADY_VERIFIED;

        Instant tokenExpiration = email.getEmailVerificationTokenExpiration();
        
        if(tokenExpiration != null && tokenExpiration.isAfter(Instant.now())){ // El token no expiró 
            Instant lastEmailSentAt = email.getLastVerificationEmailSentAt();

            if(lastEmailSentAt.plus(intervalSendEmail).isAfter(Instant.now())) return EmailVerificationStatus.VERIFICATION_EMAIL_ALREADY_SENT;  // El tiempo para reenviar email todavia no paso
        }else{
            // Se genera el token
            assignVerificationToken(email);
            email = emailRepository.save(email);
        }

        sendEmailService.verificationEmail(emailAddress, email.getEmailVerificationToken());
        email.setLastVerificationEmailSentAt(Instant.now());
        emailRepository.save(email);
        return EmailVerificationStatus.VERIFICATION_EMAIL_SENT_NOW;
    }

    @Transactional
    public EmailVerificationResult verifyEmail(String token){
        Optional<Account> optionalEmail = emailRepository.findByEmailVerificationToken(token);

        if(optionalEmail.isEmpty()) return EmailVerificationResult.TOKEN_NOT_FOUND;

        Account email = optionalEmail.get();
        
        Instant tokenExpiration = email.getEmailVerificationTokenExpiration();

        if(tokenExpiration.isBefore(Instant.now())) return EmailVerificationResult.TOKEN_EXPIRED;

        email.setEmailVerified(true);
        // Se limpia el token de verificación
        email.setEmailVerificationToken(null);
        email.setEmailVerificationTokenExpiration(null);

        emailRepository.save(email);
        
        return EmailVerificationResult.VERIFIED;
    }
}
