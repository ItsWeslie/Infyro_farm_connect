package com.infyro.ifc.controller;

import com.infyro.ifc.api.APIConstants;
import com.infyro.ifc.dto.crop.CropRequest;
import com.infyro.ifc.dto.crop.CropResponse;
import com.infyro.ifc.service.CropService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(APIConstants.CROP)
public class CropController {

    private final CropService service;

    @PostMapping(APIConstants.Crop.CREATE)
    public ResponseEntity<CropResponse> createNewCrop(@RequestBody CropRequest request){
        return service.CreateNewCrop(request);
    }

    @GetMapping(APIConstants.Crop.GET_CROP)
    public ResponseEntity<CropResponse> getCropWithID(@PathVariable Integer id){
        return service.GetCropWithID(id);
    }

    @PostMapping(APIConstants.Crop.CULTIVATE_CROP)
    public ResponseEntity<String> cultivateCrop(@PathVariable Long FieldID,@PathVariable Integer CropID){
        return service.CultivateCropWithID(FieldID,CropID);
    }

    @PostMapping(APIConstants.Crop.ALLOCATE_FIELD)
    public ResponseEntity<String> allocateField(@PathVariable Long FieldID,@PathVariable Integer CropID){
        return service.AllocateCropToField(FieldID,CropID);
    }

    @GetMapping(APIConstants.Crop.GET_ALL_CROPS)
    public ResponseEntity<List<CropResponse>> GetAllCrops(){
        return service.GetAllCropsAsList();
    }


}
