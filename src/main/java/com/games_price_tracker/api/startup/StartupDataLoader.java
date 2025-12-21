package com.games_price_tracker.api.startup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.games_price_tracker.api.game.AppSteam;
import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.game.GameMapper;
import com.games_price_tracker.api.game.GameRepository;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class StartupDataLoader implements ApplicationRunner {
    @Value("${some.steam.data.path}")
    private String steamMockData;
    private final ResourceLoader resourceLoader;
    private final GameMapper gameMapper;
    private final GameRepository gameRepository;
    private final ObjectMapper objectMapper;

    StartupDataLoader(ResourceLoader resourceLoader, GameMapper gameMapper, GameRepository gameRepository, ObjectMapper objectMapper){
        this.resourceLoader = resourceLoader;
        this.gameMapper = gameMapper;
        this.gameRepository = gameRepository;
        this.objectMapper = objectMapper;
    }
    
    private List<AppSteam> getSteamApps() throws IOException, JacksonException{
        JsonNode jsonNode = objectMapper
            .readTree(resourceLoader.getResource(steamMockData).getInputStream())
            .get("apps"); 
    
        return objectMapper.convertValue(jsonNode, new TypeReference<List<AppSteam>>(){});    
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Game> games = getSteamApps().stream().map((steamApp) -> gameMapper.fromAppSteam(steamApp)).toList();

        gameRepository.saveAll(games);
    }
}
