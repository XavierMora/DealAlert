package com.games_price_tracker.api.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.games_price_tracker.api.account.exceptions.AccountAuthErrorException;
import com.games_price_tracker.api.account.exceptions.AuthError;
import com.games_price_tracker.api.account.sign_in_code.SignInCodeHandler;
import com.games_price_tracker.api.email.SendEmailException;
import com.games_price_tracker.api.email.SendEmailService;
import com.games_price_tracker.api.session_token.SessionToken;
import com.games_price_tracker.api.session_token.SessionTokenService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

public class AccountServiceTest {
    @Mock private AccountRepository accountRepository;
    @Mock private SessionTokenService sessionTokenService;
    @Mock private AccountRateLimit accountRateLimit;
    @Mock private AccountEmailCooldown accountEmailCooldown;
    @Mock private SendEmailService sendEmailService;
    @Mock private SignInCodeHandler signInCodeHandler;
    @InjectMocks private AccountService accountService;
    private final String emailTest = "test";

    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);    
    }

    @Test
    void shouldSendSignInCodeWhenAccountIsNotRegistered(){        
        given(accountRepository.findByEmail(emailTest)).willReturn(Optional.empty());
        given(signInCodeHandler.getOrCreate(emailTest)).willReturn("123456");

        accountService.sendSignInCode(emailTest);
        ArgumentCaptor<Account> accountArg = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountArg.capture());
        verify(sendEmailService).verificationEmail(eq(emailTest), eq("123456"));
        verify(accountEmailCooldown).updateSignInEmailSentAt(eq(emailTest), any(Instant.class));

        assertEquals(emailTest, accountArg.getValue().getEmail());
    }

    @Test
    void shouldHandleErrorSendingSignInCodeEmail(){
        Account account = new Account(emailTest);
        
        given(accountRepository.findByEmail(emailTest)).willReturn(Optional.of(account));
        given(signInCodeHandler.getOrCreate(emailTest)).willReturn("");
        doThrow(SendEmailException.class).when(sendEmailService).verificationEmail(eq(emailTest), anyString());

        assertThrows(SendEmailException.class, () -> accountService.sendSignInCode(emailTest));
        verify(accountEmailCooldown).cleanSignInEmailCooldown(eq(emailTest));
    }

    @Test
    void shouldReturnSessionTokenWhenCodeIsValid(){
        Account account = new Account(emailTest);
        String code = "123456";
        given(accountRepository.findByEmail(emailTest)).willReturn(Optional.of(account));
        given(signInCodeHandler.codeIsValid(emailTest, code)).willReturn(true);
        SessionToken token = new SessionToken(account, Duration.ofMinutes(10));
        given(sessionTokenService.createSessionToken(account)).willReturn(token);

        SessionToken tokenReturned = accountService.verifySignInCode(emailTest, code);
        assertEquals(token, tokenReturned);
        assertEquals(1, account.getSessionTokens().size());
        assertEquals(tokenReturned, account.getSessionTokens().get(0));
        verify(signInCodeHandler).deleteCode(eq(emailTest));
    }

    @Test
    void shouldThrowAccountAuthErrorExceptionWhenEmailNotFound(){
        given(accountRepository.findByEmail(emailTest)).willReturn(Optional.empty());

        AccountAuthErrorException ex = assertThrows(AccountAuthErrorException.class, () -> accountService.verifySignInCode(emailTest, "123456"));
        assertEquals(AuthError.EMAIL_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void shouldThrowAccountAuthErrorExceptionInvalidCodeWhenCodeIsNotValid(){
        Account account = new Account(emailTest);
        String code = "123456";
        given(signInCodeHandler.codeIsValid(emailTest, code)).willReturn(false);
        given(accountRepository.findByEmail(emailTest)).willReturn(Optional.of(account));

        AccountAuthErrorException ex = assertThrows(AccountAuthErrorException.class, () -> accountService.verifySignInCode(emailTest, code));
        assertEquals(AuthError.INVALID_CODE, ex.getErrorCode());
    }
}
