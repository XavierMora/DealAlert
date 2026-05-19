package com.games_price_tracker.api.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @Mock private AccountRepository accountRepository;
    @Mock private SessionTokenService sessionTokenService;
    @Mock private AccountRateLimit accountRateLimit;
    @Mock private AccountEmailCooldown accountEmailCooldown;
    @Mock private SendEmailService sendEmailService;
    @Mock private SignInCodeHandler signInCodeHandler;
    @Mock private TelegramTokenHandler telegramTokenHandler;
    @InjectMocks private AccountService accountService;
    private final String emailTest = "test";

    @Test
    void shouldHandleErrorSendingSignInCodeEmail(){        
        given(signInCodeHandler.getOrCreate(emailTest)).willReturn("");
        doThrow(SendEmailException.class).when(sendEmailService).verificationEmail(eq(emailTest), anyString());

        assertThrows(SendEmailException.class, () -> accountService.sendSignInCode(emailTest));
        verify(signInCodeHandler).getOrCreate(eq(emailTest));
        verify(accountEmailCooldown).cleanSignInEmailCooldown(eq(emailTest));
    }

    @Test
    void shouldReturnSessionTokenAndCreateAccountWhenCodeIsValid(){
        ArgumentCaptor<Account> accountArg = ArgumentCaptor.forClass(Account.class);
        String code = "123456";
        given(accountRepository.findByEmail(emailTest)).willReturn(Optional.empty());
        given(signInCodeHandler.codeIsValid(emailTest, code)).willReturn(true);
        SessionToken token = new SessionToken();
        token.setToken(UUID.randomUUID());
        given(sessionTokenService.createSessionToken(any(Account.class))).willReturn(token);

        SessionToken tokenReturned = accountService.verifySignInCode(emailTest, code);

        verify(sessionTokenService).createSessionToken(accountArg.capture());
        Account account = accountArg.getValue();
        
        assertEquals(1, account.getSessionTokens().size());
        assertEquals(tokenReturned, account.getSessionTokens().get(0));
        verify(signInCodeHandler).deleteCode(eq(emailTest));
        verify(accountRepository).save(eq(account));
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
        verify(accountRepository, never()).save(eq(account));
    }

    @Test
    void shouldThrowWhenCodeIsNotValid(){
        String code = "123456";
        given(signInCodeHandler.codeIsValid(emailTest, code)).willReturn(false);

        AccountAuthErrorException ex = assertThrows(AccountAuthErrorException.class, () -> accountService.verifySignInCode(emailTest, code));
        assertEquals(AuthError.INVALID_CODE, ex.getErrorCode());
    }

    @Test
    void shouldCreateAndReturnTelegramToken(){
        Account account = new Account();
        account.setId(1L);
        given(telegramTokenHandler.getToken(account.getId())).willReturn("1");

        assertEquals("1", accountService.generateTelegramToken(account));
        verify(telegramTokenHandler).getToken(eq(account.getId()));
    }

    @Test
    void shouldThrowWhenAccountAlreadyHaveTelegramLinked(){
        Account account = new Account();
        account.setTelegramUserId(1L);
        
        TelegramException ex = assertThrows(TelegramException.class, () -> accountService.generateTelegramToken(account));
        assertEquals(TelegramError.TELEGRAM_ALREADY_LINKED, ex.getErrorCode());
    }

    @Test
    void shouldThrowWhenTelegramTokenCreationFail(){
        Account account = new Account();
        account.setId(1L);
        given(telegramTokenHandler.getToken(account.getId())).willReturn(null);

        TelegramException ex = assertThrows(TelegramException.class, () -> accountService.generateTelegramToken(account));
        assertEquals(TelegramError.TELEGRAM_TOKEN_CREATION_FAILED, ex.getErrorCode());
        verify(telegramTokenHandler).getToken(eq(account.getId()));
    }

    @Test
    void shouldReturnFalseWhenTelegramTokenHandlerReturnsNull(){
        given(telegramTokenHandler.getAccountId("token")).willReturn(null);

        assertFalse(accountService.linkTelegramAccount("token", null));
    }

    @Test
    void shouldReturnFalseWhenAccountIdDoesNotExist(){
        given(telegramTokenHandler.getAccountId("token")).willReturn(1L);
        given(accountRepository.findById(1L)).willReturn(Optional.empty());

        assertFalse(accountService.linkTelegramAccount("token", null));
    }

    @Test
    void shouldReturnTrueAndLinkTelegramAccount(){
        Account account = new Account();
        given(telegramTokenHandler.getAccountId("token")).willReturn(1L);
        given(accountRepository.findById(1L)).willReturn(Optional.of(account));

        assertTrue(accountService.linkTelegramAccount("token", 1L));
        assertEquals(1L, account.getTelegramUserId());
    }
}
