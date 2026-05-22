package com.games_price_tracker.api.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.games_price_tracker.api.core.exceptions.ExceptionsHandlerController;
import com.games_price_tracker.api.core.response.ErrorCode;
import com.games_price_tracker.api.session_token.SessionToken;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.core.StringContains.containsString;

import java.time.Duration;

public class AccountControllerTest {
    @Mock AccountService accountService;
    @InjectMocks AccountController accountController;
    private MockMvc mockMvc;

    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders
        .standaloneSetup(accountController)
        .setControllerAdvice(new ExceptionsHandlerController())
        .build();
    }

    @Test
    void shouldSendSignInCode() throws Exception{
        mockMvc.perform(
            post("/account/sign-in-code")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email": "test@test"}
            """)
        ).andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            jsonPath("$.success").value(true),
            jsonPath("$.message").isNotEmpty()
        );
    }

    @Test
    void shouldRejectWhenEmailIsInvalid() throws Exception{
        mockMvc.perform(
            post("/account/sign-in-code")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email": "test"}
            """)
        ).andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            jsonPath("$.success").value(false),
            jsonPath("$.error").value(ErrorCode.INVALID_DATA.getErrorCode()),
            jsonPath("$.data.email").isNotEmpty()
        );
    }

    @Test 
    void shouldVerifyCode() throws Exception{
        String email = "test@test";
        String code = "123456";
        SessionToken token = new SessionToken(null, Duration.ofMinutes(10));
        given(accountService.verifySignInCode(email, code)).willReturn(token);

        mockMvc.perform(
            post("/account/verify-code")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email": "%s", "code": "%s"}
            """.formatted(email, code))
        ).andExpectAll(
            status().isNoContent(),
            header().string(HttpHeaders.SET_COOKIE, containsString("SESSION="+token.getToken().toString())),
            header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")),
            header().string(HttpHeaders.SET_COOKIE, containsString("Secure")),
            header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age"))
        );
    }

    @Test 
    void shouldRejectWhenVerifyCodeBodyIsInvalid() throws Exception{
        mockMvc.perform(
            post("/account/verify-code")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email": "test", "code": "123"}
            """)
        ).andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.APPLICATION_JSON),
            jsonPath("$.success").value(false),
            jsonPath("$.error").value(ErrorCode.INVALID_DATA.getErrorCode()),
            jsonPath("$.data.email").isNotEmpty(),
            jsonPath("$.data.code").isNotEmpty()
        );
    }
}
