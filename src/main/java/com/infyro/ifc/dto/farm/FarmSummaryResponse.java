package com.infyro.ifc.dto.farm;

import com.infyro.ifc.enums.CropPlantingStatus;
import com.infyro.ifc.enums.ExpenseCategory;
import com.infyro.ifc.enums.HarvestUnit;
import java.math.BigDecimal;
import java.util.Map;

public record FarmSummaryResponse(Long farmId, String farmName, long totalFields, long totalPlantings,
        Map<CropPlantingStatus, Long> plantingsByStatus, BigDecimal totalExpenses,
        Map<ExpenseCategory, BigDecimal> expensesByCategory,
        Map<HarvestUnit, BigDecimal> harvestQuantityByUnit, long harvestRecordCount) {}
