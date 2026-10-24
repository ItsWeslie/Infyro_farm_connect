package com.infyro.ifc.dto.crop;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CropPlantingRequest(
        @NotNull(message = "Crop type is required")
        Long cropTypeId,

        @NotNull(message = "Crop area is required")
        @DecimalMin(value = "0.01", message = "Crop area must be greater than 0")
        BigDecimal cropArea,

        @NotNull(message = "Planting date is required")
        LocalDate plantedOn,

        LocalDate expectedHarvestOn,

        @Positive(message = "Maturity time must be greater than 0 days")
        Integer maturityTimeInDays
) {}
