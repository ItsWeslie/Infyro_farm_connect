package com.infyro.ifc.calendar;

import com.infyro.ifc.dto.calendar.CalendarEventResponse;
import com.infyro.ifc.dto.calendar.ReminderCandidate;
import com.infyro.ifc.enums.CropPlantingStatus;
import com.infyro.ifc.model.CropPlanting;
import com.infyro.ifc.repository.CropPlantingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CropPlantingEventProvider implements CalendarEventProvider {
    private static final List<CropPlantingStatus> REMINDER_ELIGIBLE_STATUSES =
            List.of(CropPlantingStatus.PLANNED, CropPlantingStatus.ACTIVE, CropPlantingStatus.HARVESTING);

    private final CropPlantingRepository cropPlantingRepository;

    @Override
    public List<CalendarEventResponse> findEvents(String ownerEmail, LocalDate from, LocalDate to) {
        List<CalendarEventResponse> events = new ArrayList<>();
        for (CropPlanting planting : cropPlantingRepository.findCalendarPlantings(ownerEmail, from, to)) {
            String cropName = planting.getCropType().getName();
            String farmName = planting.getField().getFarm().getFarmName();
            String fieldName = planting.getField().getFieldName();
            if (planting.getPlantedOn() != null && !planting.getPlantedOn().isBefore(from)
                    && !planting.getPlantedOn().isAfter(to)) {
                events.add(new CalendarEventResponse(
                        eventKey(planting.getId(), "PLANTING"), "PLANTING",
                        cropName + " planting", planting.getPlantedOn(), planting.getId(),
                        farmName, fieldName, planting.getStatus().name()));
            }
            if (planting.getExpectedHarvestOn() != null
                    && !planting.getExpectedHarvestOn().isBefore(from)
                    && !planting.getExpectedHarvestOn().isAfter(to)) {
                events.add(new CalendarEventResponse(
                        eventKey(planting.getId(), "HARVEST"), "HARVEST",
                        cropName + " expected harvest", planting.getExpectedHarvestOn(), planting.getId(),
                        farmName, fieldName, planting.getStatus().name()));
            }
        }
        return events;
    }

    @Override
    public List<ReminderCandidate> findReminderCandidates(LocalDate eventDate) {
        return cropPlantingRepository.findHarvestReminders(eventDate, REMINDER_ELIGIBLE_STATUSES)
                .stream()
                .map(planting -> new ReminderCandidate(
                        planting.getField().getFarm().getOwner().getId(),
                        eventKey(planting.getId(), "HARVEST"),
                        "HARVEST",
                        planting.getCropType().getName() + " harvest is scheduled for tomorrow",
                        planting.getExpectedHarvestOn()))
                .toList();
    }

    private String eventKey(Long plantingId, String type) {
        return "crop-planting:" + plantingId + ":" + type;
    }
}