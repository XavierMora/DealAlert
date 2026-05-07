package com.games_price_tracker.api.account;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.games_price_tracker.api.account.exceptions.AccountAuthErrorException;
import com.games_price_tracker.api.account.exceptions.AuthError;
import com.games_price_tracker.api.email.SendEmailException;
import com.games_price_tracker.api.email.SendEmailService;
import com.games_price_tracker.api.session_token.SessionToken;
import com.games_price_tracker.api.session_token.SessionTokenService;

import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final SendEmailService sendEmailService;
    private final Duration signInCodeValidDuration = Duration.ofMinutes(10);
    private final SessionTokenService sessionTokenService;
    private final int maxTokens = 3;
    private final AccountRateLimit accountRateLimit;
    private final SecureRandom secureRandom = new SecureRandom();
    private final AccountEmailCooldown accountEmailCooldown;

    public AccountService(AccountRepository accountRepository, SendEmailService sendEmailService, SessionTokenService sessionTokenService, AccountRateLimit accountRateLimit, AccountEmailCooldown accountEmailCooldown){
        this.accountRepository = accountRepository;
        this.sendEmailService = sendEmailService;
        this.sessionTokenService = sessionTokenService;
        this.accountRateLimit = accountRateLimit;
        this.accountEmailCooldown = accountEmailCooldown;
    }

    @Transactional
    public void sendSignInCode(String email){
        accountRateLimit.checkAccountRequestLimit(email);
        
        Optional<Account> optionalAccount = accountRepository.findByEmail(email);
        Account account;
        
        if(optionalAccount.isEmpty()) account = new Account(email);
        else account = optionalAccount.get();

        accountEmailCooldown.checkSignInEmailCanBeSent(email, account.getLastSignInCodeSentAt());

        try {
            sendEmailService.verificationEmail(account.getEmail(), getOrCreateSignInCode(account));
        } catch (SendEmailException e) {
            accountEmailCooldown.cleanSignInEmailCooldown(email);
            throw e;
        }

        account.setLastSignInCodeSentAt(Instant.now());
        accountEmailCooldown.updateSignInEmailSentAt(email, account.getLastSignInCodeSentAt());
        
        accountRepository.save(account);
    }

    private String getOrCreateSignInCode(Account account){
        if(!shouldGenerateNewSignInCode(account)) return account.getSignInCode();
        
        String code = String.valueOf(secureRandom.nextInt(100000, 1000000));
        account.assignSignInCode(code, signInCodeValidDuration);
        
        return code;
    }

    private boolean shouldGenerateNewSignInCode(Account account){
        if(account.getSignInCodeExpectedExpiration() == null) return true;

        if(account.signInCodeExpired()) return true;
        
        Instant expectedLastSignInEmailSendTime = account.getSignInCodeExpectedExpiration().minus(accountEmailCooldown.getSignInEmailCooldown());

        // Se genera un nuevo código si el email que se quiere enviar está despues de 
        // expiración código - intervalo de envío de email
        return Instant.now().isAfter(expectedLastSignInEmailSendTime);         
    }

    @Transactional
    public void clearLastSignInCodeSentAt(String email){
        accountEmailCooldown.cleanSignInEmailCooldown(email);
        accountRepository.updateLastSignInCodeSentAtByEmail(email, null);
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    @Transactional
    public SessionToken verifyCode(String email, String code){
        accountRateLimit.checkAccountRequestLimit(email);

        Account account = accountRepository.findByEmail(email).orElseThrow(
            () -> new AccountAuthErrorException(AuthError.EMAIL_NOT_FOUND, "Email no encontrado.")
        );

        accountRateLimit.checkVerificationCodeAttemptLimit(email);

        if(account.getSignInCode() == null || !account.getSignInCode().equals(code)) throw new AccountAuthErrorException(AuthError.INCORRECT_CODE, "Código incorrecto.");

        if(account.signInCodeExpired()) throw new AccountAuthErrorException(AuthError.EXPIRED_CODE, "Código expirado.");
        
        SessionToken token = sessionTokenService.createSessionToken(account);
        account.addToken(token, maxTokens);

        account.setSignInCode(null);
        account.setSignInCodeExpectedExpiration(null);
        
        // Se tiene cascade persist en el onetomany entonces cuando se persiste account, que es la entidad padre, tambien se persiste/guarda el token en la bd
        return token;
    }
}
