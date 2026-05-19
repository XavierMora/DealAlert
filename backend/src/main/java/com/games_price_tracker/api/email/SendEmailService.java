package com.games_price_tracker.api.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.games_price_tracker.api.email.brevo.BrevoPostBody;
import com.games_price_tracker.api.price_change_alert.PriceChangeAlertRepository;

@Service
public class SendEmailService {
    private final EmailBuilder emailBuilder;
    private final Logger log = LoggerFactory.getLogger(SendEmailService.class);
    private final RestClient brevoClient;
    private final TaskExecutor sendEmailExecutor;

    public SendEmailService(EmailBuilder emailBuilder, PriceChangeAlertRepository alertRepository, TaskExecutor sendEmailExecutor, RestClient brevoRestClient){
        this.emailBuilder = emailBuilder;
        this.sendEmailExecutor = sendEmailExecutor;
        brevoClient = brevoRestClient;
    }

    public void verificationEmail(String recipient, String code){        
        try {
            BrevoPostBody message = emailBuilder.createVerificationEmail(recipient, code);
            
            sendEmailExecutor.execute(() -> {
                brevoClient.post().body(message).retrieve().toBodilessEntity();
            });
        } catch (Exception e) {
            log.error("Error sending verification email to {}", recipient, e);
            throw new SendEmailException(e);
        }
    }
}
