package com.games_price_tracker.api.steam;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class SteamClient {
    private final ObjectMapper objectMapper;
    private final RestClient restAppListClient;
    private final RestClient restAppDetailsClient;

    SteamClient(ObjectMapper objectMapper, @Value("${steam.applist.api.url}") String steamAppListUrl, @Value("${steam.appdetails.api.url}") String steamAppDetailsUrl){
        this.objectMapper = objectMapper;
        restAppListClient = RestClient.create(steamAppListUrl);

        JdkClientHttpRequestFactory clientHttpRequestFactory = new JdkClientHttpRequestFactory();
        clientHttpRequestFactory.setReadTimeout(10000);

        restAppDetailsClient = RestClient.builder()
            .requestFactory(clientHttpRequestFactory)
            .baseUrl(steamAppDetailsUrl)
            .build();
    }

    private List<AppSteam> parseSteamApps(String response){
        JsonNode apps = objectMapper
            .readTree(response)
            .get("response")
            .get("apps"); 
        
        return objectMapper.treeToValue(apps, new TypeReference<List<AppSteam>>(){});    
    }

    public List<AppSteam> getAppList(){
        String response = restAppListClient.get().retrieve().body(String.class);

        return parseSteamApps(response);
    }

    private AppDetailsSteam parseAppDetails(String response, Long steamId){
        JsonNode data = objectMapper
            .readTree(response)
            .get(steamId.toString());

        return objectMapper.treeToValue(data, AppDetailsSteam.class);
    }

    public AppDetailsSteam getAppDetails(Long steamId){
        String response = restAppDetailsClient.get()
            .uri(uriBuilder -> uriBuilder
                .queryParam("cc", "AR")
                .queryParam("appids", "{id}")
                .build(steamId)
            ).retrieve().body(String.class);

        return parseAppDetails(response, steamId);
    }
}
