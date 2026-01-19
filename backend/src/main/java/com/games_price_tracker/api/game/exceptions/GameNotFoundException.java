package com.games_price_tracker.api.game.exceptions;

public class GameNotFoundException extends RuntimeException {
    private Long id;

    public GameNotFoundException(Long id){
        this.id=id;
    }

    public Long getId() {
        return id;
    }
}
