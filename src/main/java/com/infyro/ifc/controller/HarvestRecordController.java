package com.infyro.ifc.controller;

import com.infyro.ifc.dto.harvest.*;
import com.infyro.ifc.service.HarvestRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crop-plantings/{plantingId}/harvest-records")
public class HarvestRecordController {
    private final HarvestRecordService service;

    @PostMapping
    public ResponseEntity<HarvestRecordResponse> create(@PathVariable Long plantingId,
            @Valid @RequestBody HarvestRecordRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(plantingId, principal.getName(), request));
    }
    @GetMapping
    public List<HarvestRecordResponse> list(@PathVariable Long plantingId, Principal principal) {
        return service.list(plantingId, principal.getName());
    }
    @GetMapping("/{id}")
    public HarvestRecordResponse get(@PathVariable Long plantingId, @PathVariable Long id, Principal principal) {
        HarvestRecordResponse record = service.getById(id, principal.getName());
        if (!record.cropPlantingId().equals(plantingId)) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Harvest record not found");
        return record;
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long plantingId, @PathVariable Long id, Principal principal) {
        HarvestRecordResponse record = service.getById(id, principal.getName());
        if (!record.cropPlantingId().equals(plantingId)) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Harvest record not found");
        service.delete(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
