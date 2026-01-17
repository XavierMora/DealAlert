package com.games_price_tracker.api.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@EnableWebSecurity
@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain priceAlert(HttpSecurity http, AuthFilter authFilter){
        http.securityMatcher("/price-alert")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterAfter(authFilter, ExceptionTranslationFilter.class)
            .authorizeHttpRequests(a -> a.anyRequest().authenticated())
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            .exceptionHandling(e -> 
                e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            );

        return http.build();
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http){
        http
        .securityMatcher("/**")
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(a -> a.anyRequest().permitAll())
        .httpBasic(h -> h.disable())
        .formLogin(f -> f.disable());

        return http.build();
    }

    @Bean
    FilterRegistrationBean<AuthFilter> authFilterRegistration(AuthFilter filter) { // No registra el filtro permitiendo que solo httpsecurity lo agregue
        FilterRegistrationBean<AuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
