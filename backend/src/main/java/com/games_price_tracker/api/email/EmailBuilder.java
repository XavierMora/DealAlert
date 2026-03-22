package com.games_price_tracker.api.email;

import java.util.List;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.games_price_tracker.api.email.config.EmailConfigProperties;
import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.price.dtos.ChangePriceResult;
import com.games_price_tracker.api.steam.SteamUrlBuilder;

@Component
public class EmailBuilder {
    private final String from;
    private final TemplateEngine emailTemplateEngine;
    private final SteamUrlBuilder steamUrlBuilder;

    EmailBuilder(EmailConfigProperties emailConfigProperties, TemplateEngine emailTemplateEngine, SteamUrlBuilder steamUrlBuilder){
        this.from = emailConfigProperties.getFrom();
        this.emailTemplateEngine = emailTemplateEngine;
        this.steamUrlBuilder = steamUrlBuilder; 
    }

    public BrevoPostBody createVerificationEmail(String recipient, String code){
        Context ctx = new Context();
        ctx.setVariable("code", code);
        String template = emailTemplateEngine.process("verification.html", ctx);

        return new BrevoPostBody(
            "Tu código de acceso es "+code, 
            from, 
            List.of(recipient), 
            template
        );
    }

    public BrevoPostBody createDealEmail(Game game, ChangePriceResult changePriceResult, List<String> recipients){
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

        return new BrevoPostBody(
            "El juego %s está en oferta".formatted(game.getName()), 
            from, 
            recipients, 
            template
        );
    }
}
