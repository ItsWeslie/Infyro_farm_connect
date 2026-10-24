package com.infyro.ifc.dto.activity;

import com.infyro.ifc.enums.ActivityStatus;
import com.infyro.ifc.enums.CropStage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CropActivityLogRequest(
        @NotNull(message = "Crop planting is required")
        Long cropPlantingId,

        @NotNull(message = "Crop-cycle day number is required")
        @Positive(message = "Crop-cycle day number must be positive")
        Integer dayNumber,

        CropStage cropStage,

        @NotBlank(message = "Daily activity is required")
        @Size(max = 2000, message = "Daily activity must be at most 2000 characters")
        String dailyActivity,

        @Size(max = 2000)
        String nutritionAndWaterManagement,

        @Size(max = 2000)
        String pestAndDiseaseControl,

        @Size(max = 2000)
        String notes,

        @NotNull(message = "Activity status is required")
        ActivityStatus status
) {}
