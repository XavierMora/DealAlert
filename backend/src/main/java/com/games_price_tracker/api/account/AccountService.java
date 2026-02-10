package com.games_price_tracker.api.account;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.games_price_tracker.api.account.exceptions.AccountAuthErrorException;
import com.games_price_tracker.api.account.exceptions.AuthError;
import com.games_price_tracker.api.common.exceptions.TooManyRequestsException;
import com.games_price_tracker.api.email.SendEmailException;
import com.games_price_tracker.api.session_token.SessionToken;
import com.games_price_tracker.api.session_token.SessionTokenService;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    @Value("${app.email.sign-in-code.interval}")
    private Duration intervalSendEmail;
    private final SessionTokenService sessionTokenService;
    private final int maxTokens = 3;
    private final AccountCacheService accountCacheService;
    
    public AccountService(AccountRepository accountRepository, SessionTokenService sessionTokenService, AccountCacheService accountCacheService){
        this.accountRepository = accountRepository;
        this.sessionTokenService = sessionTokenService;
        this.accountCacheService = accountCacheService;
    }

    public void verifyCodeRateLimit(String email){
        Bucket bucket = accountCacheService.getBucket(email);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if(!probe.isConsumed()) throw new TooManyRequestsException(probe.getNanosToWaitForRefill(), TimeUnit.NANOSECONDS, "Muchos intentos. Intentar más tarde."); 
    }

    public void signInCode(String email) throws SendEmailException{
        accountCacheService.sendSignInCode(email);
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    @CacheEvict(cacheNames = "email-sent")
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
