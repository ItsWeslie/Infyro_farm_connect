package com.infyro.ifc.service;

import com.infyro.ifc.calendar.CalendarEventProvider;
import com.infyro.ifc.dto.calendar.CalendarEventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CalendarServiceTest {
    private CalendarEventProvider provider;
    private CalendarService service;

    @BeforeEach
    void setUp() {
        provider = mock(CalendarEventProvider.class);
        service = new CalendarService(List.of(provider));
    }

    @Test
    void returnsEventsSortedByDateAndStableEventKey() {
        LocalDate from = LocalDate.of(2026, 12, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);
        CalendarEventResponse later = new CalendarEventResponse(
                "event-b", "HARVEST", "Harvest", LocalDate.of(2026, 12, 10),
                2L, "Farm", "Field", "ACTIVE");
        CalendarEventResponse earlier = new CalendarEventResponse(
                "event-a", "PLANTING", "Planting", LocalDate.of(2026, 12, 5),
                1L, "Farm", "Field", "PLANNED");
        when(provider.findEvents("farmer@example.com", from, to)).thenReturn(List.of(later, earlier));

        List<CalendarEventResponse> events = service.getEvents("farmer@example.com", from, to);

        assertEquals(List.of(earlier, later), events);
        verify(provider).findEvents("farmer@example.com", from, to);
    }

    @Test
    void rejectsInvertedDateRange() {
        LocalDate from = LocalDate.of(2026, 12, 31);
        LocalDate to = LocalDate.of(2026, 12, 1);

        assertThrows(ResponseStatusException.class,
                () -> service.getEvents("farmer@example.com", from, to));
        verifyNoInteractions(provider);
    }

    @Test
    void rejectsDateRangesLongerThanOneYear() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2027, 1, 3);

        assertThrows(ResponseStatusException.class,
                () -> service.getEvents("farmer@example.com", from, to));
        verifyNoInteractions(provider);
    }

    @Test
    void rejectsMissingOwnerIdentity() {
        assertThrows(ResponseStatusException.class,
                () -> service.getEvents(" ", LocalDate.now(), LocalDate.now().plusDays(1)));
        verifyNoInteractions(provider);
    }
}