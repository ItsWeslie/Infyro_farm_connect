package com.infyro.ifc.service.field;

import com.infyro.ifc.dto.api.ApiResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
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
                .legacyAvailableArea(request.totalArea())
                .farm(farm)
                .build();

        Field savedField = fieldRepo.save(field);
        log.info("FieldService-createField Field got created! \n field={}",field);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fieldMapper.mapToFieldResponse(savedField));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<FieldResponse>> getFieldsByFarm(Long farmId,
                                                               String email) {
        Farm farm = farmRepo.findByIdAndOwner_Email(farmId, email)
                .orElseThrow(() -> new FarmNotFoundException("Farm not found"));

        List<FieldResponse> fields = fieldRepo
                .findAllByFarm_IdAndFarm_Owner_Email(farm.getId(), email)
                .stream()
                .map(fieldMapper::mapToFieldResponse)
                .toList();
        log.info("FieldService-getFieldByFarm Executed! \n Data={}",fields);
        return ResponseEntity.ok(fields);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<FieldResponse> getFieldById(Long fieldId, String email) {
        Field field = getOwnedField(fieldId, email);
        log.info("FieldService-getFieldByID Executed for the ID={} \n Data={}",fieldId,field);
        return ResponseEntity.ok(fieldMapper.mapToFieldResponse(field));
    }

    @Transactional
    public ResponseEntity<FieldResponse> updateField(Long fieldId,
                                                     FieldUpdateRequest request,
                                                     String email) {
        Field field = getOwnedField(fieldId, email);
        field.setFieldName(request.fieldName().trim());
        Field updatedField = fieldRepo.save(field);
        log.info("FieldService-updateField Field updated! \n  updated_field={}",updatedField);
        return ResponseEntity.ok(fieldMapper.mapToFieldResponse(updatedField));
    }

    @Transactional
    public ResponseEntity<ApiResponse> deleteField(Long fieldId, String email) {
        Field field = getOwnedField(fieldId, email);
        if (field.getCrops() != null && !field.getCrops().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete a field while crops are assigned to it");
        }
        fieldRepo.delete(field);
        log.info("FieldService-deleteField Field deleted with the ID={}",fieldId);
        return ResponseEntity.ok(new ApiResponse("Field deleted successfully"));
    }

    private Field getOwnedField(Long fieldId, String email) {
        return fieldRepo.findByIdAndFarm_Owner_Email(fieldId, email)
                .orElseThrow(() ->
                        new FieldNotFoundException("Field not found with ID: " + fieldId));
    }
}
