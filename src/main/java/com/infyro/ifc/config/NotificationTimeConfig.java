package com.infyro.ifc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class NotificationTimeConfig {
    @Bean
    public ZoneId notificationZoneId(
            @Value("${app.notification.time-zone:Asia/Kolkata}") String timeZone) {
        return ZoneId.of(timeZone);
    }

    @Bean
    public Clock notificationClock(ZoneId notificationZoneId) {
        return Clock.system(notificationZoneId);
    }
}
