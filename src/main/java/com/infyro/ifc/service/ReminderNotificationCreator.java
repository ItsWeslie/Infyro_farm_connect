package com.infyro.ifc.service;

import com.infyro.ifc.dto.calendar.ReminderCandidate;
import com.infyro.ifc.model.DashboardNotification;
import com.infyro.ifc.repository.DashboardNotificationRepository;
import com.infyro.ifc.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ReminderNotificationCreator {
    private static final LocalTime REMINDER_TIME = LocalTime.of(9, 0);
    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Kolkata");

    private final DashboardNotificationRepository notificationRepository;
    private final UserRepo userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createIfAbsent(ReminderCandidate candidate) {
        if (notificationRepository.existsByOwner_IdAndEventKey(candidate.ownerId(), candidate.eventKey())) {
            return;
        }
        var owner = userRepository.findById(candidate.ownerId()).orElse(null);
        if (owner == null) {
            return;
        }
        var scheduledAt = candidate.eventDate().minusDays(1)
                .atTime(REMINDER_TIME).atZone(APPLICATION_ZONE).toInstant();
        notificationRepository.save(DashboardNotification.builder()
                .owner(owner)
                .eventKey(candidate.eventKey())
                .eventType(candidate.eventType())
                .title(candidate.title())
                .eventDate(candidate.eventDate())
                .scheduledAt(scheduledAt)
                .read(false)
                .build());
    }
}