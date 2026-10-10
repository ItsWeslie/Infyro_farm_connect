package com.infyro.ifc.dto.crop;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CropRequest (
        String name,
    BigDecimal cropArea,
    BigDecimal expense,
    LocalDate plantedOn,
    LocalDate cultivateOn,
    Integer maturityTimeInDays)
{ }
