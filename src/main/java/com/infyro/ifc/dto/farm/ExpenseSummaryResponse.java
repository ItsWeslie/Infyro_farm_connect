package com.infyro.ifc.dto.farm;

import com.infyro.ifc.enums.ExpenseCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record ExpenseSummaryResponse(Long farmId, LocalDate from, LocalDate to, long recordCount,
        BigDecimal totalAmount, Map<ExpenseCategory, BigDecimal> byCategory) {}
