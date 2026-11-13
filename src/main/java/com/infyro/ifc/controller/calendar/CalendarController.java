package com.infyro.ifc.controller.calendar;

import com.infyro.ifc.api.APIConstants;
import com.infyro.ifc.dto.calendar.CalendarEventResponse;
import com.infyro.ifc.service.calendar.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(APIConstants.CALENDAR)
@RequiredArgsConstructor
public class CalendarController {
    private final CalendarService calendarService;

    @GetMapping(APIConstants.Calendar.EVENTS)
    public List<CalendarEventResponse> getEvents(
            Principal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return calendarService.getEvents(principal.getName(), from, to);
    }
}