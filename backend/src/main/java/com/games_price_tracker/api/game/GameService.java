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
import com.games_price_tracker.api.common.exceptions.ResourceNotFoundException;
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

    GameService(GameRepository gameRepository, AccountRepository accountRepository, GameMapper gameMapper){
        this.gameMapper = gameMapper;
        this.gameRepository = gameRepository;
        this.accountRepository=accountRepository;
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

    @Transactional
    public Game createGame(Long steamId, String name){
        return gameRepository.save(new Game(steamId, name));
    }

    public Page<Game> getGames(Pageable pageable){
        return gameRepository.findAll(pageable);        
    }

    public Page<GameData> getGames(String name, Account account, Pageable pageable){
        return gameRepository.findGames(
            name == null ? "" : name.trim(), 
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
        } catch (RuntimeException e) {
            log.error("Failed saving {} games", gamesToSave.size(), e);
            throw e;
        }
    }
}
