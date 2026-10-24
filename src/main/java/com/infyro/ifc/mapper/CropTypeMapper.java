package com.infyro.ifc.mapper;

import com.infyro.ifc.dto.crop.CropTypeResponse;
import com.infyro.ifc.model.CropType;
import org.springframework.stereotype.Component;

@Component
public class CropTypeMapper {
    public CropTypeResponse toResponse(CropType cropType) {
        return new CropTypeResponse(
                cropType.getId(),
                cropType.getName(),
                cropType.getDescription(),
                cropType.getDefaultMaturityTimeInDays(),
                cropType.getFarm().getId(),
                cropType.getCreatedAt(),
                cropType.getUpdatedAt());
    }
}
