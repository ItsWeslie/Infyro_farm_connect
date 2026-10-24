package com.infyro.ifc.controller;

import com.infyro.ifc.api.APIConstants;
import com.infyro.ifc.dto.activity.CropActivityLogRequest;
import com.infyro.ifc.dto.activity.CropActivityLogResponse;
import com.infyro.ifc.enums.ActivityStatus;
import com.infyro.ifc.service.CropActivityLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(APIConstants.CROP_ACTIVITY)
public class CropActivityLogController {

    private final CropActivityLogService service;

    @PostMapping
    public ResponseEntity<CropActivityLogResponse> create(
            @Valid @RequestBody CropActivityLogRequest request, Principal principal) {
        return service.create(principal.getName(), request);
    }

    @GetMapping(APIConstants.CropActivity.BY_PLANTING)
    public ResponseEntity<List<CropActivityLogResponse>> getByPlanting(
            @PathVariable Long plantingId, Principal principal) {
        return service.getByPlanting(plantingId, principal.getName());
    }

    @GetMapping(APIConstants.CropActivity.BY_ID)
    public ResponseEntity<CropActivityLogResponse> getById(
            @PathVariable Long id, Principal principal) {
        return service.getById(id, principal.getName());
    }

    @PutMapping(APIConstants.CropActivity.BY_ID)
    public ResponseEntity<CropActivityLogResponse> update(
            @PathVariable Long id, @Valid @RequestBody CropActivityLogRequest request,
            Principal principal) {
        return service.update(id, principal.getName(), request);
    }

    @PatchMapping(APIConstants.CropActivity.STATUS)
    public ResponseEntity<CropActivityLogResponse> updateStatus(
            @PathVariable Long id, @RequestParam ActivityStatus status, Principal principal) {
        return service.updateStatus(id, principal.getName(), status);
    }

    @DeleteMapping(APIConstants.CropActivity.BY_ID)
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        return service.delete(id, principal.getName());
    }
}
