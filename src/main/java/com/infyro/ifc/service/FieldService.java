package com.infyro.ifc.service;


import com.infyro.ifc.dto.APIResponse;
import com.infyro.ifc.dto.crop.CropRequest;
import com.infyro.ifc.dto.field.FieldResponse;
import com.infyro.ifc.exception.FarmNotFoundException;
import com.infyro.ifc.exception.FieldIsFullWithCropsException;
import com.infyro.ifc.exception.FieldNotFoundException;
import com.infyro.ifc.mapper.FieldMapper;
import com.infyro.ifc.model.Crop;
import com.infyro.ifc.model.Farm;
import com.infyro.ifc.model.Field;
import com.infyro.ifc.repository.FarmRepo;
import com.infyro.ifc.repository.FieldRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FieldService {

    private final FieldRepo fieldRepo;
    private final FarmRepo farmRepo;
    private final FieldMapper fieldMapper;

    @Transactional(readOnly = true)
    public ResponseEntity<List<FieldResponse>> getUnAllocatedFieldsOfAFarm(Long id){

        Farm existingFarm = farmRepo.findById(id)
                .orElseThrow(()-> new FarmNotFoundException("Farm not found with given id: "+id));

        List<FieldResponse> response = existingFarm.getFields()
                                            .stream()
                                            .filter(field -> field.getCrops().isEmpty())
                                            .map(fieldMapper::mapToFieldResponse).toList();

        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<APIResponse> mapCropsToField(Long fieldID, List<CropRequest> crops) {

        Field field = fieldRepo.findById(fieldID)
                .orElseThrow(()-> new FieldNotFoundException("Field not found with given id: "+fieldID));

        for (CropRequest cropRequest:crops){
            if(!canPlantCrops(field,fieldMapper.mapCropRequestToCrop(cropRequest))){
                throw new FieldIsFullWithCropsException("Sorry this field is full with crops!");
            }
        }

        List<Crop> newCrops = crops.stream()
                                    .map(fieldMapper::mapCropRequestToCrop)
                                    .toList();

        for (Crop crop:newCrops){
            field.setArea(field.getArea().subtract(crop.getCropArea()));
        }

        field.setCrops(newCrops);
        fieldRepo.save(field);

        return ResponseEntity.ok(new APIResponse("Field successfully mapped!"));
    }

    public boolean canPlantCrops(Field field,Crop crop){
        BigDecimal areaLeft = field.getArea();

        for (Crop c:field.getCrops()){
            areaLeft = areaLeft.subtract(c.getCropArea());
        }

        return areaLeft.subtract(crop.getCropArea()).intValue() >= 0;
    }
}
