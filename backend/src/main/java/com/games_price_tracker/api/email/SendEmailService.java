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

    public boolean verificationEmail(String emailTo, String token){
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper msgHelper = new MimeMessageHelper(msg);
        boolean emailSent = true;

        try {
            msgHelper.setFrom(appEmailConfig.getFrom());
            msgHelper.setTo(emailTo);
            msgHelper.setSubject("Verificación de email");

            msgHelper.setText("<a href="+appEmailConfig.getClientVerificationUrl()+"?token="+token+">Confirmar email</a>", true);

            mailSender.send(msg);
        } catch (MessagingException e) {
            emailSent = false;
        }

        return emailSent;
    }
}
