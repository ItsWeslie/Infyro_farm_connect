package com.infyro.ifc.service;

import com.infyro.ifc.calendar.CalendarEventProvider;
import com.infyro.ifc.dto.calendar.ReminderCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReminderScheduler {
    private static final LocalTime REMINDER_TIME = LocalTime.of(9, 0);
    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Kolkata");

    private final List<CalendarEventProvider> providers;
    private final ReminderNotificationCreator notificationCreator;
    private final Clock clock = Clock.system(APPLICATION_ZONE);

    @Scheduled(cron = "0 */5 * * * *", zone = "Asia/Kolkata")
    public void createDueReminders() {
        var now = java.time.ZonedDateTime.now(clock);
        if (now.toLocalTime().isBefore(REMINDER_TIME)) {
            return;
        }
        LocalDate eventDate = now.toLocalDate().plusDays(1);
        for (CalendarEventProvider provider : providers) {
            for (ReminderCandidate candidate : provider.findReminderCandidates(eventDate)) {
                try {
                    notificationCreator.createIfAbsent(candidate);
                } catch (DataIntegrityViolationException ignored) {
                    // The unique owner/event constraint protects against concurrent scheduler instances.
                }
            }
        }
    }
}