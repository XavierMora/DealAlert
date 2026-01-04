package com.games_price_tracker.api.steam;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
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
        
        JdkClientHttpRequestFactory clientHttpRequestFactory = new JdkClientHttpRequestFactory(
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build()
        );
        clientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(8));

        restAppListClient = RestClient.builder()
            .requestFactory(clientHttpRequestFactory)
            .baseUrl(steamAppListUrl)
            .build();

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

    private List<AppDetailsSteam> parseAppDetails(String response, List<Long> steamIds){
        return steamIds.stream().map(steamId -> {
            JsonNode data = objectMapper
                .readTree(response)
                .get(steamId.toString());

            AppDetailsSteam appDetailsSteam = objectMapper.treeToValue(data, AppDetailsSteam.class);
            appDetailsSteam.setSteamId(steamId);

            return appDetailsSteam;
        }).toList();
    }

    public List<AppDetailsSteam> getMultipleAppDetails(List<Long> steamIds) throws ResourceAccessException{
        String response = restAppDetailsClient.get()
            .uri(uriBuilder -> {
                List<String> steamIdsStr = steamIds.stream().map(steamId -> steamId.toString()).toList(); 
                
                return uriBuilder
                .queryParam("cc", "AR")
                .queryParam("appids", String.join(",", steamIdsStr))
                .queryParam("filters", "price_overview")
                .build();
            }).retrieve().body(String.class);

        return parseAppDetails(response, steamIds);
    }
}
