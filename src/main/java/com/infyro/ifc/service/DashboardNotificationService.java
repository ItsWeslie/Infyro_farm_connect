package com.infyro.ifc.service;

import com.infyro.ifc.dto.notification.DashboardNotificationResponse;
import com.infyro.ifc.model.DashboardNotification;
import com.infyro.ifc.repository.DashboardNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardNotificationService {
    private final DashboardNotificationRepository repository;

    @Transactional(readOnly = true)
    public List<DashboardNotificationResponse> list(String email, boolean unreadOnly, int page, int size) {

        if (page < 0 || size < 1 || size > 100) {
            log.error("DashboardNotificationService-list Failed Invalid pagination Page={},size={} ",page,size);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid pagination");
        }

        var pageable = PageRequest.of(page, size);
        var items = unreadOnly
                ? repository.findAllByOwner_EmailAndReadFalseOrderByScheduledAtDesc(email, pageable)
                : repository.findAllByOwner_EmailOrderByScheduledAtDesc(email, pageable);

        log.info("DashboardNotificationService-list Executed! \n Data={}",items
                .stream().map(this::toResponse).toList());

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

        log.info("DashboardNotificationService-markRead Executed! \n Notification={}",notification);
        return toResponse(notification);
    }

    private DashboardNotificationResponse toResponse(DashboardNotification n) {
        log.info("DashboardNotificationService-toResponse converted_data={}",n.toString());
        return new DashboardNotificationResponse(n.getId(), n.getEventKey(), n.getEventType(),
                n.getTitle(), n.getEventDate(), n.getScheduledAt(), n.isRead(), n.getReadAt());
    }
}