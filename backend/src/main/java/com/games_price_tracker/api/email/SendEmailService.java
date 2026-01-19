package com.games_price_tracker.api.email;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class SendEmailService {
    private final JavaMailSender mailSender;
    private final AppEmailConfigProperties appEmailConfig;

    public SendEmailService(JavaMailSender mailSender, AppEmailConfigProperties appEmailConfig){
        this.mailSender = mailSender;
        this.appEmailConfig = appEmailConfig;
    }

    public void verificationEmail(String emailTo, String code) throws SendEmailException{
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper msgHelper = new MimeMessageHelper(msg);

        try {
            msgHelper.setFrom(appEmailConfig.getFrom());
            msgHelper.setTo(emailTo);
            msgHelper.setSubject("Tu código de acceso es "+code);
            msgHelper.setText("Si no solicitaste este código podés ignorar este email.");

            mailSender.send(msg);
        } catch (MessagingException e) {
            throw new SendEmailException();
        }
    }
}
