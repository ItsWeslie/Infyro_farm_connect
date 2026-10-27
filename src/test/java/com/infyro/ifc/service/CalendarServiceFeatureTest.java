package com.infyro.ifc.service;

import com.infyro.ifc.calendar.CalendarEventProvider;
import com.infyro.ifc.dto.calendar.CalendarEventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CalendarServiceFeatureTest {
    private CalendarEventProvider first;
    private CalendarEventProvider second;
    private CalendarService service;

    @BeforeEach
    void setUp() {
        first = mock(CalendarEventProvider.class);
        second = mock(CalendarEventProvider.class);
        service = new CalendarService(List.of(first, second));
    }

    @Test
    void mergesProvidersAndSortsByDateThenEventKey() {
        LocalDate from = LocalDate.of(2026, 10, 1), to = LocalDate.of(2026, 10, 31);
        var z = event("z", LocalDate.of(2026, 10, 5));
        var a = event("a", LocalDate.of(2026, 10, 5));
        var earlier = event("later-key", LocalDate.of(2026, 10, 2));
        when(first.findEvents("owner@example.com", from, to)).thenReturn(List.of(z, earlier));
        when(second.findEvents("owner@example.com", from, to)).thenReturn(List.of(a));

        assertEquals(List.of(earlier, a, z), service.getEvents("owner@example.com", from, to));
    }

    @Test
    void acceptsSingleDayAndMaximum367DayInclusiveRange() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        when(first.findEvents(anyString(), any(), any())).thenReturn(List.of());
        when(second.findEvents(anyString(), any(), any())).thenReturn(List.of());

        assertDoesNotThrow(() -> service.getEvents("owner@example.com", start, start));
        assertDoesNotThrow(() -> service.getEvents("owner@example.com", start, start.plusDays(366)));
    }

    @Test
    void rejectsNullBlankOwnerAndNullDatesAsAppropriate() {
        LocalDate day = LocalDate.of(2026, 10, 1);
        assertStatus(HttpStatus.UNAUTHORIZED, () -> service.getEvents(null, day, day));
        assertStatus(HttpStatus.UNAUTHORIZED, () -> service.getEvents("  ", day, day));
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.getEvents("owner@example.com", null, day));
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.getEvents("owner@example.com", day, null));
        verifyNoInteractions(first, second);
    }

    @Test
    void rejectsInvertedAndOverlongRangesWithoutCallingProviders() {
        LocalDate day = LocalDate.of(2026, 1, 1);
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.getEvents("owner@example.com", day.plusDays(1), day));
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.getEvents("owner@example.com", day, day.plusDays(367)));
        verifyNoInteractions(first, second);
    }

    private static CalendarEventResponse event(String key, LocalDate date) {
        return new CalendarEventResponse(key, "PLANTING", key, date, 1L, "Farm", "Field", "PLANNED");
    }

    private static void assertStatus(HttpStatus expected, org.junit.jupiter.api.function.Executable action) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, action);
        assertEquals(expected, ex.getStatusCode());
    }
}