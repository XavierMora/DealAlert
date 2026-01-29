package com.games_price_tracker.api.price_change_alert;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.games_price_tracker.api.account.Account;
import com.games_price_tracker.api.common.response.ApiResponseBody;
import com.games_price_tracker.api.page_dto.PageDto;
import com.games_price_tracker.api.page_dto.PageDtoMapper;
import com.games_price_tracker.api.price_change_alert.dtos.CreatePriceChangeAlertBody;
import com.games_price_tracker.api.price_change_alert.dtos.PriceChangeAlertInfo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import java.util.Optional;

import org.hibernate.validator.constraints.Range;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/price-alerts")
public class PriceChangeAlertController {
    private final PriceChangeAlertService priceChangeAlertService;
    private final PageDtoMapper pageDtoMapper;
    private final PriceChangeAlertMapper priceChangeAlertMapper;

    PriceChangeAlertController(PriceChangeAlertService priceChangeAlertService, PageDtoMapper pageDtoMapper, PriceChangeAlertMapper priceChangeAlertMapper){
        this.priceChangeAlertService = priceChangeAlertService;
        this.pageDtoMapper = pageDtoMapper;
        this.priceChangeAlertMapper = priceChangeAlertMapper;
    }

    @PostMapping()
    public ResponseEntity<ApiResponseBody> createAlert(@AuthenticationPrincipal Account account, @RequestBody @Valid CreatePriceChangeAlertBody body) {
        Optional<PriceChangeAlert> alert = priceChangeAlertService.createAlert(account, body.gameId());
        
        if(alert.isPresent()) return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseBody(
            true,
            "Alerta creada.", 
            priceChangeAlertMapper.toPriceChangeAlertInfo(alert.get())
        ));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponseBody(
            false,
            "La alerta ya existe.", 
            null
        ));
    }

    @GetMapping()
    public ResponseEntity<ApiResponseBody> getAlerts(
        @AuthenticationPrincipal Account account, 
        @RequestParam(defaultValue = "10") @Range(min = 1, max = 50, message = "Size debe estar entre 1 y 50.") int size, 
        @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page debe ser mayor o igual a 0.") int page
    ) {
        Page<PriceChangeAlert> pageAlerts = priceChangeAlertService.getAlerts(account, PageRequest.of(page, size));

        Page<PriceChangeAlertInfo> pageAlertInfo = pageAlerts.map((alert) -> priceChangeAlertMapper.toPriceChangeAlertInfo(alert));

        PageDto<PriceChangeAlertInfo> pageDto = pageDtoMapper.fromPage(pageAlertInfo);

        return ResponseEntity.ok(new ApiResponseBody(true, null, pageDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseBody> deleteAlert(@AuthenticationPrincipal Account account, @PathVariable Long id){
        boolean success = priceChangeAlertService.deleteAlert(id, account);
        
        if(!success) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponseBody(
            false,
            "La alerta no existe.", 
            null
        ));

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponseBody(
            true,
            "Alerta eliminada.", 
            null
        ));
    }
}
