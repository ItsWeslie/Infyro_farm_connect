package com.infyro.ifc.service.calendar;

import com.infyro.ifc.dto.calendar.CalendarEventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService {
    private final List<EventProvider> eventProviders;

    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getEvents(String ownerEmail, LocalDate from, LocalDate to) {
        if (ownerEmail == null || ownerEmail.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authenticated user email is required");
        }
        if (from == null || to == null || to.isBefore(from)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid calendar date range");
        }
        if (to.isAfter(from.plusYears(1))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Calendar date range cannot exceed one year");
        }
        return eventProviders.stream()
                .flatMap(provider -> provider.findEvents(ownerEmail, from, to).stream())
                .sorted(Comparator.comparing(CalendarEventResponse::eventDate)
                        .thenComparing(CalendarEventResponse::eventType)
                        .thenComparing(CalendarEventResponse::eventKey))
                .toList();
    }
}
