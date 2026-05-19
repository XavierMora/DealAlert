package com.games_price_tracker.api.telegram_bot;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.games_price_tracker.api.account.AccountService;
import com.games_price_tracker.api.core.exceptions.ExceptionsHandlerController;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TelegramWebhookTest {
    private MockMvc mockMvc;
    @Mock AccountService accountService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup(){
        mockMvc = MockMvcBuilders
        .standaloneSetup(new TelegramWebhook("123", accountService))
        .setControllerAdvice(new ExceptionsHandlerController())
        .build();
    }

    @Test
    void shouldReturnForbiddenWhenSecretTokenDoesNotMatch() throws Exception{
        mockMvc.perform(
            post("/telegram/webhook")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new Update()))
            .header("X-Telegram-Bot-Api-Secret-Token", "0")
        ).andExpectAll(
            status().isForbidden()
        );
    }

    @Test
    void shouldReturnNoContentWhenCommandIsWrong() throws Exception{
        Update update = new Update();
        Message msg = new Message();
        msg.setText("/a 123");
        update.setMessage(msg);

        mockMvc.perform(
            post("/telegram/webhook")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(update))
            .header("X-Telegram-Bot-Api-Secret-Token", "123")
        ).andExpectAll(
            status().isNoContent()
        );
    }

    @Test
    void shouldReturnSuccessMsgWhenVerifyTokenSuccess() throws Exception{
        Update update = new Update();
        Message msg = new Message();
        msg.setText("/start 123");
        User user = new User();
        user.setId(1L);
        msg.setFrom(user);
        update.setMessage(msg);

        given(accountService.linkTelegramAccount("123", 1L)).willReturn(true);

        mockMvc.perform(
            post("/telegram/webhook")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(update))
            .header("X-Telegram-Bot-Api-Secret-Token", "123")
        ).andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            jsonPath("$.method").value("sendMessage"),
            jsonPath("$.chat_id").value(1L),
            jsonPath("$.text").value("Se vinculo la cuenta.")
        );

        verify(accountService).linkTelegramAccount(eq("123"), eq(1L));
    }

    @Test
    void shouldReturnFailMsgWhenVerifyTokenFails() throws Exception{
        Update update = new Update();
        Message msg = new Message();
        msg.setText("/start 123");
        User user = new User();
        user.setId(1L);
        msg.setFrom(user);
        update.setMessage(msg);

        given(accountService.linkTelegramAccount(eq("123"), eq(1L))).willReturn(false);

        mockMvc.perform(
            post("/telegram/webhook")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(update))
            .header("X-Telegram-Bot-Api-Secret-Token", "123")
        ).andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            jsonPath("$.method").value("sendMessage"),
            jsonPath("$.chat_id").value(1L),
            jsonPath("$.text").value("No se pudo vincular la cuenta.")
        );

        verify(accountService).linkTelegramAccount(eq("123"), eq(1L));
    }
}
