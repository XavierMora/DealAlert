package com.games_price_tracker.api.email.task;

import java.util.ArrayList;
import java.util.List;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.internet.MimeMessage;

public class EmailToMultipleRecipientsTask implements Runnable {
    private final List<String> recipients;
    private final JavaMailSender mailSender;
    private final MimeMessage message;
    private final EmailTasksHandler emailTasksHandler;
    private final int MAX_ATTEMPTS=3;
    private int attempt;

    public EmailToMultipleRecipientsTask(List<String> recipients, JavaMailSender mailSender, MimeMessage message, int attempt, EmailTasksHandler emailTasksHandler){
        this.recipients = recipients;
        this.mailSender = mailSender;
        this.message = message;
        this.emailTasksHandler = emailTasksHandler;
        this.attempt=attempt;
    }

    @Override
    public void run() {
        MimeMessageHelper messageHelper = new MimeMessageHelper(message);

        // Se filtran los que fallaron para reintentar después
        List<String> pendingRecipients = new ArrayList<>(recipients.size());
        for(String recipient : recipients){
            try {
                messageHelper.setTo(recipient);
                mailSender.send(message);
            } catch (Exception e) {
                pendingRecipients.add(recipient);
            }
        };

        if(pendingRecipients.isEmpty()) return;

        if(attempt < MAX_ATTEMPTS) emailTasksHandler.retryTask(pendingRecipients, message, attempt+1);
    }
}
