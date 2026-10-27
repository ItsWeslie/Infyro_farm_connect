package com.infyro.ifc.dto.calendar;

import java.time.LocalDate;

public record ReminderCandidate(
        Long ownerId,
        String eventKey,
        String eventType,
        String title,
        LocalDate eventDate
) {}