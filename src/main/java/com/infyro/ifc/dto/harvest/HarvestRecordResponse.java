package com.infyro.ifc.dto.harvest;

import com.infyro.ifc.enums.HarvestUnit;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record HarvestRecordResponse(Long id, Long cropPlantingId, LocalDate harvestDate,
        BigDecimal quantity, HarvestUnit unit, String notes, Instant createdAt, Instant updatedAt,
        boolean voided, String statusLabel, Instant voidedAt, String voidReason) {}
