package com.infyro.ifc.controller;

import com.infyro.ifc.api.APIConstants;
import com.infyro.ifc.dto.APIResponse;
import com.infyro.ifc.dto.crop.CropPlantingRequest;
import com.infyro.ifc.dto.crop.CropPlantingResponse;
import com.infyro.ifc.dto.crop.CropPlantingStatusRequest;
import com.infyro.ifc.service.CropPlantingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(APIConstants.CROP_PLANTING)
public class CropPlantingController {

    private final CropPlantingService plantingService;

    @PostMapping(APIConstants.CropPlanting.CREATE)
    public ResponseEntity<CropPlantingResponse> create(@PathVariable Long fieldId,
                                                       @Valid @RequestBody CropPlantingRequest request,
                                                       Principal principal) {
        return plantingService.create(fieldId, principal.getName(), request);
    }

    @GetMapping
    public ResponseEntity<List<CropPlantingResponse>> getAll(Principal principal) {
        return plantingService.getAll(principal.getName());
    }

    @GetMapping(APIConstants.CropPlanting.GET_BY_FIELD)
    public ResponseEntity<List<CropPlantingResponse>> getByField(@PathVariable Long fieldId,
                                                                 Principal principal) {
        return plantingService.getByField(fieldId, principal.getName());
    }

    @GetMapping(APIConstants.CropPlanting.GET_BY_ID)
    public ResponseEntity<CropPlantingResponse> getById(@PathVariable Long id,
                                                        Principal principal) {
        return plantingService.getById(id, principal.getName());
    }

    @PutMapping(APIConstants.CropPlanting.UPDATE)
    public ResponseEntity<CropPlantingResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody CropPlantingRequest request,
                                                       Principal principal) {
        return plantingService.update(id, principal.getName(), request);
    }

    @PatchMapping(APIConstants.CropPlanting.UPDATE_STATUS)
    public ResponseEntity<CropPlantingResponse> updateStatus(@PathVariable Long id,
                                                             @Valid @RequestBody CropPlantingStatusRequest request,
                                                             Principal principal) {
        return plantingService.updateStatus(id, principal.getName(), request.status());
    }

    @DeleteMapping(APIConstants.CropPlanting.DELETE)
    public ResponseEntity<APIResponse> delete(@PathVariable Long id, Principal principal) {
        return plantingService.delete(id, principal.getName());
    }
}
