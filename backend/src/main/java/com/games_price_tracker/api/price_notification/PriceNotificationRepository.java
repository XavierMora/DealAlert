package com.games_price_tracker.api.price_notification;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceNotificationRepository extends JpaRepository<PriceNotification, Long> {
    
}
