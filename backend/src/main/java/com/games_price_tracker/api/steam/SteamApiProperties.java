package com.games_price_tracker.api.steam;

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

        public AppDetailsConfig(String url){
            this.url = url;
        }

        public String getUrl() {
            return url;
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
