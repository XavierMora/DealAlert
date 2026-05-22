package com.games_price_tracker.api.email;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.games_price_tracker.api.email.brevo.BrevoPostBody;
import com.games_price_tracker.api.price.PriceMapper;
import com.games_price_tracker.api.steam.SteamUrlBuilder;

@Component
public class EmailBuilder {
    private final String from;
    private final TemplateEngine htmlTemplateEngine;

    EmailBuilder(@Value("${app.email}") String appEmail, TemplateEngine htmlTemplateEngine, SteamUrlBuilder steamUrlBuilder, PriceMapper priceMapper){
        this.from = appEmail;
        this.htmlTemplateEngine = htmlTemplateEngine;
    }

    public BrevoPostBody createVerificationEmail(String recipient, String code){
        Context ctx = new Context();
        ctx.setVariable("code", code);
        String template = htmlTemplateEngine.process("verification.html", ctx);

        return new BrevoPostBody(
            "Tu código de acceso es "+code, 
            from, 
            List.of(recipient), 
            template
        );
    }
}
