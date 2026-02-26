package com.games_price_tracker.api.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    AdminController(AdminService adminService){
        this.adminService = adminService;
    }

    @PostMapping("/save-games")
    public ResponseEntity<Void> syncGames(@RequestParam(name = "max_games") @Min(1) @Max(30000) Integer maxGames) {
        adminService.saveAppList(maxGames);

        return ResponseEntity.accepted().build();
    }   
}
