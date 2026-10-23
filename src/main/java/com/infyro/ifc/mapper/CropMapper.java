package com.infyro.ifc.mapper;

import com.infyro.ifc.dto.crop.CropResponse;
import com.infyro.ifc.model.Crop;
import org.springframework.stereotype.Component;

@Component
public class CropMapper {

    public CropResponse MapToCropResponse(Crop crop){
         return CropResponse.builder().name(crop.getName())
                 .id(crop.getId())
                 .cropArea(crop.getCropArea())
                 .expense(crop.getExpense())
                 .plantedOn(crop.getPlantedOn())
                 .cultivateOn(crop.getCultivateOn())
                 .maturityTime(crop.getMaturityTimeInDays())
                 .build();
    }
}
