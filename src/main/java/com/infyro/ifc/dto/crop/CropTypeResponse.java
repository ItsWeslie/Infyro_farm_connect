package com.infyro.ifc.dto.crop;

import java.time.Instant;

public record CropTypeResponse(
        Long id,
        String name,
        String description,
        Integer defaultMaturityTimeInDays,
        Long farmId,
        Instant createdAt,
        Instant updatedAt
) {}