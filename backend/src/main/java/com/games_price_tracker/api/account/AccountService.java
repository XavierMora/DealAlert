package com.games_price_tracker.api.account;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.games_price_tracker.api.account.dtos.SignInBody;
import com.games_price_tracker.api.account.dtos.VerifyCodeBody;
import com.games_price_tracker.api.account.enums.SignInCodeResult;
import com.games_price_tracker.api.account.exceptions.AccountAuthErrorException;
import com.games_price_tracker.api.email.SendEmailService;
import com.games_price_tracker.api.session_token.SessionToken;
import com.games_price_tracker.api.session_token.SessionTokenService;

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

    public AccountService(AccountRepository accountRepository, SendEmailService sendEmailService, SessionTokenService sessionTokenService){
        this.accountRepository = accountRepository;
        this.sendEmailService = sendEmailService;
        this.sessionTokenService = sessionTokenService;
    }

    private String generateSignInCode(){
        return String.valueOf(secureRandom.nextInt(100000, 1000000));
    }

    @Transactional
    public SignInCodeResult signInCode(SignInBody signInBody, String deviceId){
        Optional<Account> optionalAccount = accountRepository.findByEmail(signInBody.email());
        Account account;
        String code=null;

        if(optionalAccount.isEmpty()) account = new Account(signInBody.email());
        else account = optionalAccount.get();
            
        String lastDeviceId = account.getLastDeviceIdAssignedCode();
         
        if(lastDeviceId != null && lastDeviceId.equals(deviceId)){
            if(account.getLastSignInCodeSentAt() != null && account.getLastSignInCodeSentAt().plus(intervalSendEmail).isAfter(Instant.now())) return SignInCodeResult.TOO_MANY_REQUESTS; // Se ejecuta si el dispositivo es el mismo y no paso el tiempo para reenviar el codigo

            // Se recupera el codigo si no expiro
            if(!account.signInCodeExpired()) code = account.getSignInCode();
        }

        // En caso que no se pudo obtener un código válido
        if(code == null){
            code = generateSignInCode();
            account.assignSignInCode(code, signInCodeValidDuration, deviceId);
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
    public SessionToken verifyCode(VerifyCodeBody verifyCodeBody, String deviceId){
        Account account = accountRepository.findByEmailAndSignInCodeAndLastDeviceIdAssignedCode(verifyCodeBody.email(), verifyCodeBody.code(), deviceId).orElseThrow(
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

    public void logout(UUID sessionToken){
        sessionTokenService.invalidateToken(sessionToken);
    }
}
