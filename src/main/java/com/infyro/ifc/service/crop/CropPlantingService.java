package com.infyro.ifc.service.crop;

import com.infyro.ifc.dto.api.ApiResponse;
import com.infyro.ifc.dto.crop.CropPlantingRequest;
import com.infyro.ifc.dto.crop.CropPlantingResponse;
import com.infyro.ifc.enums.CropPlantingStatus;
import com.infyro.ifc.mapper.CropPlantingMapper;
import com.infyro.ifc.model.CropPlanting;
import com.infyro.ifc.model.CropType;
import com.infyro.ifc.model.Field;
import com.infyro.ifc.repository.CropActivityLogRepo;
import com.infyro.ifc.repository.CropPlantingRepo;
import com.infyro.ifc.repository.CropTypeRepo;
import com.infyro.ifc.repository.ExpenseRepo;
import com.infyro.ifc.repository.FieldRepo;
import com.infyro.ifc.repository.HarvestRecordRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CropPlantingService {

    private final CropPlantingRepo plantingRepository;
    private final CropTypeRepo cropTypeRepo;
    private final FieldRepo fieldRepo;
    private final CropPlantingMapper cropPlantingMapper;
    private final ExpenseRepo expenseRepo;
    private final HarvestRecordRepo harvestRecordRepo;
    private final CropActivityLogRepo activityLogRepository;

    @Transactional
    public ResponseEntity<CropPlantingResponse> create(Long fieldId, String email,
                                                       CropPlantingRequest request) {

        Field field = getLockedField(fieldId, email);
        CropType cropType = getCropType(field, request.cropTypeId(), email);
        int maturity = resolveMaturity(request, cropType);
        validateDates(request.plantedOn(), request.expectedHarvestOn());

        if (request.cropArea().compareTo(field.getAvailableArea()) > 0) {
            log.error("Error in CropPlantingService-create <Insufficient available field area> " +
                    "\n Data={}", request);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient available field area");
        }

        LocalDate expectedHarvest = request.expectedHarvestOn() != null
                ? request.expectedHarvestOn() : request.plantedOn().plusDays(maturity);

        CropPlanting planting = CropPlanting.builder()
                .field(field).cropType(cropType).cropArea(request.cropArea())
                .plantedOn(request.plantedOn()).expectedHarvestOn(expectedHarvest)
                .maturityTimeInDays(maturity).status(CropPlantingStatus.PLANNED).build();

        CropPlanting saved = plantingRepository.save(planting);
        field.getCrops().add(saved);

        log.info("CropPlantingService-create crop planted inside field {} \n Data={}",fieldId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cropPlantingMapper.toResponse(saved));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<CropPlantingResponse>> getAll(String email) {
        log.info("CropPlantingService-getAll Executed and list of all crops returned for {}",email);
        return ResponseEntity.ok(plantingRepository
                .findAllByField_Farm_Owner_Email(email)
                .stream().map(cropPlantingMapper::toResponse).toList());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<CropPlantingResponse>> getByField(Long fieldId, String email) {

        if (fieldRepo.findByIdAndFarm_Owner_Email(fieldId, email).isEmpty()) {
            log.error("CropPlantingService-getByField Not Executed! " +
                    "Field not found for fieldID={},Email={}",fieldId,email);

            throw notFound("Field not found");
        }
        log.info("CropPlantingService-getByField Executed for fieldID={},Email={}",fieldId,email);
        return ResponseEntity.ok(plantingRepository
                .findAllByField_IdAndField_Farm_Owner_Email(fieldId, email)
                .stream().map(cropPlantingMapper::toResponse).toList());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<CropPlantingResponse> getById(Long id, String email) {
        CropPlanting planting = plantingRepository
                .findByIdAndField_Farm_Owner_Email(id, email)
                .orElseThrow(() -> notFound("Planting not found"));

        log.info("CropPlantingService-getById Executed for ID={},Email={}",id,email);
        return ResponseEntity.ok(cropPlantingMapper.toResponse(planting));
    }

    @Transactional
    public ResponseEntity<CropPlantingResponse> update(Long id, String email, CropPlantingRequest request) {

        CropPlanting planting = getPlanting(id, email);

        if (isTerminal(planting.getStatus())) {
            log.error(
                    "CropPlantingService-update Failed! " +
                    "Completed or cancelled plantings cannot be updated for ID={},Email={} " +
                    "\n Data={}",id,email,request.toString());

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Completed or cancelled plantings cannot be updated");
        }

        if (harvestRecordRepo.existsByCropPlanting_Id(id)) {

            boolean plantingDateChanged = !planting.getPlantedOn().equals(request.plantedOn());

            boolean cropChanged = !planting.getCropType().getId().equals(request.cropTypeId());

            if (plantingDateChanged || cropChanged) {
                log.error("CropPlantingService-update Failed! " +
                        "Planting date and crop type cannot be changed after harvest history exists for " +
                        "\n Data={}", request);
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Planting date and crop type cannot be changed after harvest history exists");
            }
        }

        Field field = getLockedField(planting.getField().getId(), email);
        BigDecimal difference = request.cropArea().subtract(planting.getCropArea());

        if (difference.compareTo(field.getAvailableArea()) > 0) {
            log.error("""
                    CropPlantingService-update Failed! \
                    Insufficient available field area\s
                     available_area = {} \
                    
                     crop_area = {}""",field.getAvailableArea(),difference);

            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient available field area");
        }

        CropType cropType = getCropType(field, request.cropTypeId(), email);
        int maturity = resolveMaturity(request, cropType);
        validateDates(request.plantedOn(), request.expectedHarvestOn());

        LocalDate expectedHarvest = request.expectedHarvestOn() != null
                ? request.expectedHarvestOn() : request.plantedOn().plusDays(maturity);

        planting.setCropType(cropType);
        planting.setCropArea(request.cropArea());
        planting.setPlantedOn(request.plantedOn());
        planting.setExpectedHarvestOn(expectedHarvest);
        planting.setMaturityTimeInDays(maturity);

        log.info("CropPlantingService-update Request={} Updated plantingId={}", request,planting.getId());

        return ResponseEntity.ok(cropPlantingMapper.toResponse(plantingRepository.save(planting)));
    }

    @Transactional
    public ResponseEntity<CropPlantingResponse> updateStatus(Long id, String email, CropPlantingStatus newStatus) {

        CropPlanting planting = getPlanting(id, email);
        CropPlantingStatus oldStatus = planting.getStatus();

        if (!isAllowedTransition(oldStatus, newStatus)) {
            log.error("CropPlantingService-updateStatus Failed! " +
                    "Invalid planting status transition from {} to {}",oldStatus,newStatus);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid planting status transition");
        }

        getLockedField(planting.getField().getId(), email);
        planting.setStatus(newStatus);

        if (newStatus == CropPlantingStatus.COMPLETED) {
            planting.setCompletedOn(LocalDate.now());
        } else if (newStatus == CropPlantingStatus.CANCELLED) {
            planting.setCompletedOn(null);
        }

        log.info("CropPlantingService-update Status updated to {}",newStatus);
        return ResponseEntity.ok(cropPlantingMapper.toResponse(plantingRepository.save(planting)));
    }

    @Transactional
    public ResponseEntity<ApiResponse> delete(Long id, String email) {

        CropPlanting planting = getPlanting(id, email);

        if (planting.getStatus() != CropPlantingStatus.PLANNED) {
            log.error("CropPlantingService-delete Failed! " +
                    "Only planned plantings can be deleted;" +
                    " cancel active plantings instead. plantingId={}, status={}",id,planting.getStatus());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only planned plantings can be deleted; cancel active plantings instead");
        }
        if (expenseRepo.existsByCropPlanting_Id(id)
                || harvestRecordRepo.existsByCropPlanting_Id(id)
                || activityLogRepository.existsByCropPlanting_Id(id)) {

            log.error("CropPlantingService-delete Failed! " +
                    "Planting has expense, harvest, or activity history. plantingId={}",id);

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Planting cannot be deleted while expense, harvest, or activity history exists");
        }

        Field field = getLockedField(planting.getField().getId(), email);
        field.getCrops().removeIf(crop -> crop.getId().equals(planting.getId()));

        plantingRepository.delete(planting);
        log.info("CropPlantingService-delete Executed and Data Deleted! plantingId={}",id);
        return ResponseEntity.ok(new ApiResponse("Planting deleted successfully"));
    }

    private Field getLockedField(Long fieldId, String email) {
        log.info("CropPlantingService-getLockedField Executed for ID={},Email={}",fieldId,email);
        return fieldRepo.findOwnedFieldForUpdate(fieldId, email)
                .orElseThrow(() -> notFound("Field not found"));
    }

    private CropPlanting getPlanting(Long id, String email) {
        log.info("CropPlantingService-getPlanting Executed for ID={},Email={}",id,email);
        return plantingRepository.findOwnedPlantingForUpdate(id, email)
                .orElseThrow(() -> notFound("Planting not found"));
    }

    private CropType getCropType(Field field, Long cropTypeId, String email) {
        log.info("CropPlantingService-getCropType Executed!");
        return cropTypeRepo.findByIdAndFarm_IdAndFarm_Owner_Email(cropTypeId, field.getFarm().getId(), email)
                .orElseThrow(() -> notFound("Crop type not found in this farm"));
    }

    private int resolveMaturity(CropPlantingRequest request, CropType cropType) {

        Integer maturity = request.maturityTimeInDays() != null
                ? request.maturityTimeInDays() : cropType.getDefaultMaturityTimeInDays();

        if (maturity == null || maturity <= 0) {
            log.error("""
                    CropPlantingService-resolveMaturity Failed! \
                    Provide maturity days or configure a crop type default\s
                     \
                    CropType={}\s
                     Data={}""",cropType.toString(), request);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Provide maturity days or configure a crop type default");
        }
        return maturity;
    }

    private void validateDates(LocalDate plantedOn, LocalDate expectedHarvestOn) {
        if (expectedHarvestOn != null && expectedHarvestOn.isBefore(plantedOn)) {
            log.error("CropPlantingService-validateDates Failed! " +
                    "Expected harvest date cannot be before planting date " +
                    "\n PlantedOn = {},ExpectedHarvest = {} ",plantedOn,expectedHarvestOn);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Expected harvest date cannot be before planting date");
        }
        log.info("CropPlantingService-validateDates Executed!");
    }

    private boolean isTerminal(CropPlantingStatus status) {

        log.info("CropPlantingService-isTerminal Executed! {}",
                        status == CropPlantingStatus.COMPLETED
                        || status == CropPlantingStatus.CANCELLED);

        return status == CropPlantingStatus.COMPLETED || status == CropPlantingStatus.CANCELLED;
    }

    private boolean isAllowedTransition(CropPlantingStatus oldStatus, CropPlantingStatus newStatus) {

        log.info("CropPlantingService-isAllowedTransition oldStatus={},newStatus={}",oldStatus,newStatus);

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
        log.info("CropPlantingService-notFound {}",message);
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
