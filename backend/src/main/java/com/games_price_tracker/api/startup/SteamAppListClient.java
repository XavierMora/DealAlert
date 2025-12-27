package com.games_price_tracker.api.startup;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.game.GameMapper;
import com.games_price_tracker.api.steam.AppSteam;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class SteamAppListClient {
    @Value("${steam.applist.api.url}")
    private String steamAppListUrl;
    private final ObjectMapper objectMapper;
    private final GameMapper gameMapper;

    public SteamAppListClient(ObjectMapper objectMapper, GameMapper gameMapper){
        this.objectMapper = objectMapper;
        this.gameMapper = gameMapper;
    }

    private List<AppSteam> parseSteamApps(String response){
        JsonNode jsonNode = objectMapper
            .readTree(response)
            .get("response")
            .get("apps"); 
    
        return objectMapper.convertValue(jsonNode, new TypeReference<List<AppSteam>>(){});    
    }

    public List<Game> getGamesFromAppListApi(){
        RestClient restClient = RestClient.create(steamAppListUrl);

        String response = restClient.get().retrieve().body(String.class);

        // Se parsean las apps de la respuesta a SteamApp y se transforman a Game
        return parseSteamApps(response).stream().map((steamApp) -> gameMapper.fromAppSteam(steamApp)).toList();
    }
}
