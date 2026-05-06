package com.games_price_tracker.api.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import com.games_price_tracker.api.email.SendEmailException;
import com.games_price_tracker.api.email.SendEmailService;
import com.games_price_tracker.api.session_token.SessionTokenService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

public class AccountServiceTest {
    @Mock private AccountRepository accountRepository;
    @Mock private SessionTokenService sessionTokenService;
    @Mock private AccountRateLimit accountRateLimit;
    @Mock private AccountEmailCooldown accountEmailCooldown;
    @Mock private SendEmailService sendEmailService;
    @InjectMocks private AccountService accountService;
    private final String emailTest = "test";

    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);    
    }

    @Test
    void shouldSendSignInCodeWhenAccountIsNotRegistered(){        
        given(accountRepository.findByEmail(emailTest)).willReturn(Optional.empty());
        
        accountService.sendSignInCode(emailTest);
        ArgumentCaptor<Account> accountArg = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountArg.capture());
        verify(sendEmailService).verificationEmail(eq(emailTest), anyString());
        verify(accountEmailCooldown).updateSignInEmailSentAt(eq(emailTest), eq(accountArg.getValue().getLastSignInCodeSentAt()));

        Account account = accountArg.getValue();
        assertNotNull(account.getSignInCode());
        assertNotNull(account.getSignInCodeExpectedExpiration());
        assertNotNull(account.getLastSignInCodeSentAt());
        assertEquals(emailTest, account.getEmail());
    }

    @Test
    void shouldHandleErrorSendingSignInCodeEmail(){
        Account account = new Account(emailTest);
        
        given(accountRepository.findByEmail(emailTest)).willReturn(Optional.of(account));
        doThrow(SendEmailException.class).when(sendEmailService).verificationEmail(eq(emailTest), anyString());

        assertThrows(SendEmailException.class, () -> accountService.sendSignInCode(emailTest));
        assertNull(account.getLastSignInCodeSentAt());
        assertNotNull(account.getSignInCode());
        verify(accountEmailCooldown).cleanSignInEmailCooldown(eq(emailTest));
    }

    @Test
    void shouldCreateNewSignInCodeWhenSignInCodeIsRequestedAfterLastExpectedSend(){
        Account account = new Account(emailTest);
        String testCode = "0";
        account.setSignInCode(testCode);
        given(accountEmailCooldown.getSignInEmailInterval()).willReturn(Duration.ofMinutes(2));
        account.setSignInCodeExpectedExpiration(Instant.now().plus(accountEmailCooldown.getSignInEmailInterval().dividedBy(2)));
        given(accountRepository.findByEmail(emailTest)).willReturn(Optional.of(account));

        accountService.sendSignInCode(emailTest);
        
        verify(accountEmailCooldown).updateSignInEmailSentAt(eq(emailTest), notNull());
        assertNotEquals(testCode, account.getSignInCode());
    }

    @Test
    void shouldNotCreateNewSignInCodeWhenSignInCodeIsRequestedBeforeLastExpectedSend(){
        Account account = new Account(emailTest);
        String testCode = "123456";
        account.setSignInCode(testCode);
        given(accountEmailCooldown.getSignInEmailInterval()).willReturn(Duration.ofMinutes(2));
        account.setSignInCodeExpectedExpiration(Instant.now().plus(accountEmailCooldown.getSignInEmailInterval().plusSeconds(2)));
        given(accountRepository.findByEmail(emailTest)).willReturn(Optional.of(account));

        accountService.sendSignInCode(emailTest);
        
        verify(accountEmailCooldown).updateSignInEmailSentAt(eq(emailTest), notNull());
        assertEquals(testCode, account.getSignInCode());
    }
}
