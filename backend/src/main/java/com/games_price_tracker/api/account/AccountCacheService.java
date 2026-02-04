package com.games_price_tracker.api.account;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.games_price_tracker.api.email.SendEmailService;

import io.github.bucket4j.Bucket;

@Service
public class AccountCacheService {
    private final AccountRepository accountRepository;
    private final SendEmailService sendEmailService;
    private final Duration signInCodeValidDuration = Duration.ofMinutes(10);
    @Value("${app.email.sign-in-code.interval}")
    private Duration intervalSendEmail;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Cache emailSentCache;

    public AccountCacheService(AccountRepository accountRepository, SendEmailService sendEmailService,CacheManager cacheManager){
        this.emailSentCache = cacheManager.getCache("email-sent");
        this.accountRepository = accountRepository;
        this.sendEmailService = sendEmailService; 
    }

    public long emailSentAgo(String email){
        ValueWrapper wrapperLastSent = emailSentCache.get(email);

        if(wrapperLastSent == null) return -1;

        return ((Instant) wrapperLastSent.get()).until(Instant.now(), ChronoUnit.SECONDS);
    }

    // Cacheo de bucket con 1 token/seg de un email
    @Cacheable(cacheNames = "account-rate-limit", sync = true)
    public Bucket getBucket(String email){
        return Bucket.builder()
        .addLimit(limit -> limit.capacity(1).refillIntervally(1, Duration.ofSeconds(2)).initialTokens(1))
        .build();
    }

    private String generateSignInCode(){
        return String.valueOf(secureRandom.nextInt(100000, 1000000));
    }

    // Se cachea la fecha en que se envió el email y este expira cuando pase el intervalo
    @Cacheable(cacheNames = "email-sent", sync = true)
    @Transactional
    public Instant sendSignInCode(String email){
        Optional<Account> optionalAccount = accountRepository.findByEmail(email);
        Account account;
        
        if(optionalAccount.isEmpty()) account = new Account(email);
        else account = optionalAccount.get();

        String code;
        // Se recupera el codigo si no expiro
        if(!account.signInCodeExpired(intervalSendEmail)){
            code = account.getSignInCode();
        }else{
            code = generateSignInCode();
            account.assignSignInCode(code, signInCodeValidDuration);
        }

        sendEmailService.verificationEmail(account.getEmail(), code);

        account.setLastSignInCodeSentAt(Instant.now());
        accountRepository.save(account);

        return account.getLastSignInCodeSentAt();
    }
}
