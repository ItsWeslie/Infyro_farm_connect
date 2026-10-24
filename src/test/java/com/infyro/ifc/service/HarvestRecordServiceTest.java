package com.infyro.ifc.service;

import com.infyro.ifc.dto.harvest.HarvestRecordRequest;
import com.infyro.ifc.enums.CropPlantingStatus;
import com.infyro.ifc.enums.HarvestUnit;
import com.infyro.ifc.model.CropPlanting;
import com.infyro.ifc.model.HarvestRecord;
import com.infyro.ifc.dto.harvest.HarvestRecordResponse;
import com.infyro.ifc.repository.CropPlantingRepo;
import com.infyro.ifc.repository.HarvestRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HarvestRecordServiceTest {
    @Mock HarvestRecordRepository records;
    @Mock CropPlantingRepo plantings;
    @InjectMocks HarvestRecordService service;

    @Test
    void rejectsPlantingNotOwnedByCaller() {
        when(plantings.findByIdAndField_Farm_Owner_Email(4L, "owner@example.com")).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.create(4L, "owner@example.com", request(LocalDate.now())));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(records, never()).save(any());
    }

    @Test
    void rejectsHarvestBeforePlantingDate() {
        CropPlanting planting = CropPlanting.builder().id(4L)
                .plantedOn(LocalDate.of(2026, 1, 10))
                .expectedHarvestOn(LocalDate.of(2026, 4, 10))
                .status(CropPlantingStatus.ACTIVE).build();
        when(plantings.findByIdAndField_Farm_Owner_Email(4L, "owner@example.com")).thenReturn(Optional.of(planting));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.create(4L, "owner@example.com", request(LocalDate.of(2026, 1, 9))));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(records, never()).save(any());
    }

    @Test
    void rejectsEarlyHarvestWhilePlantingStillPlanned() {
        CropPlanting planting = CropPlanting.builder().id(4L)
                .plantedOn(LocalDate.of(2026, 1, 10))
                .expectedHarvestOn(LocalDate.of(2026, 4, 10))
                .status(CropPlantingStatus.PLANNED).build();
        when(plantings.findByIdAndField_Farm_Owner_Email(4L, "owner@example.com")).thenReturn(Optional.of(planting));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.create(4L, "owner@example.com", request(LocalDate.of(2026, 3, 1))));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(records, never()).save(any());
    }

    @Test
    void allowsValidHarvestForActivePlanting() {
        LocalDate harvestDate = LocalDate.of(2026, 4, 10);
        CropPlanting planting = CropPlanting.builder().id(4L)
                .plantedOn(LocalDate.of(2026, 1, 10))
                .expectedHarvestOn(harvestDate)
                .status(CropPlantingStatus.ACTIVE).build();

        when(plantings.findByIdAndField_Farm_Owner_Email(4L, "owner@example.com"))
                .thenReturn(Optional.of(planting));
        when(records.save(any(HarvestRecord.class))).thenAnswer(invocation -> {
            HarvestRecord record = invocation.getArgument(0);
            record.setId(55L);
            return record;
        });

        HarvestRecordResponse response = service.create(
                4L, "owner@example.com", request(harvestDate));

        assertEquals(55L, response.id());
        assertEquals(4L, response.cropPlantingId());
        verify(records).save(any(HarvestRecord.class));
    }

    @Test
    void rejectsReadingHarvestOwnedByAnotherUser() {
        when(records.findByIdAndCropPlanting_Field_Farm_Owner_Email(
                55L, "attacker@example.com")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.getById(55L, "attacker@example.com"));
    }

    @Test
    void rejectsDeletingHarvestOwnedByAnotherUser() {
        when(records.findByIdAndCropPlanting_Field_Farm_Owner_Email(
                55L, "attacker@example.com")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.delete(55L, "attacker@example.com"));

        verify(records, never()).delete(any(HarvestRecord.class));
    }

    @Test
    void rejectsListingRecordsForUnownedPlanting() {
        when(plantings.findByIdAndField_Farm_Owner_Email(
                4L, "attacker@example.com")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.list(4L, "attacker@example.com"));

        verify(records, never())
                .findAllByCropPlanting_IdAndCropPlanting_Field_Farm_Owner_EmailOrderByHarvestDateDescCreatedAtDesc(
                        anyLong(), anyString());
    }

    private HarvestRecordRequest request(LocalDate date) {
        return new HarvestRecordRequest(date, new BigDecimal("2.500"), HarvestUnit.KILOGRAM, "harvest");
    }
}
