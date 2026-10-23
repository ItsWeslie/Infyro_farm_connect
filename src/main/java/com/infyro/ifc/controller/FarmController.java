package com.infyro.ifc.controller;


import com.infyro.ifc.api.APIConstants;
import com.infyro.ifc.dto.APIResponse;
import com.infyro.ifc.dto.farm.FarmRequest;
import com.infyro.ifc.dto.farm.FarmResponse;
import com.infyro.ifc.service.FarmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(APIConstants.FARM)
@Slf4j
@RequiredArgsConstructor
public class FarmController {

    private final FarmService farmService;

    @PostMapping(APIConstants.Farm.CREATE)
    public ResponseEntity<FarmResponse> createNewFarm(@RequestBody @Valid FarmRequest req,
                                           Principal principal) {
        return farmService.createNewFarm(req,principal.getName());
    }

    @GetMapping(APIConstants.Farm.VIEW_FARM)
    public ResponseEntity<FarmResponse> getFarmByID(@PathVariable Long id,Principal principal) {
        return farmService.getFarmByID(id,principal.getName());
    }

    @GetMapping(APIConstants.Farm.OWNED_FARMS)
    public ResponseEntity<List<FarmResponse>> getFarmByOwner(Principal principal) {
        return farmService.getFarmsByOwner(principal.getName());

    }

    @DeleteMapping(APIConstants.Farm.DELETE)
    public ResponseEntity<APIResponse> deleteFarm(@PathVariable Long id,Principal principal) {
       return farmService.deleteFarm(id,principal.getName());
    }

    @PutMapping(APIConstants.Farm.UPDATE)
    public ResponseEntity<FarmResponse> updateFarm(@PathVariable Long id,Principal principal,
                                        @RequestBody FarmRequest request) {
            return farmService.updateFarm(id, principal.getName(), request);
    }
}
