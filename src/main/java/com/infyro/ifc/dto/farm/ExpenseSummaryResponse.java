package com.infyro.ifc.dto.farm;

import com.infyro.ifc.enums.ExpenseCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Builder
@AllArgsConstructor
public class ExpenseSummaryResponse{
        Long farmId;
        LocalDate from;
        LocalDate to;
        Long recordCount;
        BigDecimal totalAmount;
        Map<ExpenseCategory, BigDecimal> byCategory;
}
