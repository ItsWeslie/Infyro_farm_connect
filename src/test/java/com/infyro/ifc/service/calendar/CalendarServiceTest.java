package com.infyro.ifc.service.calendar;

import com.infyro.ifc.dto.calendar.CalendarEventResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CalendarServiceTest {
    @Test
    void combinesAndSortsEventsFromProviders() {
        EventProvider plantingProvider = mock(EventProvider.class);
        EventProvider harvestProvider = mock(EventProvider.class);
        LocalDate from = LocalDate.parse("2026-12-01");
        LocalDate to = LocalDate.parse("2026-12-31");

        when(plantingProvider.findEvents("farmer@example.com", from, to)).thenReturn(List.of(
                new CalendarEventResponse("PLANTING:2", "PLANTING", "Plant tomato",
                        LocalDate.parse("2026-12-12"), 2L, "ACTIVE")));
        when(harvestProvider.findEvents("farmer@example.com", from, to)).thenReturn(List.of(
                new CalendarEventResponse("HARVEST:5", "HARVEST_RECORD", "Harvest recorded",
                        LocalDate.parse("2026-12-04"), 5L, "RECORDED")));

        CalendarService service = new CalendarService(List.of(plantingProvider, harvestProvider));
        List<CalendarEventResponse> events = service.getEvents("farmer@example.com", from, to);

        assertEquals(2, events.size());
        assertEquals("HARVEST:5", events.get(0).eventKey());
        assertEquals("PLANTING:2", events.get(1).eventKey());
        verify(plantingProvider).findEvents("farmer@example.com", from, to);
        verify(harvestProvider).findEvents("farmer@example.com", from, to);
    }

    @Test
    void rejectsInvalidDateRange() {
        CalendarService service = new CalendarService(List.of());
        assertThrows(ResponseStatusException.class, () -> service.getEvents(
                "farmer@example.com", LocalDate.parse("2026-12-31"), LocalDate.parse("2026-12-01")));
    }

    @Test
    void rejectsRangesLongerThanOneYear() {
        CalendarService service = new CalendarService(List.of());
        assertThrows(IllegalArgumentException.class, () -> service.getEvents(
                "farmer@example.com", LocalDate.parse("2026-01-01"), LocalDate.parse("2027-01-02")));
    }
}
