package com.infyro.ifc.service;


import com.infyro.ifc.dto.APIResponse;
import com.infyro.ifc.dto.field.FieldRequest;
import com.infyro.ifc.dto.field.FieldResponse;
import com.infyro.ifc.dto.field.FieldUpdateRequest;
import com.infyro.ifc.exception.FarmNotFoundException;
import com.infyro.ifc.exception.FieldNotFoundException;
import com.infyro.ifc.mapper.FieldMapper;
import com.infyro.ifc.model.Farm;
import com.infyro.ifc.model.Field;
import com.infyro.ifc.repository.FarmRepo;
import com.infyro.ifc.repository.FieldRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FieldService {

    private final FieldRepo fieldRepo;
    private final FarmRepo farmRepo;
    private final FieldMapper fieldMapper;


    @Transactional
    public ResponseEntity<FieldResponse> createField(Long farmId,
                                                     FieldRequest request, String email) {

        Farm farm = farmRepo.findByIdAndOwner_Email(farmId, email)
                .orElseThrow(() ->
                        new FarmNotFoundException("Farm not found with ID: " + farmId));

        Field field = Field.builder()
                .fieldName(request.fieldName().trim())
                .totalArea(request.totalArea())
                .availableArea(request.totalArea())
                .farm(farm)
                .build();

        Field savedField = fieldRepo.save(field);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(fieldMapper.mapToFieldResponse(savedField));
    }


    @Transactional(readOnly = true)
    public ResponseEntity<List<FieldResponse>> getFieldsByFarm(Long farmId,
                                                               String email) {

        Farm farm = farmRepo.findByIdAndOwner_Email(farmId, email)
                .orElseThrow(() -> new FarmNotFoundException("Farm not found"));

        List<FieldResponse> fields =
                fieldRepo.findAllByFarm_IdAndFarm_Owner_Email(farm.getId(), email)
                            .stream()
                            .map(fieldMapper::mapToFieldResponse)
                            .toList();

        return ResponseEntity.ok(fields);
    }


    @Transactional(readOnly = true)
    public ResponseEntity<FieldResponse> getFieldById(Long fieldId, String email) {

        Field field = getOwnedField(fieldId, email);

        return ResponseEntity.ok(fieldMapper.mapToFieldResponse(field));
    }

    @Transactional
    public ResponseEntity<FieldResponse> updateField(Long fieldId,
                                                     FieldUpdateRequest request,
                                                     String email) {

        Field field = getOwnedField(fieldId, email);

        field.setFieldName(request.fieldName().trim());

        Field updatedField = fieldRepo.save(field);

        return ResponseEntity.ok(fieldMapper.mapToFieldResponse(updatedField));
    }

    @Transactional
    public ResponseEntity<APIResponse> deleteField(Long fieldId, String email) {

        Field field = getOwnedField(fieldId, email);

        if (field.getCrops() != null && !field.getCrops().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a field while crops are assigned to it");
        }

        fieldRepo.delete(field);

        return ResponseEntity.ok(new APIResponse("Field deleted successfully"));
    }

    private Field getOwnedField(Long fieldId, String email) {
        return fieldRepo.findByIdAndFarm_Owner_Email(fieldId, email)
                .orElseThrow(() ->
                        new FieldNotFoundException("Field not found with ID: " + fieldId));
    }
}
