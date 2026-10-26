package com.infyro.ifc.service;

import com.infyro.ifc.dto.harvest.HarvestRecordRequest;
import com.infyro.ifc.enums.HarvestUnit;
import com.infyro.ifc.model.CropPlanting;
import com.infyro.ifc.model.Expense;
import com.infyro.ifc.model.HarvestRecord;
import com.infyro.ifc.repository.CropPlantingRepo;
import com.infyro.ifc.repository.ExpenseRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseHarvestBusinessRuleTest {
    @Mock
    private ExpenseRepository expenses;

    @Mock
    private HarvestRecordRepository harvestRecords;

    @Mock
    private CropPlantingRepo plantings;

    @InjectMocks
    private ExpenseService expenseService;

    @InjectMocks
    private HarvestRecordService harvestRecordService;

    @Test
    void rejectsVoidingAnExpenseMoreThanOnce() {
        Expense alreadyVoided = Expense.builder().id(11L).voided(true).build();
        when(expenses.findByIdAndFarm_Owner_Email(11L, "owner@example.com"))
                .thenReturn(Optional.of(alreadyVoided));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> expenseService.delete(11L, "owner@example.com"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(expenses, never()).save(any(Expense.class));
    }

    @Test
    void rejectsVoidingAHarvestRecordMoreThanOnce() {
        HarvestRecord alreadyVoided = HarvestRecord.builder().id(12L).voided(true).build();
        when(harvestRecords.findByIdAndCropPlanting_Field_Farm_Owner_Email(12L, "owner@example.com"))
                .thenReturn(Optional.of(alreadyVoided));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> harvestRecordService.delete(12L, "owner@example.com"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(harvestRecords, never()).save(any(HarvestRecord.class));
    }

    @Test
    void rejectsFutureHarvestDateBeforeSaving() {
        LocalDate today = LocalDate.now();
        CropPlanting planting = CropPlanting.builder()
                .id(5L)
                .plantedOn(today.minusDays(30))
                .build();
        planting.setStatus(com.infyro.ifc.enums.CropPlantingStatus.ACTIVE);
        when(plantings.findByIdAndField_Farm_Owner_Email(5L, "owner@example.com"))
                .thenReturn(Optional.of(planting));

        HarvestRecordRequest request = new HarvestRecordRequest(
                today.plusDays(1), new BigDecimal("1.000"), HarvestUnit.KILOGRAM, null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> harvestRecordService.create(5L, "owner@example.com", request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(harvestRecords, never()).save(any(HarvestRecord.class));
    }
}
