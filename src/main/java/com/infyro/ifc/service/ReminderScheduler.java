package com.infyro.ifc.service;

import com.infyro.ifc.calendar.CalendarEventProvider;
import com.infyro.ifc.dto.calendar.ReminderCandidate;
import com.infyro.ifc.model.DashboardNotification;
import com.infyro.ifc.model.User;
import com.infyro.ifc.repository.DashboardNotificationRepository;
import com.infyro.ifc.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final DashboardNotificationRepository notificationRepository;
    private final UserRepo userRepository;
    private final Clock clock = Clock.system(APPLICATION_ZONE);

    @Scheduled(cron = "0 */5 * * * *", zone = "Asia/Kolkata")
    @Transactional
    public void createDueReminders() {
        var now = java.time.ZonedDateTime.now(clock);
        if (now.toLocalTime().isBefore(REMINDER_TIME)) {
            return;
        }
        LocalDate eventDate = now.toLocalDate().plusDays(1);
        for (CalendarEventProvider provider : providers) {
            for (ReminderCandidate candidate : provider.findReminderCandidates(eventDate)) {
                createIfAbsent(candidate);
            }
        }
    }

    private void createIfAbsent(ReminderCandidate candidate) {
        if (notificationRepository.existsByOwner_IdAndEventKey(candidate.ownerId(), candidate.eventKey())) {
            return;
        }
        User owner = userRepository.findById(candidate.ownerId()).orElse(null);
        if (owner == null) {
            return;
        }
        var scheduledAt = candidate.eventDate().minusDays(1)
                .atTime(REMINDER_TIME).atZone(APPLICATION_ZONE).toInstant();
        DashboardNotification notification = DashboardNotification.builder()
                .owner(owner)
                .eventKey(candidate.eventKey())
                .eventType(candidate.eventType())
                .title(candidate.title())
                .eventDate(candidate.eventDate())
                .scheduledAt(scheduledAt)
                .read(false)
                .build();
        try {
            notificationRepository.save(notification);
        } catch (DataIntegrityViolationException ignored) {
            // A concurrent scheduler instance already created this event reminder.
        }
    }
}