package com.infyro.ifc.service.calendar;

import com.infyro.ifc.dto.calendar.CalendarEventResponse;
import com.infyro.ifc.model.HarvestRecord;
import com.infyro.ifc.repository.HarvestRecordCalendarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HarvestRecordEventProvider implements EventProvider {
    private final HarvestRecordCalendarRepository repository;

    @Override
    public List<CalendarEventResponse> findEvents(String ownerEmail, LocalDate from, LocalDate to) {
        return repository.findCalendarHarvests(ownerEmail, from, to).stream()
                .map(this::toEvent)
                .toList();
    }

    private CalendarEventResponse toEvent(HarvestRecord record) {
        String cropName = record.getCropPlanting().getCropType().getName();
        return new CalendarEventResponse("HARVEST_RECORD:" + record.getId(), "HARVEST_RECORD",
                "Harvest recorded: " + cropName, record.getHarvestDate(), record.getId(), "RECORDED");
    }
}
