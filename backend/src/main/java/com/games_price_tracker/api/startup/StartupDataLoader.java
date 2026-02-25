package com.games_price_tracker.api.startup;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.games_price_tracker.api.admin.AdminService;

@Component
@Profile("prod")
public class StartupDataLoader implements ApplicationRunner {
    private final AdminService adminService;

    StartupDataLoader(AdminService adminService){
        this.adminService = adminService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        adminService.saveAppList(1000);
    }
}
