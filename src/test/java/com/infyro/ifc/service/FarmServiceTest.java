package com.infyro.ifc.service;

import com.infyro.ifc.mapper.FarmMapper;
import com.infyro.ifc.repository.ExpenseRepository;
import com.infyro.ifc.repository.FarmRepo;
import com.infyro.ifc.repository.HarvestRecordRepository;
import com.infyro.ifc.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmServiceTest {
    @Mock FarmRepo farmRepo;
    @Mock UserRepo userRepo;
    @Mock FarmMapper farmMapper;
    @Mock ExpenseRepository expenseRepository;
    @Mock HarvestRecordRepository harvestRecordRepository;
    @InjectMocks FarmService service;

    @Test
    void refusesDeletingFarmWithExpenseHistory() {
        when(farmRepo.existsByIdAndOwner_Email(10L, "owner@example.com")).thenReturn(true);
        when(expenseRepository.existsByFarm_Id(10L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.deleteFarm(10L, "owner@example.com"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(farmRepo, never()).deleteByIdAndOwner_Email(anyLong(), anyString());
    }

    @Test
    void refusesDeletingFarmWithHarvestHistory() {
        when(farmRepo.existsByIdAndOwner_Email(10L, "owner@example.com")).thenReturn(true);
        when(expenseRepository.existsByFarm_Id(10L)).thenReturn(false);
        when(harvestRecordRepository.existsByCropPlanting_Field_Farm_Id(10L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.deleteFarm(10L, "owner@example.com"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(farmRepo, never()).deleteByIdAndOwner_Email(anyLong(), anyString());
    }
}
