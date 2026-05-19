package com.games_price_tracker.api.steam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class SteamConfig {
    @Bean
    RestClient steamAppListRestClient(SteamApiProperties steamApiProperties, JdkClientHttpRequestFactory clientHttpRequestFactory){
        return RestClient.builder()
        .requestFactory(clientHttpRequestFactory)
        .baseUrl(steamApiProperties.getApplist().getUrl())
        .build();
    }

    @Bean
    RestClient steamAppDetailsRestClient(SteamApiProperties steamApiProperties, JdkClientHttpRequestFactory clientHttpRequestFactory){
        return RestClient.builder()
        .requestFactory(clientHttpRequestFactory)
        .baseUrl(steamApiProperties.getAppdetails().getUrl())
        .build();
    }
}
