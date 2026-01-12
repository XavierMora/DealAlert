package com.games_price_tracker.api.account;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.games_price_tracker.api.account.dtos.SignInBody;
import com.games_price_tracker.api.account.enums.SignInCodeResult;
import com.games_price_tracker.api.email.SendEmailService;

import jakarta.transaction.Transactional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final SendEmailService sendEmailService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Duration signInCodeValidDuration = Duration.ofMinutes(10);
    private final Duration intervalSendEmail = Duration.ofMinutes(1);

    public AccountService(AccountRepository accountRepository, SendEmailService sendEmailService){
        this.accountRepository = accountRepository;
        this.sendEmailService = sendEmailService;
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
            
        String lastDeviceId = account.getDeviceIdLastCodeAssign();
         
        if(lastDeviceId != null && lastDeviceId.equals(deviceId)){
            if(account.getLastSignInCodeSentAt().plus(intervalSendEmail).isAfter(Instant.now())) return SignInCodeResult.TOO_MANY_REQUESTS; // Se ejecuta si el dispositivo es el mismo y no paso el tiempo para reenviar el codigo

            Instant codeExpiration = account.getSignInCodeExpiration();
            // Se recupera el codigo si no expiro
            if(codeExpiration != null && codeExpiration.isAfter(Instant.now())) code = account.getSignInCode();
        }

        if(code == null){
            code = generateSignInCode();
            account.assignSignInCode(code, signInCodeValidDuration, deviceId);
        }

        sendEmailService.verificationEmail(account.getEmail(), code);
        account.setLastSignInCodeSentAt(Instant.now());

        accountRepository.save(account);
        return SignInCodeResult.SUCCESS;
    }
}
