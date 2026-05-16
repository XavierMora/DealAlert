package com.games_price_tracker.api.account;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.games_price_tracker.api.account.exceptions.AccountAuthErrorException;
import com.games_price_tracker.api.account.exceptions.AuthError;
import com.games_price_tracker.api.account.sign_in_code.SignInCodeHandler;
import com.games_price_tracker.api.email.SendEmailException;
import com.games_price_tracker.api.email.SendEmailService;
import com.games_price_tracker.api.session_token.SessionToken;
import com.games_price_tracker.api.session_token.SessionTokenService;
import com.games_price_tracker.api.telegram_bot.TelegramError;
import com.games_price_tracker.api.telegram_bot.TelegramException;
import com.games_price_tracker.api.telegram_bot.TelegramTokenHandler;

import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final SendEmailService sendEmailService;
    private final SessionTokenService sessionTokenService;
    private final int maxTokens = 3;
    private final AccountRateLimit accountRateLimit;
    private final AccountEmailCooldown accountEmailCooldown;
    private final SignInCodeHandler signInCodeHandler;
    private final TelegramTokenHandler telegramTokenHandler;

    public AccountService(AccountRepository accountRepository, SendEmailService sendEmailService, SessionTokenService sessionTokenService, AccountRateLimit accountRateLimit, AccountEmailCooldown accountEmailCooldown, SignInCodeHandler signInCodeHandler, TelegramTokenHandler telegramTokenHandler){
        this.accountRepository = accountRepository;
        this.sendEmailService = sendEmailService;
        this.sessionTokenService = sessionTokenService;
        this.accountRateLimit = accountRateLimit;
        this.accountEmailCooldown = accountEmailCooldown;
        this.signInCodeHandler = signInCodeHandler;
        this.telegramTokenHandler = telegramTokenHandler;
    }

    @Transactional
    public void sendSignInCode(String email){
        accountRateLimit.checkAccountRequestLimit(email);
        
        Optional<Account> optionalAccount = accountRepository.findByEmail(email);
        Account account;
        
        if(optionalAccount.isEmpty()) account = new Account(email);
        else account = optionalAccount.get();

        accountEmailCooldown.checkSignInEmailCanBeSent(email);

        try {
            sendEmailService.verificationEmail(
                account.getEmail(), 
                signInCodeHandler.getOrCreate(account.getEmail())
            );
        } catch (SendEmailException e) {
            accountEmailCooldown.cleanSignInEmailCooldown(email);
            throw e;
        }

        accountEmailCooldown.updateSignInEmailSentAt(email, Instant.now());
        
        accountRepository.save(account);
    }

    @Transactional
    public void clearLastSignInCodeSentAt(String email){
        accountEmailCooldown.cleanSignInEmailCooldown(email);
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    @Transactional
    public SessionToken verifySignInCode(String email, String code){
        accountRateLimit.checkAccountRequestLimit(email);

        Account account = accountRepository.findByEmail(email).orElseThrow(
            () -> new AccountAuthErrorException(AuthError.EMAIL_NOT_FOUND, "Email no encontrado.")
        );

        accountRateLimit.checkVerificationCodeAttemptLimit(email);

        if(!signInCodeHandler.codeIsValid(email, code)) throw new AccountAuthErrorException(AuthError.INVALID_CODE, "Código inválido.");
        
        SessionToken token = sessionTokenService.createSessionToken(account);
        account.addToken(token, maxTokens);

        signInCodeHandler.deleteCode(email);

        // Se tiene cascade persist en el onetomany entonces cuando se persiste account, que es la entidad padre, tambien se persiste/guarda el token en la bd
        return token;
    }

    public String generateTelegramToken(Account account){
        if(account.getTelegramUserId() != null) throw new TelegramException(TelegramError.TELEGRAM_ALREADY_LINKED, "Ya existe una vinculación a una cuenta de telegram.");
            
        String token = telegramTokenHandler.getToken(account.getId());

        if(token == null) throw new TelegramException(TelegramError.TELEGRAM_TOKEN_CREATION_FAILED, "Error generando token.");

        return token;
    }
}
