package com.infyro.ifc.mapper;

import com.infyro.ifc.dto.crop.CropRequest;
import com.infyro.ifc.dto.crop.CropResponse;
import com.infyro.ifc.dto.field.FieldResponse;
import com.infyro.ifc.model.Crop;
import com.infyro.ifc.model.Field;
import org.springframework.stereotype.Component;

@Component
public class FieldMapper {

    public Crop mapCropRequestToCrop(CropRequest cropRequest){
        return Crop.builder()
                .cropArea(cropRequest.cropArea())
                .expense(cropRequest.expense())
                .name(cropRequest.name())
                .plantedOn(cropRequest.plantedOn())
                .cultivateOn(cropRequest.cultivateOn())
                .maturityTimeInDays(cropRequest.maturityTimeInDays())
                .build();
    }

    public FieldResponse mapToFieldResponse(Field field){
        return FieldResponse.builder()
                .id(field.getId())
                .fieldName(field.getFieldName())
                .area(field.getArea())
                .crops(field.getCrops().stream().map(this::mapToCropResponse).toList())
                .build();
    }

    public CropResponse mapToCropResponse(Crop crop){
        return CropResponse.builder()
                .id(crop.getId())
                .name(crop.getName())
                .cropArea(crop.getCropArea())
                .expense(crop.getExpense())
                .plantedOn(crop.getPlantedOn())
                .cultivateOn(crop.getCultivateOn())
                .maturityTime(crop.getMaturityTimeInDays())
                .build();
    }

}
