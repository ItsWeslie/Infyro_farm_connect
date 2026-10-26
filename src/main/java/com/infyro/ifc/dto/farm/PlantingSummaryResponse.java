package com.infyro.ifc.dto.farm;

import com.infyro.ifc.enums.CropPlantingStatus;
import java.util.Map;

public record PlantingSummaryResponse(Long farmId, long totalPlantings,
        Map<CropPlantingStatus, Long> countsByStatus) {}
