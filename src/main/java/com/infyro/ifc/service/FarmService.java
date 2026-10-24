package com.infyro.ifc.service;

import com.infyro.ifc.dto.APIResponse;
import com.infyro.ifc.dto.farm.FarmRequest;
import com.infyro.ifc.dto.farm.FarmResponse;
import com.infyro.ifc.exception.FarmNotFoundException;
import com.infyro.ifc.exception.UserNotFoundException;
import com.infyro.ifc.mapper.FarmMapper;
import com.infyro.ifc.model.Farm;
import com.infyro.ifc.model.User;
import com.infyro.ifc.repository.FarmRepo;
import com.infyro.ifc.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FarmService {

    private final FarmRepo farmRepo;
    private final UserRepo userRepo;
    private final FarmMapper farmMapper;

    @Transactional
    public ResponseEntity<FarmResponse> createNewFarm(FarmRequest req,String Email){

        User user = userRepo.findUserByEmail(Email)
                .orElseThrow(()-> new UserNotFoundException("User not found"));

        Farm newFarm = Farm.builder()
                            .farmName(req.farmName())
                            .owner(user)
                            .build();

        Farm createdFarm = farmRepo.save(newFarm);

        FarmResponse response = farmMapper.mapToFarmResponse(createdFarm);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }



    @Transactional(readOnly = true)
    public ResponseEntity<FarmResponse> getFarmByID(Long id, String Email) {

        Farm farm = farmRepo.findByIdAndOwner_Email(id, Email)
                .orElseThrow(() ->
                        new FarmNotFoundException("Farm not found with ID: " + id));

        FarmResponse response = farmMapper.mapToFarmResponse(farm);

        return ResponseEntity.ok(response);
    }


    @Transactional(readOnly = true)
    public ResponseEntity<List<FarmResponse>> getFarmsByOwner(String email) {

        List<FarmResponse> responses = farmRepo.findAllByOwner_Email(email)
                .stream()
                .map(farmMapper::mapToFarmResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @Transactional
    public ResponseEntity<APIResponse> deleteFarm(Long id, String email) {

        boolean exists = farmRepo.existsByIdAndOwner_Email(id, email);

        if (!exists) {
            throw new FarmNotFoundException(
                    "Farm not found with given id: " + id
            );
        }

        farmRepo.deleteByIdAndOwner_Email(id, email);

        return ResponseEntity.ok(
                new APIResponse("Farm deleted successfully")
        );
    }

    @Transactional
    public ResponseEntity<FarmResponse> updateFarm(
            Long id, String email, FarmRequest request) {

        Farm oldFarm = farmRepo.findByIdAndOwner_Email(id, email)
                .orElseThrow(() ->
                        new FarmNotFoundException(
                                "Farm not found with given id: " + id
                        ));

        oldFarm.setFarmName(request.farmName());

        Farm updatedFarm = farmRepo.save(oldFarm);

        return ResponseEntity.ok(
                farmMapper.mapToFarmResponse(updatedFarm)
        );
    }

}
