package com.infyro.ifc.dto.crop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CropTypeRequest(
        @NotBlank(message = "Name must required")
        @Size(max = 100,message = "Name must be within 100 characters")
        String name,

        @Size(max = 500,message = "Description length should be within 500 characters")
        String description,

        @Positive(message = "Maturity time in days must be positive number")
        Integer defaultMaturityTimeInDays
) {}
