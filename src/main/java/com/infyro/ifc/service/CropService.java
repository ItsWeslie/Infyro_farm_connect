package com.infyro.ifc.service;

import com.infyro.ifc.api.APIConstants;
import com.infyro.ifc.dto.crop.CropRequest;
import com.infyro.ifc.dto.crop.CropResponse;
import com.infyro.ifc.exception.FieldIsFullWithCropsException;
import com.infyro.ifc.mapper.CropMapper;
import com.infyro.ifc.model.Crop;
import com.infyro.ifc.model.Farm;
import com.infyro.ifc.model.Field;
import com.infyro.ifc.repository.CropRepository;
import com.infyro.ifc.repository.FieldRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CropService {

    private CropRepository cropRepository;
    private CropMapper cropMapper;
    private FieldRepo fieldRepo;

    public ResponseEntity<CropResponse> CreateNewCrop(CropRequest cropRequest){

        Crop crop = Crop.builder()
                        .name(cropRequest.name())
                .cropArea(cropRequest.cropArea())
                .cultivateOn(cropRequest.cultivateOn())
                .expense(cropRequest.expense())
                .maturityTimeInDays(cropRequest.maturityTimeInDays())
                .plantedOn(cropRequest.plantedOn())
                .build();

        cropRepository.save(crop);

        CropResponse response = cropMapper.MapToCropResponse(crop);


        return ResponseEntity.ok().body(response);
    }

    public ResponseEntity<CropResponse> GetCropWithID(Integer id){
           Crop crop = cropRepository.findById(id).orElseThrow();

           CropResponse res = cropMapper.MapToCropResponse(crop);

           return ResponseEntity.ok().body(res);
    }

    public ResponseEntity<String> CultivateCropWithID(Long FieldID,Integer CropID){
        Crop crop = cropRepository.findById(CropID).orElseThrow();

        crop.setField(null);

        Field field = fieldRepo.findById(FieldID).orElseThrow();
        field.getCrops().removeIf(cr -> cr.getId().intValue() == CropID);

        fieldRepo.save(field);
        cropRepository.save(crop);

        return ResponseEntity.ok().body("Crop cultivated successfully!");

    }

    public ResponseEntity<String> AllocateCropToField(Long FieldID,Integer CropID){
            Field field = fieldRepo.findById(FieldID).orElseThrow();
            Crop crop = cropRepository.findById(CropID).orElseThrow();

            BigDecimal areaOccupied = BigDecimal.ZERO;
            for(Crop c:field.getCrops()){
                areaOccupied = areaOccupied.add(c.getCropArea());
            }

            if (areaOccupied.add(crop.getCropArea()).doubleValue() >=  field.getArea().doubleValue()){
                 throw new FieldIsFullWithCropsException("Sorry there is not enough space!");
            }

            field.getCrops().add(crop);
            crop.setField(field);

            fieldRepo.save(field);
            cropRepository.save(crop);



            return ResponseEntity.ok().body("Crop Allocated Successfully!");
    }

    public ResponseEntity<List<CropResponse>> GetAllCropsAsList(){
        List<CropResponse> responses = cropRepository.findAll()
                                                     .stream()
                                                     .map(response -> cropMapper.MapToCropResponse(response) )
                                                     .toList();

        return ResponseEntity.ok().body(responses);
    }
}
