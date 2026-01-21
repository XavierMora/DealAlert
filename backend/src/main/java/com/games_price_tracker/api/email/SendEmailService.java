package com.games_price_tracker.api.email;

import java.util.List;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.games_price_tracker.api.email.config.EmailConfigProperties;
import com.games_price_tracker.api.email.task.EmailTasksHandler;
import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.price.dtos.ChangePriceResult;

import jakarta.mail.internet.MimeMessage;

@Service
public class SendEmailService {
    private final JavaMailSender mailSender;
    private final EmailConfigProperties emailConfigProperties;
    private final TemplateEngine emailTemplateEngine;
    private final EmailTasksHandler emailTasksHandler;

    public SendEmailService(JavaMailSender mailSender, EmailConfigProperties emailConfigProperties, TemplateEngine emailTemplateEngine, EmailTasksHandler emailTasksHandler){
        this.mailSender = mailSender;
        this.emailConfigProperties = emailConfigProperties;
        this.emailTemplateEngine = emailTemplateEngine;
        this.emailTasksHandler = emailTasksHandler;
    }

    public void verificationEmail(String emailTo, String code) throws SendEmailException{
        MimeMessage message = mailSender.createMimeMessage();
        
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");
            
            Context ctx = new Context();
            ctx.setVariable("code", code);
            String template = emailTemplateEngine.process("verification.html", ctx);
    
            messageHelper.setFrom(emailConfigProperties.getFrom());
            messageHelper.setTo(emailTo);
            messageHelper.setSubject("Tu código de acceso es "+code);
            messageHelper.setText(template, true);

            mailSender.send(message);
        } catch (Exception e) {
            throw new SendEmailException();
        }
    }
   
    public void priceChangeEmail(Game game, ChangePriceResult changePriceResult, List<String> recipients){
        MimeMessage message = mailSender.createMimeMessage(); 

        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");        
            messageHelper.setFrom(emailConfigProperties.getFrom());
            messageHelper.setSubject(String.format("Precio de %s actualizado", game.getName()));  

            // Se carga y establece la plantilla con los datos
            Context ctx = new Context();
            ctx.setVariable("gameName", game.getName());
            ctx.setVariable("oldPrice", changePriceResult.oldPrice());
            ctx.setVariable("newPrice", changePriceResult.newPrice());
            ctx.setVariable("basePriceChanged", changePriceResult.oldPrice().initialPrice() != changePriceResult.newPrice().initialPrice());
            String template = emailTemplateEngine.process("price-change.html", ctx);
            messageHelper.setText(template, true);      

            // Crea y pone en espera la tarea que envia el email a los destinatarios
            emailTasksHandler.createEmailToMultipleRecipientsTask(recipients, message);
        } catch (Exception e) {
            throw new SendEmailException();
        }
    }
}
