package com.infyro.ifc.service;

import com.infyro.ifc.dto.api.ApiResponse;
import com.infyro.ifc.dto.harvest.HarvestRecordRequest;
import com.infyro.ifc.dto.harvest.HarvestRecordResponse;
import com.infyro.ifc.enums.CropPlantingStatus;
import com.infyro.ifc.exception.CropPlantingNotFoundException;
import com.infyro.ifc.exception.HarvestRecordNotFoundException;
import com.infyro.ifc.mapper.HarvestRecordMapper;
import com.infyro.ifc.model.CropPlanting;
import com.infyro.ifc.model.HarvestRecord;
import com.infyro.ifc.repository.CropPlantingRepo;
import com.infyro.ifc.repository.HarvestRecordRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HarvestRecordService {

    private final HarvestRecordRepo harvestRecordRepo;
    private final CropPlantingRepo plantings;
    private final HarvestRecordMapper harvestRecordMapper;

    @Transactional
    public ResponseEntity<HarvestRecordResponse> create(Long plantingId,
                                                        String email,
                                                        HarvestRecordRequest request) {

        CropPlanting planting = getPlanting(plantingId, email);

        if (planting.getPlantedOn() == null || request.harvestDate().isBefore(planting.getPlantedOn())) {
            log.error("""
                    HarvestRecordService-create Failed! \
                    Harvest date cannot be before planting date\s
                     HarvestDate={}\s
                     PlantedOn={}""",request.harvestDate(),planting.getPlantedOn());

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Harvest date cannot be before planting date");
        }
        if (planting.getStatus() != CropPlantingStatus.ACTIVE
                && planting.getStatus() != CropPlantingStatus.HARVESTING) {

            log.error("HarvestRecordService-create Failed! " +
                    "Harvest records can only be added to active or harvesting plantings " +
                    "\n PlantingStatus={}",planting.getStatus());

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Harvest records can only be added to active or harvesting plantings");
        }

        HarvestRecord record = HarvestRecord.builder()
                .cropPlanting(planting)
                .harvestDate(request.harvestDate())
                .quantity(request.quantity())
                .unit(request.unit())
                .notes(request.notes() == null || request.notes().isBlank() ? null : request.notes().trim())
                .build();

        log.info("HarvestRecordService-create Executed and record created! \n Record={}",record.toString());

        HarvestRecordResponse response = harvestRecordMapper.response(harvestRecordRepo.save(record));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<HarvestRecordResponse>> getAllHarvestRecordsByPlantingId(Long plantingId,
                                                                                        String email) {

        getPlanting(plantingId, email);

        log.info("HarvestRecordService-list Executed for the Planting_ID={}",plantingId);

        List<HarvestRecordResponse> response = harvestRecordRepo
                .findByPlanting(
                        plantingId, email)
                            .stream()
                            .map(harvestRecordMapper::response)
                            .toList();
        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<HarvestRecordResponse> getByPlantingIdAndHarvestRecordId(Long id,
                                                                                   Long plantingId,
                                                                                   String email) {
        log.info("HarvestRecordService-getById Executed for the ID={}",id);

        HarvestRecord harvestRecord = harvestRecordRepo.findByIdAndCropPlanting_Field_Farm_Owner_Email(id, email)
                .orElseThrow(() -> new HarvestRecordNotFoundException("Harvest record not found"));

        if (!harvestRecord.getCropPlanting().getId().equals(plantingId))
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Harvest record not found");

        return ResponseEntity.ok(harvestRecordMapper.response(harvestRecord));
    }

    @Transactional
    public ResponseEntity<ApiResponse> deleteByPlantingIdAndHarvestRecordId(Long id, Long plantingId, String email) {

        HarvestRecord record = harvestRecordRepo.findByIdAndCropPlanting_Field_Farm_Owner_Email(id, email)
                .orElseThrow(() -> new HarvestRecordNotFoundException("Harvest record not found"));

        if (!record.getCropPlanting().getId().equals(plantingId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Harvest record not found");

        if (record.isVoided()) {
            log.error("HarvestRecordService-delete Failed!,Harvest record is already cancelled");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Harvest record is already cancelled");
        }

        record.setVoided(true);
        record.setVoidedAt(Instant.now());
        record.setVoidReason("Cancelled by owner; create a replacement harvest record to correct this entry");
        log.info("HarvestRecordService-delete Executed and record deleted!");
        harvestRecordRepo.save(record);

        return ResponseEntity.ok(new ApiResponse("Harvest record deleted Successfully"));
    }

    private CropPlanting getPlanting(Long id, String email) {
        log.info("HarvestRecordService-getPlanting Executed for the ID={}",id);
        return plantings.findByIdAndField_Farm_Owner_Email(id, email)
                .orElseThrow(() -> new CropPlantingNotFoundException("Crop planting not found"));
    }
}
