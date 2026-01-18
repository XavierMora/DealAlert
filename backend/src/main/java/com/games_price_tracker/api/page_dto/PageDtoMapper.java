package com.games_price_tracker.api.page_dto;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PageDtoMapper {
    public <T> PageDto<T> fromPage(Page<T> page){
        return new PageDto<T>(
            page.getContent(), 
            page.isEmpty(), 
            page.isFirst(), 
            page.isLast(),
            page.getNumberOfElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize()
        );
    }
}
