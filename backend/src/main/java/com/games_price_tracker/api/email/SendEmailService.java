package com.games_price_tracker.api.email;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.games_price_tracker.api.email.brevo.BrevoPostBody;
import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.price.dtos.ChangePriceResult;
import com.games_price_tracker.api.price_change_alert.PriceChangeAlertRepository;

@Service
public class SendEmailService {
    private final EmailBuilder emailBuilder;
    private final Logger log = LoggerFactory.getLogger(SendEmailService.class);
    private final RestClient brevoClient;

    public SendEmailService(EmailBuilder emailBuilder, PriceChangeAlertRepository alertRepository, @Value("${brevo.api.key}") String brevoApiKey){
        this.emailBuilder = emailBuilder;

        JdkClientHttpRequestFactory clientHttp = new JdkClientHttpRequestFactory(
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
        );
        clientHttp.setReadTimeout(Duration.ofSeconds(7));

        brevoClient = RestClient.builder()
        .baseUrl("https://api.brevo.com/v3/smtp/email")
        .defaultHeader("api-key", brevoApiKey)
        .defaultHeader("content-type", "application/json")
        .defaultHeader("accept", "application/json")
        .requestFactory(clientHttp)
        .build();
    }

    public void verificationEmail(String recipient, String code){        
        try {
            BrevoPostBody message = emailBuilder.createVerificationEmail(recipient, code);
            
            brevoClient.post().body(message).retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.error("Error sending verification email to {}", recipient, e);
            throw new SendEmailException(e);
        }
    }
   
    public void dealEmail(Game game, ChangePriceResult changePriceResult, List<String> recipients){
        try {
            BrevoPostBody messages = emailBuilder.createDealEmail(game, changePriceResult, recipients);

            brevoClient.post().body(messages).retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.error("Error sending deal email to {} recipients", recipients.size(), e);
            throw new SendEmailException(e);
        }
    }    
}
