package com.games_price_tracker.api.email;

import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.games_price_tracker.api.email.config.EmailConfigProperties;
import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.price.dtos.ChangePriceResult;
import com.games_price_tracker.api.steam.SteamUrlBuilder;

import jakarta.mail.internet.MimeMessage;

@Component
public class EmailBuilder {
    private final EmailConfigProperties emailConfigProperties;
    private final TemplateEngine emailTemplateEngine;
    private final SteamUrlBuilder steamUrlBuilder;

    EmailBuilder(EmailConfigProperties emailConfigProperties, TemplateEngine emailTemplateEngine, SteamUrlBuilder steamUrlBuilder){
        this.emailConfigProperties = emailConfigProperties;
        this.emailTemplateEngine = emailTemplateEngine;
        this.steamUrlBuilder = steamUrlBuilder; 
    }

    public MimeMessagePreparator createVerificationEmail(String recipient, String code) throws Exception{
        return new MimeMessagePreparator() {
            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                Context ctx = new Context();
                ctx.setVariable("code", code);
                String template = emailTemplateEngine.process("verification.html", ctx);

                MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                messageHelper.setFrom(emailConfigProperties.getFrom());
                messageHelper.setTo(recipient);
                messageHelper.setSubject("Tu código de acceso es "+code);
                messageHelper.setText(template, true);
            }
            
        };
    }

    public MimeMessagePreparator createDealEmail(Game game, ChangePriceResult changePriceResult, String recipient) throws Exception{
        return new MimeMessagePreparator() {
            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                // Se carga y establece la plantilla con los datos
                Context ctx = new Context();
                ctx.setVariable("gameName", game.getName());
                ctx.setVariable("oldPrice", changePriceResult.oldPrice());
                ctx.setVariable("newPrice", changePriceResult.newPrice());
                ctx.setVariable("gameSteamUrl", steamUrlBuilder.appUrl(
                    game.getSteamId(), 
                    game.getName()
                ).toString());
                String template = emailTemplateEngine.process("deal-notification.html", ctx);

                MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true,"UTF-8");
                messageHelper.setFrom(emailConfigProperties.getFrom());
                messageHelper.setSubject("El juego %s está en oferta".formatted(game.getName()));
                messageHelper.setTo(recipient);
                messageHelper.setText(template, true);
            }
        }; 
    }
}
