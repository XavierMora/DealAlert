package com.games_price_tracker.api.steam;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.games_price_tracker.api.steam.config.SteamApiProperties;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class SteamClient {
    private final ObjectMapper objectMapper;
    private final RestClient restAppListClient;
    private final RestClient restAppDetailsClient;
    private final Logger log = LoggerFactory.getLogger(SteamClient.class);

    public SteamClient(ObjectMapper objectMapper, SteamApiProperties steamApiProperties){
        this.objectMapper = objectMapper;

        JdkClientHttpRequestFactory clientHttpRequestFactory = new JdkClientHttpRequestFactory(
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build()
        );
        clientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(8));

        restAppListClient = RestClient.builder()
            .requestFactory(clientHttpRequestFactory)
            .baseUrl(steamApiProperties.getApplist().getUrl())
            .build();

        restAppDetailsClient = RestClient.builder()
            .requestFactory(clientHttpRequestFactory)
            .baseUrl(steamApiProperties.getAppdetails().getUrl())
            .build();
    }

    private List<AppSteam> parseSteamApps(String response){
        JsonNode apps = objectMapper
            .readTree(response)
            .get("response")
            .get("apps"); 

        return objectMapper.treeToValue(apps, new TypeReference<List<AppSteam>>(){});    
    }

    public Optional<List<AppSteam>> getAppList(int maxGames){
        try {
            log.info("Sending request to steam applist api for max_results={}", maxGames);
            String response = restAppListClient.get().uri(uriBuilder -> {
                return uriBuilder.queryParam("max_results", maxGames).build();
            }).retrieve().body(String.class);
    
            List<AppSteam> apps = parseSteamApps(response);
            log.info("Retrieved {} apps from steam applist api", apps.size());

            return Optional.of(apps);
        } catch (Exception e) {
            log.error("Failed to retrieve app list", e);
            return Optional.empty();
        }
    }

    private List<AppDetailsSteam> parseAppDetails(String response, List<Long> steamIds){
        return steamIds.stream().map(steamId -> {
            JsonNode data = objectMapper
                .readTree(response)
                .get(steamId.toString());

            if(data == null){
                log.error("Game with steam_id={} is not present in the appdetails response", steamId);
                return null;
            }

            AppDetailsSteam appDetailsSteam = objectMapper.treeToValue(data, AppDetailsSteam.class);
            appDetailsSteam.setSteamId(steamId);

            if(!appDetailsSteam.getSuccess()) log.info("Game with steam_id={} has success=false in response", steamId);

            return appDetailsSteam;
        }).toList();
    }

    public List<AppDetailsSteam> getMultipleAppDetails(List<Long> steamIds){
        log.info("Sending request to steam appdetails api for {} games", steamIds.size());

        try {
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
        } catch (Exception e) {
            log.error("Failed to retrieve appdetails", e);
            throw e;
        }
    }
}
