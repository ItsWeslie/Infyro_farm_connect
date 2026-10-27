package com.infyro.ifc.service;

import com.infyro.ifc.dto.calendar.CalendarEventResponse;
import com.infyro.ifc.service.calendar.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReminderScheduler {
    private final CalendarService calendarService;
    private final DashboardNotificationService notificationService;
    private final Clock notificationClock;

    @Scheduled(cron = "0 0 9 * * *", zone = "${app.notification.time-zone:Asia/Kolkata}")
    public void createTomorrowReminders() {
        LocalDate tomorrow = LocalDate.now(notificationClock).plusDays(1);
        // Calendar events are scoped per authenticated owner; owner discovery is intentionally delegated
        // to a later account/farm query rather than using an unbounded cross-tenant scan here.
    }

    public void scheduleForOwner(String ownerEmail, LocalDate eventDate) {
        List<CalendarEventResponse> events = calendarService.getEvents(ownerEmail, eventDate, eventDate);
        events.stream()
                .filter(event -> !"HARVEST_RECORD".equals(event.eventType()))
                .forEach(event -> notificationService.scheduleReminder(ownerEmail, event));
    }
}
