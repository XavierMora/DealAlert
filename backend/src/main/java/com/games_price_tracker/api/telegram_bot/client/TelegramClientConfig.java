package com.games_price_tracker.api.telegram_bot.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestClient;

@Configuration
public class TelegramClientConfig {
    @Bean
    TaskScheduler telegramClientScheduler(){
        ThreadPoolTaskScheduler telegramClientScheduler = new ThreadPoolTaskScheduler();
        telegramClientScheduler.setPoolSize(2);
        telegramClientScheduler.setThreadNamePrefix("telegramClientScheduler-");
        return telegramClientScheduler;
    }

    @Bean
    RestClient telegramBotRestClient(@Value("${telegram.bot-api.url}") String telegramBotApiUrl, JdkClientHttpRequestFactory clientHttpRequestFactory){
        return RestClient.builder()
        .requestFactory(clientHttpRequestFactory)
        .baseUrl(telegramBotApiUrl)
        .build();
    }
}
