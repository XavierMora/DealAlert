package com.games_price_tracker.api.account;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.games_price_tracker.api.account.exceptions.AccountAuthErrorException;
import com.games_price_tracker.api.common.exceptions.TooManyRequestsException;
import com.games_price_tracker.api.email.SendEmailException;
import com.games_price_tracker.api.session_token.SessionToken;
import com.games_price_tracker.api.session_token.SessionTokenService;

import io.github.bucket4j.Bucket;

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

    public void verifyRateLimit(String email){
        Bucket bucket = accountCacheService.getBucket(email);
        if(!bucket.tryConsume(1)) throw new TooManyRequestsException();        
    }

    public void signInCode(String email) throws SendEmailException{
        verifyRateLimit(email);
        accountCacheService.sendSignInCode(email);
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    @CacheEvict(cacheNames = "email-sent")
    @Transactional
    public SessionToken verifyCode(String email, String code){
        verifyRateLimit(email);
        Account account = accountRepository.findByEmailAndSignInCode(email, code).orElseThrow(
            () -> new AccountAuthErrorException("Código incorrecto.")
        );

        if(account.signInCodeExpired(intervalSendEmail)) throw new AccountAuthErrorException("Código expirado.");
        
        SessionToken token = sessionTokenService.createSessionToken(account);
        account.addToken(token, maxTokens);

        account.setSignInCode(null);
        account.setSignInCodeExpectedExpiration(null);
        account.setLastSignInCodeSentAt(null);
        
        // Se tiene cascade persist en el onetomany entonces cuando se persiste account, que es la entidad padre, tambien se persiste/guarda el token en la bd
        return token;
    }
}
