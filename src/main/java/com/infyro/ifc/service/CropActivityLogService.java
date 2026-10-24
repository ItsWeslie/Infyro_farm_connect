package com.infyro.ifc.service;

import com.infyro.ifc.dto.activity.CropActivityLogRequest;
import com.infyro.ifc.dto.activity.CropActivityLogResponse;
import com.infyro.ifc.enums.ActivityStatus;
import com.infyro.ifc.mapper.CropActivityLogMapper;
import com.infyro.ifc.model.CropActivityLog;
import com.infyro.ifc.model.CropPlanting;
import com.infyro.ifc.repository.CropActivityLogRepository;
import com.infyro.ifc.repository.CropPlantingRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CropActivityLogService {

    private final CropActivityLogRepository activityRepository;
    private final CropPlantingRepo plantingRepository;
    private final CropActivityLogMapper mapper;

    @Transactional
    public ResponseEntity<CropActivityLogResponse> create(
            String email, CropActivityLogRequest request) {
        CropPlanting planting = getOwnedPlanting(request.cropPlantingId(), email);
        CropActivityLog log = CropActivityLog.builder()
                .cropPlanting(planting)
                .dayNumber(request.dayNumber())
                .cropStage(request.cropStage())
                .dailyActivity(request.dailyActivity().trim())
                .nutritionAndWaterManagement(trimToNull(request.nutritionAndWaterManagement()))
                .pestAndDiseaseControl(trimToNull(request.pestAndDiseaseControl()))
                .notes(trimToNull(request.notes()))
                .status(request.status())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(activityRepository.save(log)));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<CropActivityLogResponse>> getByPlanting(
            Long plantingId, String email) {
        getOwnedPlanting(plantingId, email);
        List<CropActivityLogResponse> response = activityRepository
                .findAllByCropPlanting_IdAndCropPlanting_Field_Farm_Owner_EmailOrderByDayNumberAscCreatedAtAsc(
                        plantingId, email)
                .stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<CropActivityLogResponse> getById(Long id, String email) {
        return ResponseEntity.ok(mapper.toResponse(getOwnedActivity(id, email)));
    }

    @Transactional
    public ResponseEntity<CropActivityLogResponse> update(
            Long id, String email, CropActivityLogRequest request) {
        CropActivityLog log = getOwnedActivity(id, email);
        if (isTerminal(log.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Completed or cancelled activity logs cannot be edited");
        }
        CropPlanting planting = getOwnedPlanting(request.cropPlantingId(), email);
        log.setCropPlanting(planting);
        log.setDayNumber(request.dayNumber());
        log.setCropStage(request.cropStage());
        log.setDailyActivity(request.dailyActivity().trim());
        log.setNutritionAndWaterManagement(trimToNull(request.nutritionAndWaterManagement()));
        log.setPestAndDiseaseControl(trimToNull(request.pestAndDiseaseControl()));
        log.setNotes(trimToNull(request.notes()));
        log.setStatus(request.status());
        return ResponseEntity.ok(mapper.toResponse(activityRepository.save(log)));
    }

    @Transactional
    public ResponseEntity<CropActivityLogResponse> updateStatus(
            Long id, String email, ActivityStatus newStatus) {
        CropActivityLog log = getOwnedActivity(id, email);
        if (!isAllowedTransition(log.getStatus(), newStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid activity status transition");
        }
        log.setStatus(newStatus);
        return ResponseEntity.ok(mapper.toResponse(activityRepository.save(log)));
    }

    @Transactional
    public ResponseEntity<Void> delete(Long id, String email) {
        CropActivityLog log = getOwnedActivity(id, email);
        if (log.getStatus() != ActivityStatus.PLANNED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only planned activity logs can be deleted");
        }
        activityRepository.delete(log);
        return ResponseEntity.noContent().build();
    }

    private CropPlanting getOwnedPlanting(Long plantingId, String email) {
        return plantingRepository.findByIdAndField_Farm_Owner_Email(plantingId, email)
                .orElseThrow(() -> notFound("Crop planting not found"));
    }

    private CropActivityLog getOwnedActivity(Long id, String email) {
        return activityRepository.findByIdAndCropPlanting_Field_Farm_Owner_Email(id, email)
                .orElseThrow(() -> notFound("Crop activity log not found"));
    }

    private boolean isTerminal(ActivityStatus status) {
        return status == ActivityStatus.COMPLETED || status == ActivityStatus.CANCELLED;
    }

    private boolean isAllowedTransition(ActivityStatus oldStatus, ActivityStatus newStatus) {
        return switch (oldStatus) {
            case PLANNED -> newStatus == ActivityStatus.IN_PROGRESS
                    || newStatus == ActivityStatus.CANCELLED;
            case IN_PROGRESS -> newStatus == ActivityStatus.COMPLETED
                    || newStatus == ActivityStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
