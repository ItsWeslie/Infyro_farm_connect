package com.infyro.ifc.service;

import com.infyro.ifc.dto.harvest.*;
import com.infyro.ifc.model.CropPlanting;
import com.infyro.ifc.model.HarvestRecord;
import com.infyro.ifc.repository.CropPlantingRepo;
import com.infyro.ifc.repository.HarvestRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HarvestRecordService {
    private final HarvestRecordRepository records;
    private final CropPlantingRepo plantings;

    @Transactional
    public HarvestRecordResponse create(Long plantingId, String email, HarvestRecordRequest request) {
        CropPlanting planting = getPlanting(plantingId, email);
        if (request.harvestDate().isBefore(planting.getPlantedOn()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Harvest date cannot be before planting date");
        if (planting.getExpectedHarvestOn() != null && request.harvestDate().isBefore(planting.getExpectedHarvestOn())
                && planting.getStatus() == com.infyro.ifc.enums.CropPlantingStatus.PLANNED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Activate planting before recording an early harvest");
        HarvestRecord record = HarvestRecord.builder().cropPlanting(planting)
                .harvestDate(request.harvestDate()).quantity(request.quantity()).unit(request.unit())
                .notes(request.notes() == null || request.notes().isBlank() ? null : request.notes().trim()).build();
        return response(records.save(record));
    }

    @Transactional(readOnly = true)
    public List<HarvestRecordResponse> list(Long plantingId, String email) {
        getPlanting(plantingId, email);
        return records.findAllByCropPlanting_IdAndCropPlanting_Field_Farm_Owner_EmailOrderByHarvestDateDescCreatedAtDesc(plantingId, email)
                .stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public HarvestRecordResponse getById(Long id, String email) {
        return response(records.findByIdAndCropPlanting_Field_Farm_Owner_Email(id, email)
                .orElseThrow(() -> notFound("Harvest record not found")));
    }

    @Transactional
    public void delete(Long id, String email) {
        HarvestRecord record = records.findByIdAndCropPlanting_Field_Farm_Owner_Email(id, email)
                .orElseThrow(() -> notFound("Harvest record not found"));
        records.delete(record);
    }

    private CropPlanting getPlanting(Long id, String email) {
        return plantings.findByIdAndField_Farm_Owner_Email(id, email)
                .orElseThrow(() -> notFound("Crop planting not found"));
    }
    private HarvestRecordResponse response(HarvestRecord r) {
        return new HarvestRecordResponse(r.getId(), r.getCropPlanting().getId(), r.getHarvestDate(),
                r.getQuantity(), r.getUnit(), r.getNotes(), r.getCreatedAt(), r.getUpdatedAt());
    }
    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
