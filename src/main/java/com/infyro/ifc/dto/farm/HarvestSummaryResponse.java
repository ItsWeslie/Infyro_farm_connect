package com.infyro.ifc.dto.farm;

import com.infyro.ifc.enums.HarvestUnit;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record HarvestSummaryResponse(Long farmId, LocalDate from, LocalDate to, long recordCount,
        Map<HarvestUnit, BigDecimal> quantityByUnit) {}
