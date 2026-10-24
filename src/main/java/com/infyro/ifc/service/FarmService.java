package com.infyro.ifc.service;

import com.infyro.ifc.dto.APIResponse;
import com.infyro.ifc.dto.farm.FarmRequest;
import com.infyro.ifc.dto.farm.FarmResponse;
import com.infyro.ifc.exception.FarmNotFoundException;
import com.infyro.ifc.exception.UserNotFoundException;
import com.infyro.ifc.mapper.FarmMapper;
import com.infyro.ifc.model.Farm;
import com.infyro.ifc.model.User;
import com.infyro.ifc.repository.ExpenseRepository;
import com.infyro.ifc.repository.FarmRepo;
import com.infyro.ifc.repository.HarvestRecordRepository;
import com.infyro.ifc.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FarmService {

    private final FarmRepo farmRepo;
    private final UserRepo userRepo;
    private final FarmMapper farmMapper;
    private final ExpenseRepository expenseRepository;
    private final HarvestRecordRepository harvestRecordRepository;

    @Transactional
    public ResponseEntity<FarmResponse> createNewFarm(FarmRequest req, String email) {
        User user = userRepo.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Farm newFarm = Farm.builder()
                .farmName(req.farmName())
                .owner(user)
                .build();

        Farm createdFarm = farmRepo.save(newFarm);
        return ResponseEntity.status(HttpStatus.CREATED).body(farmMapper.mapToFarmResponse(createdFarm));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<FarmResponse> getFarmByID(Long id, String email) {
        Farm farm = farmRepo.findByIdAndOwner_Email(id, email)
                .orElseThrow(() -> new FarmNotFoundException("Farm not found with ID: " + id));
        return ResponseEntity.ok(farmMapper.mapToFarmResponse(farm));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<FarmResponse>> getFarmsByOwner(String email) {
        List<FarmResponse> responses = farmRepo.findAllByOwner_Email(email)
                .stream().map(farmMapper::mapToFarmResponse).toList();
        return ResponseEntity.ok(responses);
    }

    @Transactional
    public ResponseEntity<APIResponse> deleteFarm(Long id, String email) {
        boolean exists = farmRepo.existsByIdAndOwner_Email(id, email);
        if (!exists) {
            throw new FarmNotFoundException("Farm not found with given id: " + id);
        }

        if (expenseRepository.existsByFarm_Id(id)
                || harvestRecordRepository.existsByCropPlanting_Field_Farm_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Farm cannot be deleted while expense or harvest history exists");
        }

        farmRepo.deleteByIdAndOwner_Email(id, email);
        return ResponseEntity.ok(new APIResponse("Farm deleted successfully"));
    }

    @Transactional
    public ResponseEntity<FarmResponse> updateFarm(Long id, String email, FarmRequest request) {
        Farm oldFarm = farmRepo.findByIdAndOwner_Email(id, email)
                .orElseThrow(() -> new FarmNotFoundException("Farm not found with given id: " + id));
        oldFarm.setFarmName(request.farmName());
        Farm updatedFarm = farmRepo.save(oldFarm);
        return ResponseEntity.ok(farmMapper.mapToFarmResponse(updatedFarm));
    }
}
