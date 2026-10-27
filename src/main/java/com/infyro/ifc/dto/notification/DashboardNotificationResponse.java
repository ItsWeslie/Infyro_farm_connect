package com.infyro.ifc.dto.notification;

import java.time.Instant;
import java.time.LocalDate;

public record DashboardNotificationResponse(
        Long id,
        String eventKey,
        String eventType,
        String title,
        LocalDate eventDate,
        Instant scheduledAt,
        boolean read,
        Instant readAt
) {}
