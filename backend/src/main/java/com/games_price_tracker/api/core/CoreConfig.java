package com.games_price_tracker.api.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Configuration
public class CoreConfig {
    private ITemplateResolver templateResolver(){
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/template/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    @Bean
    TemplateEngine htmlTemplateEngine(){
        SpringTemplateEngine htmlTemplateEngine = new SpringTemplateEngine();
        htmlTemplateEngine.addTemplateResolver(templateResolver());
        return htmlTemplateEngine;
    }
}
