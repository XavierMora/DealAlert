package com.games_price_tracker.api.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;

@Configuration
public class EmailConfig {
    @Bean
    SimpleMailMessage templateMailMsg(@Value("${app.mail}") String appMail){
        SimpleMailMessage mailMsg = new SimpleMailMessage();
        mailMsg.setFrom(appMail);
        return mailMsg;
    }
}
