package com.infyro.ifc.service;

import com.infyro.ifc.dto.expense.ExpenseRequest;
import com.infyro.ifc.enums.ExpenseCategory;
import com.infyro.ifc.enums.HarvestUnit;
import com.infyro.ifc.model.CropPlanting;
import com.infyro.ifc.model.Expense;
import com.infyro.ifc.model.Farm;
import com.infyro.ifc.model.Field;
import com.infyro.ifc.model.HarvestRecord;
import com.infyro.ifc.repository.CropPlantingRepo;
import com.infyro.ifc.repository.ExpenseRepository;
import com.infyro.ifc.repository.FarmRepo;
import com.infyro.ifc.repository.HarvestRecordRepository;
import com.infyro.ifc.repository.UserRepo;
import com.infyro.ifc.mapper.FarmMapper;
import com.infyro.ifc.dto.farm.FarmResponse;
import com.infyro.ifc.dto.APIResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseHarvestEdgeCaseTest {
    private static final String OWNER = "owner@example.com";

    @Mock private ExpenseRepository expenses;
    @Mock private FarmRepo farms;
    @Mock private CropPlantingRepo plantings;
    @Mock private HarvestRecordRepository harvestRecords;
    @Mock private UserRepo users;
    @Mock private FarmMapper farmMapper;

    @InjectMocks private ExpenseService expenseService;
    @InjectMocks private HarvestRecordService harvestService;
    @InjectMocks private FarmService farmService;

    @Test
    void rejectsExpenseCreationWhenFarmIsNotOwnedByCaller() {
        when(farms.findByIdAndOwner_Email(1L, OWNER)).thenReturn(Optional.empty());
        ExpenseRequest request = new ExpenseRequest(null, new BigDecimal("10.00"),
                LocalDate.now(), ExpenseCategory.FERTILIZER, "Fertilizer");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> expenseService.create(1L, OWNER, request));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(expenses, never()).save(any(Expense.class));
    }

    @Test
    void rejectsExpensePlantingFromDifferentFarm() {
        Farm requestedFarm = Farm.builder().id(1L).build();
        Farm otherFarm = Farm.builder().id(2L).build();
        Field otherField = Field.builder().id(20L).farm(otherFarm).build();
        CropPlanting otherPlanting = CropPlanting.builder().id(30L).field(otherField).build();
        when(farms.findByIdAndOwner_Email(1L, OWNER)).thenReturn(Optional.of(requestedFarm));
        when(plantings.findByIdAndField_Farm_Owner_Email(30L, OWNER)).thenReturn(Optional.of(otherPlanting));

        ExpenseRequest request = new ExpenseRequest(30L, new BigDecimal("10.00"),
                LocalDate.now(), ExpenseCategory.FERTILIZER, "Fertilizer");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> expenseService.create(1L, OWNER, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(expenses, never()).save(any(Expense.class));
    }

    @Test
    void expenseListRetainsCancelledRowsForHistory() {
        Farm farm = Farm.builder().id(1L).build();
        Expense active = Expense.builder().id(1L).farm(farm).amount(new BigDecimal("10.00"))
                .expenseDate(LocalDate.now()).category(ExpenseCategory.FERTILIZER)
                .description("Active").voided(false).build();
        Expense cancelled = Expense.builder().id(2L).farm(farm).amount(new BigDecimal("20.00"))
                .expenseDate(LocalDate.now().minusDays(1)).category(ExpenseCategory.SEEDS_AND_SEEDLINGS)
                .description("Cancelled").voided(true).build();
        when(farms.findByIdAndOwner_Email(1L, OWNER)).thenReturn(Optional.of(farm));
        when(expenses.findAllByFarm_IdAndFarm_Owner_EmailOrderByExpenseDateDescCreatedAtDesc(1L, OWNER))
                .thenReturn(List.of(active, cancelled));

        var result = expenseService.listByFarm(1L, OWNER);

        assertEquals(2, result.size());
        assertFalse(result.get(0).voided());
        assertTrue(result.get(1).voided());
    }

    @Test
    void harvestListRetainsCancelledRowsForHistory() {
        Farm farm = Farm.builder().id(1L).build();
        Field field = Field.builder().id(2L).farm(farm).build();
        CropPlanting planting = CropPlanting.builder().id(3L).field(field).build();
        HarvestRecord active = HarvestRecord.builder().id(1L).cropPlanting(planting)
                .harvestDate(LocalDate.now()).quantity(new BigDecimal("5.000"))
                .unit(HarvestUnit.KILOGRAM).voided(false).build();
        HarvestRecord cancelled = HarvestRecord.builder().id(2L).cropPlanting(planting)
                .harvestDate(LocalDate.now().minusDays(1)).quantity(new BigDecimal("2.000"))
                .unit(HarvestUnit.KILOGRAM).voided(true).build();
        when(plantings.findByIdAndField_Farm_Owner_Email(3L, OWNER)).thenReturn(Optional.of(planting));
        when(harvestRecords.findAllByCropPlanting_IdAndCropPlanting_Field_Farm_Owner_EmailOrderByHarvestDateDescCreatedAtDesc(3L, OWNER))
                .thenReturn(List.of(active, cancelled));

        var result = harvestService.list(3L, OWNER);

        assertEquals(2, result.size());
        assertFalse(result.get(0).voided());
        assertTrue(result.get(1).voided());
    }

    @Test
    void farmDeletionIsBlockedWhenExpenseHistoryExists() {
        when(farms.existsByIdAndOwner_Email(1L, OWNER)).thenReturn(true);
        when(expenses.existsByFarm_Id(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> farmService.deleteFarm(1L, OWNER));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(farms, never()).deleteByIdAndOwner_Email(1L, OWNER);
    }

    @Test
    void farmDeletionIsBlockedWhenHarvestHistoryExists() {
        when(farms.existsByIdAndOwner_Email(1L, OWNER)).thenReturn(true);
        when(expenses.existsByFarm_Id(1L)).thenReturn(false);
        when(harvestRecords.existsByCropPlanting_Field_Farm_Id(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> farmService.deleteFarm(1L, OWNER));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(farms, never()).deleteByIdAndOwner_Email(1L, OWNER);
    }
}
