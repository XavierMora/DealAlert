package com.games_price_tracker.api.game;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class GameSearchNameNormalizer {
    private final Pattern patternGameSearchName = Pattern.compile("[^a-z0-9]+");

    public String transform(String gameName) {
        return patternGameSearchName.matcher(gameName.toLowerCase().trim()).replaceAll("");
    }
}
