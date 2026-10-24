package com.infyro.ifc.mapper;

import com.infyro.ifc.dto.crop.CropPlantingResponse;
import com.infyro.ifc.model.CropPlanting;
import org.springframework.stereotype.Component;

@Component
public class CropPlantingMapper {

    public CropPlantingResponse toResponse(CropPlanting planting) {
        return new CropPlantingResponse(
                planting.getId(),
                planting.getField().getId(),
                planting.getCropType().getId(),
                planting.getCropType().getName(),
                planting.getCropArea(),
                planting.getPlantedOn(),
                planting.getExpectedHarvestOn(),
                planting.getCompletedOn(),
                planting.getMaturityTimeInDays(),
                planting.getStatus(),
                planting.getCreatedAt(),
                planting.getUpdatedAt());
    }
}
