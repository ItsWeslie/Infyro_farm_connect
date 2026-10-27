package com.infyro.ifc.service;

import com.infyro.ifc.service.calendar.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ReminderScheduler {
    private final CalendarService calendarService;
    private final DashboardNotificationService notificationService;
    private final com.infyro.ifc.repository.UserEmailRepository userRepository;
    private final Clock notificationClock;

    /** Runs once daily at 9 AM in the configured notification time zone. */
    @Scheduled(cron = "0 0 9 * * *", zone = "${app.notification.time-zone:Asia/Kolkata}")
    public void createTomorrowReminders() {
        LocalDate tomorrow = LocalDate.now(notificationClock).plusDays(1);
        userRepository.findAllUsersWithFarms().stream()
                .map(user -> user.getEmail())
                .filter(email -> email != null && !email.isBlank())
                .forEach(email -> calendarService.getEvents(email, tomorrow, tomorrow).stream()
                        .filter(event -> !"HARVEST_RECORD".equals(event.eventType()))
                        .forEach(event -> notificationService.scheduleReminder(email, event)));
    }
}
