package com.infyro.ifc.dto.crop;

import com.infyro.ifc.enums.CropPlantingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record CropPlantingResponse(
        Long id,
        Long fieldId,
        Long cropTypeId,
        String cropTypeName,
        BigDecimal cropArea,
        LocalDate plantedOn,
        LocalDate expectedHarvestOn,
        LocalDate completedOn,
        Integer maturityTimeInDays,
        CropPlantingStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
