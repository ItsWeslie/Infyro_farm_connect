package com.infyro.ifc.dto.activity;

import com.infyro.ifc.enums.ActivityStatus;
import com.infyro.ifc.enums.CropStage;

import java.time.Instant;

public record CropActivityLogResponse(
        Long id,
        Long cropPlantingId,
        Integer dayNumber,
        CropStage cropStage,
        String dailyActivity,
        String nutritionAndWaterManagement,
        String pestAndDiseaseControl,
        String notes,
        ActivityStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
