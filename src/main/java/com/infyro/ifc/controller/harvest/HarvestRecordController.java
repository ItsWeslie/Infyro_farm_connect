package com.infyro.ifc.controller.harvest;

import com.infyro.ifc.api.APIConstants;
import com.infyro.ifc.dto.api.ApiResponse;
import com.infyro.ifc.dto.harvest.*;
import com.infyro.ifc.service.HarvestRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(APIConstants.HARVEST)
public class HarvestRecordController {

    private final HarvestRecordService service;

    @PostMapping
    public ResponseEntity<HarvestRecordResponse> create(@PathVariable Long plantingId,
                                                        @Valid @RequestBody HarvestRecordRequest request,
                                                        Principal principal) {
        return service.create(plantingId, principal.getName(), request);
    }

    @GetMapping
    public ResponseEntity<List<HarvestRecordResponse>> getAllHarvestRecordsByPlantingId(@PathVariable Long plantingId,
                                                                        Principal principal) {
        return service.getAllHarvestRecordsByPlantingId(plantingId, principal.getName());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HarvestRecordResponse> getByPlantingIdAndHarvestRecordId(@PathVariable Long plantingId,
                                                      @PathVariable Long id,
                                                      Principal principal) {

        return service.getByPlantingIdAndHarvestRecordId(id, plantingId,principal.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteByPlantingIdAndHarvestRecordId(@PathVariable Long plantingId,
                                                                            @PathVariable Long id,
                                                                            Principal principal) {
        return service.deleteByPlantingIdAndHarvestRecordId(id, plantingId,principal.getName());

    }
}
