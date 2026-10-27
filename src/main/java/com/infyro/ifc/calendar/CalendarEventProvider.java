package com.infyro.ifc.calendar;

import com.infyro.ifc.dto.calendar.CalendarEventResponse;
import com.infyro.ifc.dto.calendar.ReminderCandidate;

import java.time.LocalDate;
import java.util.List;

public interface CalendarEventProvider {
    List<CalendarEventResponse> findEvents(String ownerEmail, LocalDate from, LocalDate to);
    List<ReminderCandidate> findReminderCandidates(LocalDate eventDate);
}