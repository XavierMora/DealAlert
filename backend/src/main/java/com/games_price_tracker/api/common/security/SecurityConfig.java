package com.games_price_tracker.api.common.security;

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
    SecurityFilterChain priceChangeAlert(HttpSecurity http, AuthFilter authFilter){
        http.securityMatcher("/price-alerts/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterAfter(authFilter, ExceptionTranslationFilter.class)
            .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            .exceptionHandling(e -> 
                e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            );

        return http.build();
    }

    @Bean
    SecurityFilterChain account(HttpSecurity http, AuthFilter authFilter){
        http
        .securityMatcher("/account/**")
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterAfter(authFilter, ExceptionTranslationFilter.class)
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/account/logout").authenticated()
            .requestMatchers("/account/sign-in-code", "/account/verify-code").not().authenticated()
        )
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
        .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
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