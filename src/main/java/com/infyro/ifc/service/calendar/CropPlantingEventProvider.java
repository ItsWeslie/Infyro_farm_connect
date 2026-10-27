package com.infyro.ifc.service.calendar;

import com.infyro.ifc.dto.calendar.CalendarEventResponse;
import com.infyro.ifc.enums.CropPlantingStatus;
import com.infyro.ifc.model.CropPlanting;
import com.infyro.ifc.repository.CropPlantingCalendarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CropPlantingEventProvider implements EventProvider {
    private final CropPlantingCalendarRepository repository;

    @Override
    public List<CalendarEventResponse> findEvents(String ownerEmail, LocalDate from, LocalDate to) {
        List<CalendarEventResponse> events = new ArrayList<>();
        for (CropPlanting planting : repository.findCalendarPlantings(ownerEmail, from, to)) {
            String cropName = planting.getCropType().getName();
            if (!planting.getPlantedOn().isBefore(from) && !planting.getPlantedOn().isAfter(to)) {
                events.add(new CalendarEventResponse(
                        key("PLANTING", planting.getId()), "PLANTING",
                        "Plant " + cropName, planting.getPlantedOn(), planting.getId(),
                        planting.getStatus().name()));
            }
            if (planting.getStatus() != CropPlantingStatus.COMPLETED
                    && planting.getExpectedHarvestOn() != null
                    && !planting.getExpectedHarvestOn().isBefore(from)
                    && !planting.getExpectedHarvestOn().isAfter(to)) {
                events.add(new CalendarEventResponse(
                        key("EXPECTED_HARVEST", planting.getId()), "EXPECTED_HARVEST",
                        "Expected harvest: " + cropName, planting.getExpectedHarvestOn(), planting.getId(),
                        planting.getStatus().name()));
            }
        }
        return events;
    }

    private String key(String type, Long id) {
        return type + ":" + id;
    }
}
