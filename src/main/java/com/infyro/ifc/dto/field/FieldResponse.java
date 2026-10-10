package com.infyro.ifc.dto.field;


import com.infyro.ifc.dto.crop.CropResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FieldResponse {
    private Long id;
    private String fieldName;
    private BigDecimal area;
    private List<CropResponse> crops;
}
