package com.infyro.ifc.dto.farm;

import com.infyro.ifc.dto.field.FieldResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FarmResponse {
    private Long id;
    private String name;
    private List<FieldResponse> fields;
}
