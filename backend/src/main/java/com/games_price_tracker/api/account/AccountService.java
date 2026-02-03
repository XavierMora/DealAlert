package com.games_price_tracker.api.account;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

import com.games_price_tracker.api.account.enums.SignInCodeResult;
import com.games_price_tracker.api.account.exceptions.AccountAuthErrorException;
import com.games_price_tracker.api.common.exceptions.TooManyRequestsException;
import com.games_price_tracker.api.email.SendEmailException;
import com.games_price_tracker.api.email.SendEmailService;
import com.games_price_tracker.api.session_token.SessionToken;
import com.games_price_tracker.api.session_token.SessionTokenService;

import io.github.bucket4j.Bucket;

import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final SendEmailService sendEmailService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Duration signInCodeValidDuration = Duration.ofMinutes(10);
    private final Duration intervalSendEmail = Duration.ofMinutes(1);
    private final SessionTokenService sessionTokenService;
    private final int maxTokens = 3;
    private final ConcurrentHashMap<String, String> accountRequest = new ConcurrentHashMap<>();
    private final AccountCacheService bucketsByEmail;

    public AccountService(AccountRepository accountRepository, SendEmailService sendEmailService, SessionTokenService sessionTokenService, AccountCacheService bucketsByEmail){
        this.accountRepository = accountRepository;
        this.sendEmailService = sendEmailService;
        this.sessionTokenService = sessionTokenService;
        this.bucketsByEmail = bucketsByEmail;
    }

    public ConcurrentHashMap<String,String> getAccountLockRequest(){
        return accountRequest;
    }

    private String generateSignInCode(){
        return String.valueOf(secureRandom.nextInt(100000, 1000000));
    }

    public void verifyRateLimit(String email){
        Bucket bucket = bucketsByEmail.getBucket(email);
        if(!bucket.tryConsume(1)) throw new TooManyRequestsException();        
    }

    @Transactional
    public SignInCodeResult signInCode(String email) throws SendEmailException{
        verifyRateLimit(email);
        Optional<Account> optionalAccount = accountRepository.findByEmail(email);
        Account account;
        
        if(optionalAccount.isEmpty()) account = new Account(email);
        else account = optionalAccount.get();
        
        String code=null;

        // Se verifica si se puede enviar otro codigo o hay que asignar uno nuevo
        if(account.getLastSignInCodeSentAt() != null && account.getLastSignInCodeSentAt().plus(intervalSendEmail).isAfter(Instant.now())) return SignInCodeResult.TOO_MANY_REQUESTS; // No paso el tiempo para reenviar el codigo

        // Se recupera el codigo si no expiro
        if(!account.signInCodeExpired()) code = account.getSignInCode();

        // En caso que no se pudo obtener un código válido
        if(code == null){
            code = generateSignInCode();
            account.assignSignInCode(code, signInCodeValidDuration);
        }

        sendEmailService.verificationEmail(account.getEmail(), code);

        account.setLastSignInCodeSentAt(Instant.now());
        accountRepository.save(account);

        return SignInCodeResult.SUCCESS;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    @Transactional
    public SessionToken verifyCode(String email, String code){
        verifyRateLimit(email);
        Account account = accountRepository.findByEmailAndSignInCode(email, code).orElseThrow(
            () -> new AccountAuthErrorException("Código incorrecto.")
        );

        if(account.signInCodeExpired()) throw new AccountAuthErrorException("Código expirado.");
        
        SessionToken token = sessionTokenService.createSessionToken(account);
        account.addToken(token, maxTokens);

        account.setSignInCode(null);
        account.setSignInCodeExpiration(null);
        account.setLastSignInCodeSentAt(null);
        // Se tiene cascade persist en el onetomany entonces cuando se persiste account, que es la entidad padre, tambien se persiste/guarda el token en la bd
        return token;
    }

    public void logout(String sessionToken){
        sessionTokenService.invalidateToken(UUID.fromString(sessionToken));
    }
}
