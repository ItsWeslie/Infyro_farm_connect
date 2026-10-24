package com.infyro.ifc.controller;

import com.infyro.ifc.api.APIConstants;
import com.infyro.ifc.dto.APIResponse;
import com.infyro.ifc.dto.crop.CropTypeRequest;
import com.infyro.ifc.dto.crop.CropTypeResponse;
import com.infyro.ifc.service.CropTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(APIConstants.CROP_TYPE)
public class CropTypeController {

    private final CropTypeService cropTypeService;

    @PostMapping(APIConstants.CropType.CREATE)
    public ResponseEntity<CropTypeResponse> create(@PathVariable Long farmId,
                                                   @Valid @RequestBody CropTypeRequest request,
                                                   Principal principal) {
        return cropTypeService.create(farmId, principal.getName(), request);
    }

    @GetMapping(APIConstants.CropType.GET_ALL_CROPS)
    public ResponseEntity<List<CropTypeResponse>> getAll(@PathVariable Long farmId,
                                                         Principal principal) {
        return cropTypeService.getAll(farmId, principal.getName());
    }

    @GetMapping(APIConstants.CropType.GET_CROP)
    public ResponseEntity<CropTypeResponse> getById(@PathVariable Long farmId,
                                                    @PathVariable Long id,
                                                    Principal principal) {
        return cropTypeService.getById(farmId, id, principal.getName());
    }

    @PutMapping(APIConstants.CropType.UPDATE)
    public ResponseEntity<CropTypeResponse> update(@PathVariable Long farmId,
                                                   @PathVariable Long id,
                                                   @Valid @RequestBody CropTypeRequest request,
                                                   Principal principal) {

        return cropTypeService.update(farmId, id, principal.getName(), request);
    }

    @DeleteMapping(APIConstants.CropType.DELETE)
    public ResponseEntity<APIResponse> delete(@PathVariable Long farmId,
                                              @PathVariable Long id, Principal principal) {

        return cropTypeService.delete(farmId, id, principal.getName());
    }
}

