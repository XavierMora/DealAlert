package com.games_price_tracker.api.email;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.price.dtos.ChangePriceResult;

import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class SendDealEmailTask implements Runnable{
    private final JavaMailSender mailSender;
    private final MimeMessagePreparator[] messages;
    private final List<String> recipients;
    private int attempt;
    private final SendEmailService sendEmailService;
    private final Logger log = LoggerFactory.getLogger(SendDealEmailTask.class);
    private final Game game;
    private final ChangePriceResult changePriceResult;

    SendDealEmailTask(JavaMailSender mailSender, MimeMessagePreparator[] messages, List<String> recipients, int attempt, SendEmailService sendEmailService, Game game, ChangePriceResult changePriceResult){
        this.game = game;
        this.changePriceResult = changePriceResult;
        this.sendEmailService = sendEmailService;
        this.mailSender = mailSender;
        this.messages = messages;
        this.recipients = recipients;
        this.attempt = attempt;
    }

    @Override
    public void run() {
        try {
            log.info("Starting deal notification for the game with id={} to {} recipients", game.getId(), recipients.size());

            mailSender.send(messages);

            log.info("Success sending deal notification for the game with id={} to {} recipients", game.getId(), recipients.size());
        } catch(MailSendException mailSendException){
            List<String> pendingRecipients = new ArrayList<>(recipients.size());

            mailSendException.getFailedMessages().keySet().forEach(msg -> {
                MimeMessage mimeMessage = (MimeMessage) msg; 
                try{
                    pendingRecipients.add(((InternetAddress) mimeMessage.getRecipients(Message.RecipientType.TO)[0]).getAddress());
                }catch(Exception e){
                    log.error("Failed to recover recipient", e);
                }
            });

            if(pendingRecipients.isEmpty()) return;

            boolean success = sendEmailService.retryDealEmails(attempt, game, changePriceResult, pendingRecipients);

            if(success){
                log.info("Retry of deal notification for the game with id={} to {} recipients scheduled", game.getId(), pendingRecipients.size());
            }
        } catch(Exception e){
            log.error("Failed to prepare deal notification for the game with id={} on attempt n°{} to {} recipients", game.getId(), attempt, recipients.size(), e);
        }
    }
}
