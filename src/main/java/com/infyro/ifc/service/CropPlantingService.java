package com.infyro.ifc.service;

import com.infyro.ifc.dto.APIResponse;
import com.infyro.ifc.dto.crop.CropPlantingRequest;
import com.infyro.ifc.dto.crop.CropPlantingResponse;
import com.infyro.ifc.enums.CropPlantingStatus;
import com.infyro.ifc.mapper.CropPlantingMapper;
import com.infyro.ifc.model.CropPlanting;
import com.infyro.ifc.model.CropType;
import com.infyro.ifc.model.Field;
import com.infyro.ifc.repository.CropPlantingRepo;
import com.infyro.ifc.repository.CropTypeRepository;
import com.infyro.ifc.repository.FieldRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CropPlantingService {

    private final CropPlantingRepo plantingRepository;
    private final CropTypeRepository cropTypeRepository;
    private final FieldRepo fieldRepo;
    private final CropPlantingMapper cropPlantingMapper;

    @Transactional
    public ResponseEntity<CropPlantingResponse> create(Long fieldId,
                                                       String email,
                                                       CropPlantingRequest request) {
        Field field = getLockedField(fieldId, email);

        CropType cropType = getCropType(field, request.cropTypeId(), email);
        int maturity = resolveMaturity(request, cropType);
        validateDates(request.plantedOn(), request.expectedHarvestOn());

        if (request.cropArea().compareTo(field.getAvailableArea()) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Insufficient available field area");
        }

        LocalDate expectedHarvest = request.expectedHarvestOn() != null
                ? request.expectedHarvestOn()
                : request.plantedOn().plusDays(maturity);

        CropPlanting planting = CropPlanting.builder()
                .field(field)
                .cropType(cropType)
                .cropArea(request.cropArea())
                .plantedOn(request.plantedOn())
                .expectedHarvestOn(expectedHarvest)
                .maturityTimeInDays(maturity)
                .status(CropPlantingStatus.PLANNED)
                .build();

        CropPlanting saved = plantingRepository.save(planting);
        field.getCrops().add(saved);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cropPlantingMapper.toResponse(saved));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<CropPlantingResponse>> getAll(String email) {
        return ResponseEntity.ok(
                plantingRepository.findAllByField_Farm_Owner_Email(email)
                        .stream().map(cropPlantingMapper::toResponse).toList());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<CropPlantingResponse>> getByField(Long fieldId, String email) {
        if (fieldRepo.findByIdAndFarm_Owner_Email(fieldId, email).isEmpty()) {
            throw notFound("Field not found");
        }
        return ResponseEntity.ok(
                plantingRepository.findAllByField_IdAndField_Farm_Owner_Email(fieldId, email)
                        .stream().map(cropPlantingMapper::toResponse).toList());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<CropPlantingResponse> getById(Long id, String email) {
        CropPlanting planting = plantingRepository.findByIdAndField_Farm_Owner_Email(id, email)
                .orElseThrow(() -> notFound("Planting not found"));
        return ResponseEntity.ok(cropPlantingMapper.toResponse(planting));
    }

    @Transactional
    public ResponseEntity<CropPlantingResponse> update(Long id, String email,
                                                       CropPlantingRequest request) {
        CropPlanting planting = getPlanting(id, email);
        if (isTerminal(planting.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Completed or cancelled plantings cannot be updated");
        }

        Field field = getLockedField(planting.getField().getId(), email);
        BigDecimal difference = request.cropArea().subtract(planting.getCropArea());
        if (difference.compareTo(field.getAvailableArea()) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Insufficient available field area");
        }

        CropType cropType = getCropType(field, request.cropTypeId(), email);
        int maturity = resolveMaturity(request, cropType);
        validateDates(request.plantedOn(), request.expectedHarvestOn());

        LocalDate expectedHarvest = request.expectedHarvestOn() != null
                ? request.expectedHarvestOn()
                : request.plantedOn().plusDays(maturity);

        planting.setCropType(cropType);
        planting.setCropArea(request.cropArea());
        planting.setPlantedOn(request.plantedOn());
        planting.setExpectedHarvestOn(expectedHarvest);
        planting.setMaturityTimeInDays(maturity);

        return ResponseEntity.ok(cropPlantingMapper.toResponse(
                plantingRepository.save(planting)));
    }

    @Transactional
    public ResponseEntity<CropPlantingResponse> updateStatus(Long id, String email,
                                                             CropPlantingStatus newStatus) {
        CropPlanting planting = getPlanting(id, email);
        CropPlantingStatus oldStatus = planting.getStatus();

        if (!isAllowedTransition(oldStatus, newStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid planting status transition");
        }

        // Serialize lifecycle changes against field-area allocations and other
        // planting status changes for the same field.
        getLockedField(planting.getField().getId(), email);

        planting.setStatus(newStatus);
        if (newStatus == CropPlantingStatus.COMPLETED) {
            planting.setCompletedOn(LocalDate.now());
        } else if (newStatus == CropPlantingStatus.CANCELLED) {
            planting.setCompletedOn(null);
        }

        return ResponseEntity.ok(cropPlantingMapper.toResponse(
                plantingRepository.save(planting)));
    }

    @Transactional
    public ResponseEntity<APIResponse> delete(Long id, String email) {
        CropPlanting planting = getPlanting(id, email);
        if (planting.getStatus() != CropPlantingStatus.PLANNED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only planned plantings can be deleted; cancel active plantings instead");
        }

        Field field = getLockedField(planting.getField().getId(), email);
        field.getCrops().removeIf(crop -> crop.getId().equals(planting.getId()));
        plantingRepository.delete(planting);
        return ResponseEntity.ok(new APIResponse("Planting deleted successfully"));
    }

    private Field getLockedField(Long fieldId, String email) {
        return fieldRepo.findOwnedFieldForUpdate(fieldId, email)
                .orElseThrow(() -> notFound("Field not found"));
    }

    private CropPlanting getPlanting(Long id, String email) {
        return plantingRepository.findOwnedPlantingForUpdate(id, email)
                .orElseThrow(() -> notFound("Planting not found"));
    }

    private CropType getCropType(Field field, Long cropTypeId, String email) {
        return cropTypeRepository
                .findByIdAndFarm_IdAndFarm_Owner_Email(cropTypeId, field.getFarm().getId(), email)
                .orElseThrow(() -> notFound("Crop type not found in this farm"));
    }

    private int resolveMaturity(CropPlantingRequest request, CropType cropType) {
        Integer maturity = request.maturityTimeInDays() != null
                ? request.maturityTimeInDays()
                : cropType.getDefaultMaturityTimeInDays();
        if (maturity == null || maturity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Provide maturity days or configure a crop type default");
        }
        return maturity;
    }

    private void validateDates(LocalDate plantedOn, LocalDate expectedHarvestOn) {
        if (expectedHarvestOn != null && expectedHarvestOn.isBefore(plantedOn)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Expected harvest date cannot be before planting date");
        }
    }

    private boolean isTerminal(CropPlantingStatus status) {
        return status == CropPlantingStatus.COMPLETED || status == CropPlantingStatus.CANCELLED;
    }

    private boolean isAllowedTransition(CropPlantingStatus oldStatus,
                                        CropPlantingStatus newStatus) {
        return switch (oldStatus) {
            case PLANNED -> newStatus == CropPlantingStatus.ACTIVE
                    || newStatus == CropPlantingStatus.CANCELLED;
            case ACTIVE -> newStatus == CropPlantingStatus.HARVESTING
                    || newStatus == CropPlantingStatus.COMPLETED
                    || newStatus == CropPlantingStatus.CANCELLED;
            case HARVESTING -> newStatus == CropPlantingStatus.COMPLETED
                    || newStatus == CropPlantingStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
