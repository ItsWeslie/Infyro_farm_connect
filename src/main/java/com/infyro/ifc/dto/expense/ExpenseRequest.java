package com.infyro.ifc.dto.expense;

import com.infyro.ifc.enums.ExpenseCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        Long cropPlantingId,
        @NotNull @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,
        @NotNull @PastOrPresent(message = "Expense date cannot be in the future")
        LocalDate expenseDate,
        @NotNull ExpenseCategory category,
        @NotBlank @Size(max = 1000) String description
) {}
