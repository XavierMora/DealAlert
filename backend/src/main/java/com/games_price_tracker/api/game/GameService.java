package com.games_price_tracker.api.game;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import com.games_price_tracker.api.account.Account;
import com.games_price_tracker.api.account.AccountRepository;
import com.games_price_tracker.api.core.exceptions.ResourceNotFoundException;
import com.games_price_tracker.api.game.dtos.GameData;
import com.games_price_tracker.api.steam.AppSteam;

@Service
public class GameService {
    private final GameRepository gameRepository;
    @Value("${price.min-interval-update}") 
    private Duration priceMinIntervalUpdate;
    AccountRepository accountRepository;
    private final Logger log = LoggerFactory.getLogger(GameService.class);
    private final GameMapper gameMapper;
    private final GameSearchNameNormalizer gameSearchNameNormalizer;

    GameService(GameRepository gameRepository, AccountRepository accountRepository, GameMapper gameMapper, GameSearchNameNormalizer gameSearchNameNormalizer){
        this.gameMapper = gameMapper;
        this.gameRepository = gameRepository;
        this.accountRepository=accountRepository;
        this.gameSearchNameNormalizer = gameSearchNameNormalizer;
    }

    public Game getGameById(Long id) throws ResourceNotFoundException{
        return gameRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("El juego no existe."));
    }

    public boolean gamePriceNeedsUpdate(Game game){
        if(game.getPrice() == null) return true;

        Instant lastUpdate = game.getPrice().getLastUpdate();
        Instant limit = lastUpdate.plus(priceMinIntervalUpdate);

        return limit.isBefore(Instant.now());
    }

    public Page<Game> getGames(Pageable pageable){
        return gameRepository.findAllByActive(true, pageable);        
    }

    public Page<GameData> getGames(String name, Account account, Pageable pageable){
        return gameRepository.findGames(
            name == null ? "" : gameSearchNameNormalizer.transform(name), 
            account == null ? null : account.getId(), 
            pageable
        );
    }

    public Long getDateNextUpdateInSeconds(Game game){
        return Instant.now().until(
            game.getPrice().getLastUpdate().plus(priceMinIntervalUpdate), 
            ChronoUnit.SECONDS
        );
    }

    @Transactional
    public void saveGames(List<AppSteam> apps){
        log.info("Starting save of {} games", apps.size());

        Set<Long> existingSteamIds = gameRepository.getExistingSteamIdsIn(apps.stream().map(app -> app.getSteamId()).toList());

        if(existingSteamIds.size() == apps.size()){
            log.info("No games to save");
            return;
        }
        
        List<AppSteam> gamesToSave;
            
        if(existingSteamIds.isEmpty()){
            gamesToSave = apps;
        }else{
            gamesToSave = apps.stream().filter(app -> !existingSteamIds.contains(app.getSteamId())).toList();
            log.info("{} games already existed", existingSteamIds.size());
        }
          
        try {
            gameRepository.saveAll(gamesToSave.stream().map(app -> gameMapper.fromAppSteam(app)).toList());

            log.info("Success saving {} games", gamesToSave.size());
        } catch (Exception e) {
            log.error("Failed to save {} games", gamesToSave.size(), e);
            throw e;
        }
    }

    @Transactional
    public boolean updateActiveStatusBySteamId(Long steamId, boolean active){
        boolean updated = gameRepository.updateActiveStatusBySteamId(steamId, active) > 0;
        
        if(updated) log.info("Active status of game with steam_id={} updated to {}", steamId, active);
        else log.warn("No game with steam_id={} existed", steamId);

        return updated;    
    }
}
