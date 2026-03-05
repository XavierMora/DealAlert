package com.games_price_tracker.api.account;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.games_price_tracker.api.account.exceptions.AccountAuthErrorException;
import com.games_price_tracker.api.account.exceptions.AuthError;
import com.games_price_tracker.api.common.exceptions.TooManyRequestsException;
import com.games_price_tracker.api.email.SendEmailException;
import com.games_price_tracker.api.email.SendEmailService;
import com.games_price_tracker.api.email.config.EmailConfigProperties;
import com.games_price_tracker.api.session_token.SessionToken;
import com.games_price_tracker.api.session_token.SessionTokenService;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final SendEmailService sendEmailService;
    private final Duration signInCodeValidDuration = Duration.ofMinutes(10);
    private final Duration intervalSendEmail;
    private final SessionTokenService sessionTokenService;
    private final int maxTokens = 3;
    private final AccountCacheService accountCacheService;
    private final SecureRandom secureRandom = new SecureRandom();
    
    public AccountService(AccountRepository accountRepository, SendEmailService sendEmailService, SessionTokenService sessionTokenService, AccountCacheService accountCacheService, EmailConfigProperties emailConfigProperties){
        this.accountRepository = accountRepository;
        this.sendEmailService = sendEmailService;
        this.sessionTokenService = sessionTokenService;
        this.accountCacheService = accountCacheService;
        this.intervalSendEmail = emailConfigProperties.getSignInCodeInterval();
    }

    public void verifyCodeRateLimit(String email){
        Bucket bucket = accountCacheService.getBucketVerifyCode(email);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if(!probe.isConsumed()) throw new TooManyRequestsException(
            probe.getNanosToWaitForRefill(), 
            TimeUnit.NANOSECONDS, 
            "Muchos intentos. Probar más tarde.",
            AuthError.MAX_ATTEMPTS_REACHED
        ); 
    }

    @Transactional
    public Instant sendSignInCode(String email) throws SendEmailException{
        Optional<Account> optionalAccount = accountRepository.findByEmail(email);
        Account account;
        
        if(optionalAccount.isEmpty()) account = new Account(email);
        else account = optionalAccount.get();

        String code;
        // Se recupera el codigo si no expiro
        if(!account.signInCodeExpired(intervalSendEmail)){
            code = account.getSignInCode();
        }else{
            code = String.valueOf(secureRandom.nextInt(100000, 1000000));
            account.assignSignInCode(code, signInCodeValidDuration);
        }

        sendEmailService.verificationEmail(account.getEmail(), code);

        account.setLastSignInCodeSentAt(Instant.now());
        accountRepository.save(account);

        return account.getLastSignInCodeSentAt();
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    @CacheEvict(cacheNames = "email-sent", key = "#email")
    @Transactional
    public SessionToken verifyCode(String email, String code){
        Account account = accountRepository.findByEmailAndSignInCode(email, code).orElseThrow(
            () -> new AccountAuthErrorException(AuthError.INCORRECT_CODE, "Código incorrecto.")
        );

        if(account.signInCodeExpired(intervalSendEmail)) throw new AccountAuthErrorException(AuthError.EXPIRED_CODE, "Código expirado.");
        
        SessionToken token = sessionTokenService.createSessionToken(account);
        account.addToken(token, maxTokens);

        account.setSignInCode(null);
        account.setSignInCodeExpectedExpiration(null);
        account.setLastSignInCodeSentAt(null);
        
        // Se tiene cascade persist en el onetomany entonces cuando se persiste account, que es la entidad padre, tambien se persiste/guarda el token en la bd
        return token;
    }
}
