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
        String Email = principal.getName();

        return farmService.addNewFarm(req,Email);
    }

    @GetMapping(APIConstants.Farm.VIEW_FARM)
    public ResponseEntity<FarmResponse> getFarmWithID(@PathVariable Long id){
            return farmService.viewFarmData(id);
    }

    @GetMapping(APIConstants.Farm.OWNED_FARMS)
    public ResponseEntity<List<FarmResponse>> getOwnedFarms(Principal principal) {

        String Email = principal.getName();

        return farmService.getOwnedFarmsForUser(Email);

    }

    @DeleteMapping(APIConstants.Farm.REMOVE)
    public ResponseEntity<APIResponse> removeFarm(@PathVariable Long id){
       return farmService.removeFarmWithID(id);
    }

    @PutMapping(APIConstants.Farm.UPDATE)
    public ResponseEntity<FarmResponse> updateFarm(@PathVariable Long id,
                                        @RequestBody FarmRequest request) {
            return farmService.updateFarmWithID(id,request);
    }
}
