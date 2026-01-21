package com.games_price_tracker.api.steam;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class SteamUrlBuilder {
    private final String appUrl;
    private final String appImgUrl;

    public SteamUrlBuilder(@Value("${steam.store.appUrl}") String appUrl, @Value("${steam.store.appImgUrl}") String appImgUrl){
        this.appUrl = appUrl;
        this.appImgUrl = appImgUrl;
    }

    public URI appUrl(Long steamId, String name) {
        return UriComponentsBuilder
        .fromUriString(appUrl)
        .encode()
        .build(steamId, name);
    }
    
    public URI appImageUrl(Long steamId) {
        return UriComponentsBuilder
        .fromUriString(appImgUrl)
        .encode()
        .build(steamId);
    }
}
