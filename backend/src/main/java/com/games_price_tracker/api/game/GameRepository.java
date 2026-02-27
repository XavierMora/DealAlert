package com.games_price_tracker.api.game;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.games_price_tracker.api.game.dtos.GameData;

public interface GameRepository extends JpaRepository<Game, Long>{ 
    @Query("""
        select new com.games_price_tracker.api.game.dtos.GameData(
            g,
            case 
                when ?2 is null then null
                when T2.gameId is not null then true
                else false
            end as isInNotification
        )
        from Game g
        left join(
            select p.game.id as gameId
            from PriceChangeAlert p
            where p.account.id = ?2
        ) as T2
        on g.id = T2.gameId
        where g.searchName like CONCAT('%',?1,'%') and g.active = true
    """) 
    Page<GameData> findGames(String name, Long accountId, Pageable pageable);

    Page<Game> findAllByActive(boolean active, Pageable pageable);

    @Query("""
        select g.steamId     
        from Game g
        where g.steamId in(?1)
    """)
    Set<Long> getExistingSteamIdsIn(List<Long> steamIds);

    @Modifying
    @Query("""
        update Game g
        set g.active = ?2
        where g.steamId = ?1
    """)
    int updateActiveStatusBySteamId(Long steamId, boolean active);
}
