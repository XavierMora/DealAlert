package com.games_price_tracker.api.email;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import com.games_price_tracker.api.account.AccountRepository;
import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.price.dtos.ChangePriceResult;
import com.games_price_tracker.api.price_change_alert.PriceChangeAlertRepository;

@Service
public class SendEmailService {
    private final JavaMailSender mailSender;
    private final EmailBuilder emailBuilder;
    PriceChangeAlertRepository alertRepository;
    AccountRepository accountRepository;
    TaskScheduler emailScheduler;
    private final Logger log = LoggerFactory.getLogger(SendEmailService.class);
    private final int MAX_ATTEMPTS=3;

    public SendEmailService(JavaMailSender mailSender, EmailBuilder emailBuilder, PriceChangeAlertRepository alertRepository, TaskScheduler emailScheduler){
        this.emailScheduler = emailScheduler;
        this.alertRepository = alertRepository;
        this.mailSender = mailSender;
        this.emailBuilder = emailBuilder;
    }

    public void verificationEmail(String recipient, String code) throws SendEmailException{        
        try {
            MimeMessagePreparator message = emailBuilder.createVerificationEmail(recipient, code);

            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error sending verification email to {}", recipient, e);
            throw new SendEmailException(e);
        }
    }
   
    public void dealEmail(Game game, ChangePriceResult changePriceResult, List<String> recipients){
        try {
            MimeMessagePreparator[] messages = createDealEmails(game, changePriceResult, recipients);

            emailScheduler.schedule(new SendDealEmailTask(mailSender, messages, recipients, 1, this, game, changePriceResult), Instant.now());
        } catch (Exception e) {
            log.error("Error creating deal email", e);
            throw new SendEmailException(e);
        }
    }    

    private MimeMessagePreparator[] createDealEmails(Game game, ChangePriceResult changePriceResult, List<String> recipients) throws Exception{
        // Crea y pone en espera la tarea que envia el email a los destinatarios
        ArrayList<MimeMessagePreparator> messages = new ArrayList<>(recipients.size()); 

        for (String recipient : recipients) {
            messages.add(emailBuilder.createDealEmail(game, changePriceResult, recipient));
        }
        
        return messages.toArray(MimeMessagePreparator[]::new);
    }

    boolean retryDealEmails(int attempt, Game game, ChangePriceResult changePriceResult, List<String> recipients){
        if(attempt == MAX_ATTEMPTS){
            log.info("Deal notification for the game with id={} reach max attempts({}), recipients: {}", game.getId(), MAX_ATTEMPTS, recipients.size());
            return false;
        }

        try {
            MimeMessagePreparator[] messages = createDealEmails(game, changePriceResult, recipients);

            // Reintento aumentando el tiempo de inicio en 30 segundos por intento
            emailScheduler.schedule(new SendDealEmailTask(mailSender, messages, recipients, attempt+1, this, game, changePriceResult), Instant.now().plus(Duration.ofSeconds(attempt*30)));

            return true;
        } catch (Exception e) {
            log.error("Deal notification couldn't be created for attempt n°{} for {} recipients", attempt+1, recipients.size(), e);
            return false;
        }
    }
}
