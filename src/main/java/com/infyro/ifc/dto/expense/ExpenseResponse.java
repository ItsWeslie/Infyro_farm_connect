package com.infyro.ifc.dto.expense;

import com.infyro.ifc.enums.ExpenseCategory;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ExpenseResponse(Long id, Long farmId, Long cropPlantingId,
        BigDecimal amount, LocalDate expenseDate, ExpenseCategory category,
        String description, Instant createdAt, Instant updatedAt) {}
