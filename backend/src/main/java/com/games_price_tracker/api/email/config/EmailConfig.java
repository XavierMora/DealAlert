package com.games_price_tracker.api.email.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

@Configuration
public class EmailConfig {
    @Bean
    TaskExecutor sendEmailExecutor(){
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(1);
        taskExecutor.setMaxPoolSize(3);
        taskExecutor.setThreadNamePrefix("sendEmailExecutor");
        return taskExecutor;
    }

    @Bean
    RestClient brevoRestClient(@Value("${brevo.api.key}") String brevoApiKey, JdkClientHttpRequestFactory clientHttpRequestFactory){
        return RestClient.builder()
        .baseUrl("https://api.brevo.com/v3/smtp/email")
        .defaultHeader("api-key", brevoApiKey)
        .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .requestFactory(clientHttpRequestFactory)
        .build();
    }
}
