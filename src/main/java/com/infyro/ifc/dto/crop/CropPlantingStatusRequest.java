package com.infyro.ifc.dto.crop;

import com.infyro.ifc.enums.CropPlantingStatus;
import jakarta.validation.constraints.NotNull;

public record CropPlantingStatusRequest(
        @NotNull(message = "Crop planting status is required")
        CropPlantingStatus status
) {}
