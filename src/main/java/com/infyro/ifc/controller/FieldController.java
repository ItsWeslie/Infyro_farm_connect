package com.infyro.ifc.controller;

import com.infyro.ifc.api.APIConstants;
import com.infyro.ifc.dto.APIResponse;
import com.infyro.ifc.dto.crop.CropRequest;
import com.infyro.ifc.dto.field.FieldResponse;
import com.infyro.ifc.service.FieldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(APIConstants.FIELD)
@Slf4j
@RequiredArgsConstructor
public class FieldController {

    private final FieldService fieldService;

    @GetMapping(APIConstants.Field.UNALLOCATED_FIELDS)
    public ResponseEntity<List<FieldResponse>> getUnAllocatedFields(@PathVariable Long id) {
        return fieldService.getUnAllocatedFieldsOfAFarm(id);
    }

    @PostMapping(APIConstants.Field.MAP_CROP)
    public ResponseEntity<APIResponse> mapCropToField(@PathVariable Long id,
                                                      @RequestParam List<CropRequest> crops){
            return fieldService.mapCropsToField(id,crops);
    }

}
