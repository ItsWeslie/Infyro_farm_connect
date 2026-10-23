package com.infyro.ifc.dto.field;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FieldRequest(
        @NotBlank(message = "Field name is required")
        String fieldName,

        @NotNull(message = "Field area is required")
        @DecimalMin(value = "0.01", message = "Field area must be greater than zero")
        BigDecimal totalArea
) {}
