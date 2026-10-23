package com.infyro.ifc.dto.field;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FieldResponse {
    private Long id;
    private String fieldName;
    private BigDecimal totalArea;
    private BigDecimal availableArea;
}
