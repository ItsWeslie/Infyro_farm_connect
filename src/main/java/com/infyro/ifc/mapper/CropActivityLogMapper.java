package com.infyro.ifc.mapper;

import com.infyro.ifc.dto.activity.CropActivityLogResponse;
import com.infyro.ifc.model.CropActivityLog;
import org.springframework.stereotype.Component;

@Component
public class CropActivityLogMapper {

    public CropActivityLogResponse toResponse(CropActivityLog log) {
        return new CropActivityLogResponse(
                log.getId(),
                log.getCropPlanting().getId(),
                log.getDayNumber(),
                log.getCropStage(),
                log.getDailyActivity(),
                log.getNutritionAndWaterManagement(),
                log.getPestAndDiseaseControl(),
                log.getNotes(),
                log.getStatus(),
                log.getCreatedAt(),
                log.getUpdatedAt());
    }
}
