package com.games_price_tracker.api.common.security;

import java.util.List;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.games_price_tracker.api.common.security.filters.AuthFilter;
import com.games_price_tracker.api.common.security.handlers.ApiAccessDeniedHandler;
import com.games_price_tracker.api.common.security.handlers.ApiAuthenticationEntryPointHandler;

@EnableWebSecurity
@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain priceChangeAlertSecurityChain(HttpSecurity http, AuthFilter authFilter, ApiAuthenticationEntryPointHandler authEntryPointHandler){
        http
        .securityMatcher("/price-change-alerts/**")
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(csrf -> csrf.spa())
        .addFilterAfter(authFilter, ExceptionTranslationFilter.class)
        .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
        .httpBasic(httpBasic -> httpBasic.disable())
        .formLogin(formLogin -> formLogin.disable())
        .exceptionHandling(handler -> handler.authenticationEntryPoint(authEntryPointHandler));

        return http.build();
    }

    @Bean
    SecurityFilterChain accountSecurityChain(HttpSecurity http, AuthFilter authFilter, ApiAccessDeniedHandler accessDeniedHandler, ApiAuthenticationEntryPointHandler authEntryPointHandler){
        http
        .securityMatcher("/account/**")
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(csrf -> csrf.spa().ignoringRequestMatchers("/account/sign-in-code", "/account/verify-code")) // No valida el token csrf para los endpoints en ignoringRequestMatchers
        .logout(logout -> logout
            .logoutUrl("/account/logout")
            .deleteCookies("SESSION")
            .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
        )
        .addFilterAfter(authFilter, ExceptionTranslationFilter.class)
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/account/sign-in-code", "/account/verify-code").not().authenticated()
            .anyRequest().authenticated()
        )
        .httpBasic(httpBasic -> httpBasic.disable())
        .formLogin(formLogin -> formLogin.disable())
        .exceptionHandling(handler -> handler.accessDeniedHandler(accessDeniedHandler).authenticationEntryPoint(authEntryPointHandler));

        return http.build();
    }

    @Bean
    SecurityFilterChain csrfToken(HttpSecurity http){
        http
        .securityMatcher("/csrf")
        .csrf(csrf -> csrf.spa())
        .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
        .httpBasic(httpBasic -> httpBasic.disable())
        .formLogin(formLogin -> formLogin.disable());

        return http.build();
    }

    @Bean
    SecurityFilterChain generalSecurityChain(HttpSecurity http, AuthFilter authFilter){
        http
        .securityMatcher("/**")
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
        .httpBasic(httpBasic -> httpBasic.disable())
        .formLogin(formLogin -> formLogin.disable())
        .addFilterAfter(authFilter, ExceptionTranslationFilter.class);
        
        return http.build();
    }

    @Bean
    FilterRegistrationBean<AuthFilter> authFilterRegistration(AuthFilter filter) { // No registra el filtro permitiendo que solo httpsecurity lo agregue
        FilterRegistrationBean<AuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "DELETE"));
        config.setAllowCredentials(true);
        config.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN"));
        config.setExposedHeaders(List.of("Retry-After"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}