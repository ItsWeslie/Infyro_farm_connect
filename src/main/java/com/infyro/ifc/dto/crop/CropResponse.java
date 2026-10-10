package com.infyro.ifc.dto.crop;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CropResponse {
    private Long id;
    private String name;
    private BigDecimal cropArea;
    private BigDecimal expense;
    private LocalDate plantedOn;
    private LocalDate cultivateOn;
    private Integer maturityTime;
}
