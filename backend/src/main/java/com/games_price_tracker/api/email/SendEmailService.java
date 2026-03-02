package com.games_price_tracker.api.email;

import java.util.ArrayList;
import java.util.List;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.price.dtos.ChangePriceResult;

@Service
public class SendEmailService {
    private final JavaMailSender mailSender;
    private final EmailBuilder emailBuilder;

    public SendEmailService(JavaMailSender mailSender, EmailBuilder emailBuilder){
        this.mailSender = mailSender;
        this.emailBuilder = emailBuilder;
    }

    public void verificationEmail(String recipient, String code) throws SendEmailException{        
        try {
            MimeMessagePreparator message = emailBuilder.createVerificationEmail(recipient, code);

            mailSender.send(message);
        } catch (Exception e) {
            throw new SendEmailException(e);
        }
    }
   
    public void dealEmail(Game game, ChangePriceResult changePriceResult, List<String> recipients){
        try {
            // Crea y pone en espera la tarea que envia el email a los destinatarios
            ArrayList<MimeMessagePreparator> messages = new ArrayList<>(recipients.size()); 

            for (String recipient : recipients) {
                messages.add(emailBuilder.createDealEmail(game, changePriceResult, recipient));
            }

            mailSender.send(messages.toArray(MimeMessagePreparator[]::new));
        } catch (Exception e) {
            throw new SendEmailException(e);
        }
    }    
}
