package com.infyro.ifc.dto.farm;

import com.infyro.ifc.model.Field;
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
    private List<Field> fields;
}
