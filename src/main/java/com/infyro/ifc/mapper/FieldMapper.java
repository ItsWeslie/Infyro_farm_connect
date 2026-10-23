package com.infyro.ifc.mapper;


import com.infyro.ifc.dto.field.FieldResponse;
import com.infyro.ifc.model.Field;
import org.springframework.stereotype.Component;

@Component
public class FieldMapper {
    public FieldResponse mapToFieldResponse(Field field){
        return FieldResponse.builder()
                .id(field.getId())
                .fieldName(field.getFieldName())
                .totalArea(field.getTotalArea())
                .availableArea(field.getAvailableArea())
                .build();
    }
}
