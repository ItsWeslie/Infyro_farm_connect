package com.infyro.ifc.service;

import com.infyro.ifc.dto.harvest.HarvestRecordRequest;
import com.infyro.ifc.dto.harvest.HarvestRecordResponse;
import com.infyro.ifc.enums.CropPlantingStatus;
import com.infyro.ifc.model.CropPlanting;
import com.infyro.ifc.model.HarvestRecord;
import com.infyro.ifc.repository.CropPlantingRepo;
import com.infyro.ifc.repository.HarvestRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HarvestRecordService {
    private final HarvestRecordRepository records;
    private final CropPlantingRepo plantings;

    @Transactional
    public HarvestRecordResponse create(Long plantingId, String email, HarvestRecordRequest request) {
        CropPlanting planting = getPlanting(plantingId, email);

        if (request.harvestDate() == null) {
            log.error("HarvestRecordService-create Failed! Harvest date is required \n HarvestDate={} ",request.harvestDate());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Harvest date is required");
        }
        if (request.harvestDate().isAfter(LocalDate.now())) {
            log.error("HarvestRecordService-create Failed! Harvest date cannot be in the future \n HarvestDate={}",request.harvestDate());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Harvest date cannot be in the future");
        }
        if (planting.getPlantedOn() == null || request.harvestDate().isBefore(planting.getPlantedOn())) {
            log.error("HarvestRecordService-create Failed! Harvest date cannot be before planting date \n HarvestDate={} \n PlantedOn={}",request.harvestDate(),planting.getPlantedOn());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Harvest date cannot be before planting date");
        }
        if (planting.getStatus() != CropPlantingStatus.ACTIVE
                && planting.getStatus() != CropPlantingStatus.HARVESTING) {
            log.error("HarvestRecordService-create Failed! Harvest records can only be added to active or harvesting plantings \n PlantingStatus={}",planting.getStatus());
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
        return response(records.save(record));
    }

    @Transactional(readOnly = true)
    public List<HarvestRecordResponse> list(Long plantingId, String email) {
        getPlanting(plantingId, email);
        log.info("HarvestRecordService-list Executed for the Planting_ID={}",plantingId);
        return records.findAllByCropPlanting_IdAndCropPlanting_Field_Farm_Owner_EmailOrderByHarvestDateDescCreatedAtDesc(
                        plantingId, email)
                .stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public HarvestRecordResponse getById(Long id, String email) {
        log.info("HarvestRecordService-getById Executed for the ID={}",id);
        return response(records.findByIdAndCropPlanting_Field_Farm_Owner_Email(id, email)
                .orElseThrow(() -> notFound("Harvest record not found")));
    }

    @Transactional
    public void delete(Long id, String email) {
        HarvestRecord record = records.findByIdAndCropPlanting_Field_Farm_Owner_Email(id, email)
                .orElseThrow(() -> notFound("Harvest record not found"));
        if (record.isVoided()) {
            log.error("HarvestRecordService-delete Failed!,Harvest record is already cancelled");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Harvest record is already cancelled");
        }
        record.setVoided(true);
        record.setVoidedAt(Instant.now());
        record.setVoidReason("Cancelled by owner; create a replacement harvest record to correct this entry");
        log.info("HarvestRecordService-delete Executed and record deleted!");
        records.save(record);
    }

    private CropPlanting getPlanting(Long id, String email) {
        log.info("HarvestRecordService-getPlanting Executed for the ID={}",id);
        return plantings.findByIdAndField_Farm_Owner_Email(id, email)
                .orElseThrow(() -> notFound("Crop planting not found"));
    }

    private HarvestRecordResponse response(HarvestRecord record) {
        return new HarvestRecordResponse(record.getId(), record.getCropPlanting().getId(),
                record.getHarvestDate(), record.getQuantity(), record.getUnit(),
                record.getNotes(), record.getCreatedAt(), record.getUpdatedAt(),
                record.isVoided(), record.isVoided() ? "Cancelled" : "Active",
                record.getVoidedAt(), record.getVoidReason());
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
