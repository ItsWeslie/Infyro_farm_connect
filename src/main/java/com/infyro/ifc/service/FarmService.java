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
    public ResponseEntity<FarmResponse> addNewFarm(FarmRequest req,String username){

        User user = userRepo.findUserByEmail(username)
                .orElseThrow(()-> new UserNotFoundException("user not found"));

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
    public ResponseEntity<FarmResponse> viewFarmData(Long id) {

        Farm farm = farmRepo.findById(id)
                .orElseThrow(()-> new FarmNotFoundException("There is no farm found with given id:"+id));

        FarmResponse response = FarmResponse.builder()
                .id(farm.getId())
                .name(farm.getFarmName())
                .owner(farm.getOwner())
                .fields(farm.getFields())
                .build();

        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<FarmResponse>> getOwnedFarmsForUser(String username) {

        User owner = userRepo.findUserByUsername(username)
                .orElseThrow(()-> new UserNotFoundException("user not found with given username: "+username));

        List<FarmResponse> responses = owner.getFarms()
                .stream()
                .map(farmMapper::mapToFarmResponse).toList();

        return ResponseEntity.ok(responses);
    }

    @Transactional
    public ResponseEntity<APIResponse> removeFarmWithID(Long id){

            boolean flag = farmRepo.existsById(id);

            if(!flag){
                    throw new FarmNotFoundException("Farm not found with given id:"+id);
            }

            farmRepo.deleteById(id);
            return ResponseEntity.ok(new APIResponse("Farm deleted successfully"));
    }

    @Transactional
    public ResponseEntity<FarmResponse> updateFarmWithID(Long id,FarmRequest request) {
        Farm oldFarm = farmRepo.findById(id)
                .orElseThrow(()-> new FarmNotFoundException("Farm not found with given id: "+id));

        oldFarm.setFarmName(request.farmName());

        Farm updatedFarm = farmRepo.save(oldFarm);
        return ResponseEntity.ok(farmMapper.mapToFarmResponse(updatedFarm));
    }

}
