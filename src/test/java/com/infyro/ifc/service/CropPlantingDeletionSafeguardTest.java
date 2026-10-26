package com.infyro.ifc.service;

import com.infyro.ifc.enums.CropPlantingStatus;
import com.infyro.ifc.model.CropPlanting;
import com.infyro.ifc.repository.CropActivityLogRepository;
import com.infyro.ifc.repository.CropPlantingRepo;
import com.infyro.ifc.repository.CropTypeRepository;
import com.infyro.ifc.repository.ExpenseRepository;
import com.infyro.ifc.repository.FieldRepo;
import com.infyro.ifc.repository.HarvestRecordRepository;
import com.infyro.ifc.mapper.CropPlantingMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CropPlantingDeletionSafeguardTest {
    private static final Long PLANTING_ID = 42L;
    private static final String OWNER = "owner@example.com";

    @Mock private CropPlantingRepo plantingRepository;
    @Mock private CropTypeRepository cropTypeRepository;
    @Mock private FieldRepo fieldRepo;
    @Mock private CropPlantingMapper cropPlantingMapper;
    @Mock private ExpenseRepository expenseRepository;
    @Mock private HarvestRecordRepository harvestRecordRepository;
    @Mock private CropActivityLogRepository activityLogRepository;

    @InjectMocks private CropPlantingService service;

    @Test
    void blocksDeletionWhenExpenseHistoryExists() {
        givenPlannedPlanting();
        when(expenseRepository.existsByCropPlanting_Id(PLANTING_ID)).thenReturn(true);

        assertConflictAndNoDelete();

        verify(harvestRecordRepository, never()).existsByCropPlanting_Id(PLANTING_ID);
        verify(activityLogRepository, never()).existsByCropPlanting_Id(PLANTING_ID);
    }

    @Test
    void blocksDeletionWhenHarvestHistoryExists() {
        givenPlannedPlanting();
        when(expenseRepository.existsByCropPlanting_Id(PLANTING_ID)).thenReturn(false);
        when(harvestRecordRepository.existsByCropPlanting_Id(PLANTING_ID)).thenReturn(true);

        assertConflictAndNoDelete();

        verify(activityLogRepository, never()).existsByCropPlanting_Id(PLANTING_ID);
    }

    @Test
    void blocksDeletionWhenActivityHistoryExists() {
        givenPlannedPlanting();
        when(expenseRepository.existsByCropPlanting_Id(PLANTING_ID)).thenReturn(false);
        when(harvestRecordRepository.existsByCropPlanting_Id(PLANTING_ID)).thenReturn(false);
        when(activityLogRepository.existsByCropPlanting_Id(PLANTING_ID)).thenReturn(true);

        assertConflictAndNoDelete();
    }

    @Test
    void blocksDeletionWhenPlantingIsNotPlannedEvenWithoutHistory() {
        CropPlanting active = CropPlanting.builder()
                .id(PLANTING_ID)
                .status(CropPlantingStatus.ACTIVE)
                .build();
        when(plantingRepository.findOwnedPlantingForUpdate(PLANTING_ID, OWNER))
                .thenReturn(Optional.of(active));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.delete(PLANTING_ID, OWNER));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(expenseRepository, never()).existsByCropPlanting_Id(PLANTING_ID);
        verify(harvestRecordRepository, never()).existsByCropPlanting_Id(PLANTING_ID);
        verify(activityLogRepository, never()).existsByCropPlanting_Id(PLANTING_ID);
        verify(plantingRepository, never()).delete(active);
        verify(fieldRepo, never()).findOwnedFieldForUpdate(org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString());
    }

    private void givenPlannedPlanting() {
        CropPlanting planned = CropPlanting.builder()
                .id(PLANTING_ID)
                .status(CropPlantingStatus.PLANNED)
                .build();
        when(plantingRepository.findOwnedPlantingForUpdate(PLANTING_ID, OWNER))
                .thenReturn(Optional.of(planned));
    }

    private void assertConflictAndNoDelete() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.delete(PLANTING_ID, OWNER));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(plantingRepository, never()).delete(org.mockito.ArgumentMatchers.any(CropPlanting.class));
        verify(fieldRepo, never()).findOwnedFieldForUpdate(org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString());
    }
}
