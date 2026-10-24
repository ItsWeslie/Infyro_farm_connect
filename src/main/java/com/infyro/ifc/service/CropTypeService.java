package com.infyro.ifc.service;

import com.infyro.ifc.dto.APIResponse;
import com.infyro.ifc.dto.crop.CropTypeRequest;
import com.infyro.ifc.dto.crop.CropTypeResponse;
import com.infyro.ifc.mapper.CropTypeMapper;
import com.infyro.ifc.model.CropType;
import com.infyro.ifc.model.Farm;
import com.infyro.ifc.repository.CropPlantingRepo;
import com.infyro.ifc.repository.CropTypeRepository;
import com.infyro.ifc.repository.FarmRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CropTypeService {

    private final CropTypeRepository cropTypeRepository;
    private final CropPlantingRepo cropPlantingRepository;
    private final FarmRepo farmRepo;
    private final CropTypeMapper cropTypeMapper;

    @Transactional
    public ResponseEntity<CropTypeResponse> create(Long farmId,
                                                   String email,
                                                   CropTypeRequest request) {

        Farm farm = getFarm(farmId, email);
        ensureUniqueName(farmId, request.name(), null);

        CropType cropType = CropType.builder()
                .name(request.name().trim())
                .description(request.description())
                .defaultMaturityTimeInDays(request.defaultMaturityTimeInDays())
                .farm(farm)
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cropTypeMapper.toResponse(cropTypeRepository.save(cropType)));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<CropTypeResponse>> getAll(Long farmId, String email) {

        getFarm(farmId, email);

        List<CropTypeResponse> result = cropTypeRepository
                        .findAllByFarm_IdAndFarm_Owner_Email(farmId, email)
                        .stream()
                        .map(cropTypeMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(result);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<CropTypeResponse> getById(Long farmId, Long id, String email) {

        return ResponseEntity.ok(
                cropTypeMapper.toResponse(getCropType(farmId, id, email)));
    }

    @Transactional
    public ResponseEntity<CropTypeResponse> update(Long farmId,
                                                   Long id,
                                                   String email,
                                                   CropTypeRequest request) {

        CropType cropType = getCropType(farmId, id, email);
        ensureUniqueName(farmId, request.name(), id);

        cropType.setName(request.name().trim());
        cropType.setDescription(request.description());
        cropType.setDefaultMaturityTimeInDays(request.defaultMaturityTimeInDays());

        return ResponseEntity.ok(cropTypeMapper
                .toResponse(cropTypeRepository.save(cropType)));
    }

    @Transactional
    public ResponseEntity<APIResponse> delete(Long farmId, Long id, String email) {

        CropType cropType = getCropType(farmId, id, email);

        if (cropPlantingRepository.existsByCropType_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Crop type is used by existing plantings");
        }

        cropTypeRepository.delete(cropType);

        return ResponseEntity.ok(new APIResponse("Crop type deleted successfully"));
    }

    private Farm getFarm(Long farmId, String email) {
        return farmRepo.findByIdAndOwner_Email(farmId, email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Farm not found"));
    }

    private CropType getCropType(Long farmId, Long id, String email) {

        return cropTypeRepository.findByIdAndFarm_IdAndFarm_Owner_Email(id, farmId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Crop type not found"));
    }

    private void ensureUniqueName(Long farmId, String name, Long excludedId) {

        boolean exists = excludedId == null
                ? cropTypeRepository.existsByFarm_IdAndNameIgnoreCase(farmId, name.trim())
                : cropTypeRepository
                    .existsByFarm_IdAndNameIgnoreCaseAndIdNot(farmId, name.trim(), excludedId);

        if (exists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Crop type name already exists in this farm");
        }
    }


}
