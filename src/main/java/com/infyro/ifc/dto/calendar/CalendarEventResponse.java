package com.infyro.ifc.dto.calendar;

import java.time.LocalDate;

public record CalendarEventResponse(
        String eventKey,
        String eventType,
        String title,
        LocalDate eventDate,
        Long sourceId,
        String farmName,
        String fieldName,
        String status
) {}