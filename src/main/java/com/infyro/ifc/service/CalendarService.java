package com.infyro.ifc.service;

import com.infyro.ifc.calendar.CalendarEventProvider;
import com.infyro.ifc.dto.calendar.CalendarEventResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarService {
    private final List<CalendarEventProvider> providers;

    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getEvents(String ownerEmail, LocalDate from, LocalDate to) {
        if (ownerEmail == null || ownerEmail.isBlank()) {
            log.error("CalenderService-getEvents Authentication is required");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        if (from == null || to == null || to.isBefore(from) || ChronoUnit.DAYS.between(from, to) > 366) {
            log.error("CalenderService-getEvents Provide a valid date range of no more than 367 days");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Provide a valid date range of no more than 367 days");
        }
        log.info("CalenderService-getEvents Executed Successfully!");
        return providers.stream()
                .flatMap(provider -> provider.findEvents(ownerEmail, from, to).stream())
                .sorted(Comparator.comparing(CalendarEventResponse::eventDate)
                        .thenComparing(CalendarEventResponse::eventKey))
                .toList();
    }
}