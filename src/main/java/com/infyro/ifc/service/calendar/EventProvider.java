package com.infyro.ifc.service.calendar;

import com.infyro.ifc.dto.calendar.CalendarEventResponse;
import java.time.LocalDate;
import java.util.List;

/** Strategy contract for adapting a domain source into calendar events. */
public interface EventProvider {
    List<CalendarEventResponse> findEvents(String ownerEmail, LocalDate from, LocalDate to);
}
