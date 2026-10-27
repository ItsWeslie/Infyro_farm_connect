package com.infyro.ifc.service;

import com.infyro.ifc.dto.notification.DashboardNotificationResponse;
import com.infyro.ifc.model.DashboardNotification;
import com.infyro.ifc.model.User;
import com.infyro.ifc.repository.DashboardNotificationRepository;
import com.infyro.ifc.repository.UserEmailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardNotificationService {
    private final DashboardNotificationRepository repository;
    private final UserEmailRepository userRepository;
    private final ZoneId notificationZone;

    @Transactional(readOnly = true)
    public List<DashboardNotificationResponse> list(String email, boolean unreadOnly, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid pagination");
        }
        var pageable = PageRequest.of(page, size);
        var items = unreadOnly
                ? repository.findAllByOwner_EmailAndReadFalseOrderByScheduledAtDesc(email, pageable)
                : repository.findAllByOwner_EmailOrderByScheduledAtDesc(email, pageable);
        return items.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(String email) {
        return repository.countByOwner_EmailAndReadFalse(email);
    }

    @Transactional
    public DashboardNotificationResponse markRead(Long id, String email) {
        DashboardNotification notification = repository.findByIdAndOwner_Email(id, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(Instant.now());
        }
        return toResponse(notification);
    }

    @Transactional
    public void scheduleReminder(String ownerEmail, com.infyro.ifc.dto.calendar.CalendarEventResponse event) {
        User owner = userRepository.findByEmailIgnoreCase(ownerEmail)
                .orElseThrow(() -> new IllegalStateException("Event owner no longer exists"));
        Instant scheduledAt = event.eventDate().minusDays(1)
                .atTime(LocalTime.of(9, 0)).atZone(notificationZone).toInstant();
        var existing = repository.findByOwner_IdAndEventKey(owner.getId(), event.eventKey());
        DashboardNotification notification = existing.orElseGet(() -> DashboardNotification.builder()
                .owner(owner).eventKey(event.eventKey()).eventType(event.eventType())
                .title(event.title() + " is scheduled for tomorrow")
                .eventDate(event.eventDate()).scheduledAt(scheduledAt).build());
        notification.setEventType(event.eventType());
        notification.setTitle(event.title() + " is scheduled for tomorrow");
        notification.setEventDate(event.eventDate());
        notification.setScheduledAt(scheduledAt);
        if (existing.isPresent() && notification.isRead()) {
            notification.setRead(false);
            notification.setReadAt(null);
        }
        repository.save(notification);
    }

    private DashboardNotificationResponse toResponse(DashboardNotification n) {
        return new DashboardNotificationResponse(n.getId(), n.getEventKey(), n.getEventType(),
                n.getTitle(), n.getEventDate(), n.getScheduledAt(), n.isRead(), n.getReadAt());
    }
}
