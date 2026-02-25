package com.games_price_tracker.api.tracking;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.games_price_tracker.api.game.GameService;
import com.games_price_tracker.api.steam.config.SteamApiProperties;
import com.games_price_tracker.api.tracking.enqueue_games.EnqueueGamesTaskHandler;
import com.games_price_tracker.api.tracking.fetch_appdetails.FetchAppDetailsTasksHandler;

@Configuration
@EnableScheduling
@Profile("!test")
public class TrackingConfig {
    @Bean
    TaskScheduler taskScheduler(){
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(2);
        return taskScheduler;
    }

    @Bean
    TaskExecutor taskExecutor(){
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(2);
        return taskExecutor;
    }

    @Bean
    EnqueueGamesTaskHandler enqueueGamesTaskHandler(GameService gameService, FetchAppDetailsTasksHandler updatePriceTasksHandler, TaskScheduler taskScheduler, SteamApiProperties steamApiProperties, @Value("${price.min-interval-update}") Duration priceMinIntevalUpdate){
        return new EnqueueGamesTaskHandler(gameService, updatePriceTasksHandler, taskScheduler, steamApiProperties, priceMinIntevalUpdate);
    }
}
