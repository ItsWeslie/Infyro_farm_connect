package com.infyro.ifc.model;

import com.infyro.ifc.enums.CropPlantingStatus;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldAreaReservationTest {
    @Test void onlyCapacityConsumingStatusesReserveArea() {
        Field field = field("10.00");
        field.setCrops(List.of(planting(field, "3.00", CropPlantingStatus.PLANNED), planting(field, "2.00", CropPlantingStatus.ACTIVE), planting(field, "1.50", CropPlantingStatus.HARVESTING), planting(field, "4.00", CropPlantingStatus.COMPLETED), planting(field, "2.00", CropPlantingStatus.CANCELLED)));
        assertEquals(new BigDecimal("3.50"), field.getAvailableArea());
    }

    @Test void cancellationAndCompletionReleaseCapacityButKeepPlantingHistory() {
        Field field = field("10.00");
        CropPlanting planned = planting(field, "3.00", CropPlantingStatus.PLANNED);
        CropPlanting active = planting(field, "4.00", CropPlantingStatus.ACTIVE);
        field.setCrops(new ArrayList<>(List.of(planned, active)));
        assertEquals(new BigDecimal("3.00"), field.getAvailableArea());
        planned.setStatus(CropPlantingStatus.CANCELLED);
        assertEquals(new BigDecimal("6.00"), field.getAvailableArea());
        assertEquals(2, field.getCrops().size());
        active.setStatus(CropPlantingStatus.COMPLETED);
        assertEquals(new BigDecimal("10.00"), field.getAvailableArea());
        assertEquals(2, field.getCrops().size());
    }

    private Field field(String area) { return Field.builder().totalArea(new BigDecimal(area)).crops(new ArrayList<>()).build(); }
    private CropPlanting planting(Field field, String area, CropPlantingStatus status) {
        return CropPlanting.builder().field(field).cropArea(new BigDecimal(area)).plantedOn(LocalDate.of(2026, 1, 1)).maturityTimeInDays(40).status(status).build();
    }
}
