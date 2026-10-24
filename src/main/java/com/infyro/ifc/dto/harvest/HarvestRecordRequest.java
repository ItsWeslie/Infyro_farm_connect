package com.infyro.ifc.dto.harvest;

import com.infyro.ifc.enums.HarvestUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HarvestRecordRequest(
        @NotNull @PastOrPresent(message = "Harvest date cannot be in the future")
        LocalDate harvestDate,
        @NotNull @DecimalMin(value = "0.001", message = "Quantity must be greater than zero")
        @Digits(integer = 11, fraction = 3, message = "Quantity must have at most 11 integer digits and 3 decimal places")
        BigDecimal quantity,
        @NotNull HarvestUnit unit,
        @Size(max = 2000) String notes
) {}
