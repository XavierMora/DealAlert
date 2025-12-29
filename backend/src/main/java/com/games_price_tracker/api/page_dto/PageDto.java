package com.games_price_tracker.api.page_dto;

import java.util.List;

public record PageDto<T>(
    List<T> content, 
    boolean empty, 
    boolean first, 
    boolean last, 
    int numberOfElements,
    int totalPages,
    int pageNumber,
    int size
) {

}
