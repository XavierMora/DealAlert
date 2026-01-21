package com.games_price_tracker.api.steam.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "steam.api")
public class SteamApiProperties {
    private AppListConfig applist;
    private AppDetailsConfig appdetails;

    public SteamApiProperties(AppListConfig applist, AppDetailsConfig appdetails){
        this.applist = applist;
        this.appdetails = appdetails;
    }

    public AppDetailsConfig getAppdetails() {
        return appdetails;
    }
    public AppListConfig getApplist() {
        return applist;
    }

    public static class AppDetailsConfig{
        private String url;
        private Duration delayBetweenRequests;
        private int gamesPerRequest;
        private int maxPagesPerEnqueue;
    
        public AppDetailsConfig(String url, Duration delayBetweenRequests, int gamesPerRequest, int maxPagesPerEnqueue){
            this.url = url;
            this.delayBetweenRequests = delayBetweenRequests;
            this.gamesPerRequest = gamesPerRequest;
            this.maxPagesPerEnqueue = maxPagesPerEnqueue;
        }

        public Duration getDelayBetweenRequests() {
            return delayBetweenRequests;
        }
        public int getGamesPerRequest() {
            return gamesPerRequest;
        }
        public String getUrl() {
            return url;
        }
        public int getMaxPagesPerEnqueue() {
            return maxPagesPerEnqueue;
        }
    }

    public static class AppListConfig{
        private String url;

        public AppListConfig(String url){this.url=url;}

        public String getUrl() {
            return url;
        }
    }
}
