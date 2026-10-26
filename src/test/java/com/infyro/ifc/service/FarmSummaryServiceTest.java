package com.infyro.ifc.service;

import com.infyro.ifc.enums.CropPlantingStatus;
import com.infyro.ifc.enums.ExpenseCategory;
import com.infyro.ifc.enums.HarvestUnit;
import com.infyro.ifc.exception.FarmNotFoundException;
import com.infyro.ifc.model.*;
import com.infyro.ifc.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmSummaryServiceTest {
    @Mock FarmRepo farmRepo;
    @Mock FieldRepo fieldRepo;
    @Mock CropPlantingRepo cropPlantingRepo;
    @Mock ExpenseRepository expenseRepository;
    @Mock HarvestRecordRepository harvestRecordRepository;
    @InjectMocks FarmSummaryService service;

    @Test
    void farmSummaryExcludesVoidedRecordsAndGroupsHarvestsByUnit() {
        Farm farm = Farm.builder().id(5L).farmName("North Farm").build();
        CropPlanting planned = CropPlanting.builder().id(10L).status(CropPlantingStatus.PLANNED).build();
        CropPlanting active = CropPlanting.builder().id(11L).status(CropPlantingStatus.ACTIVE).build();
        Expense validExpense = Expense.builder().amount(new BigDecimal("120.50"))
                .category(ExpenseCategory.LABOUR).voided(false).build();
        Expense voidedExpense = Expense.builder().amount(new BigDecimal("900.00"))
                .category(ExpenseCategory.SEEDS_AND_SEEDLINGS).voided(true).build();
        HarvestRecord kilo = HarvestRecord.builder().quantity(new BigDecimal("20.000"))
                .unit(HarvestUnit.KILOGRAM).voided(false).build();
        HarvestRecord tonne = HarvestRecord.builder().quantity(new BigDecimal("2.000"))
                .unit(HarvestUnit.TONNE).voided(false).build();
        HarvestRecord voidedHarvest = HarvestRecord.builder().quantity(new BigDecimal("999.000"))
                .unit(HarvestUnit.KILOGRAM).voided(true).build();

        when(farmRepo.findByIdAndOwner_Email(5L, "owner@example.com")).thenReturn(Optional.of(farm));
        when(fieldRepo.findAllByFarm_IdAndFarm_Owner_Email(5L, "owner@example.com")).thenReturn(List.of(new Field()));
        when(cropPlantingRepo.findAllByField_Farm_IdAndField_Farm_Owner_Email(5L, "owner@example.com"))
                .thenReturn(List.of(planned, active));
        when(expenseRepository.findAllByFarm_IdAndFarm_Owner_EmailOrderByExpenseDateDescCreatedAtDesc(5L, "owner@example.com"))
                .thenReturn(List.of(validExpense, voidedExpense));
        when(harvestRecordRepository.findAllByCropPlanting_Field_Farm_IdAndCropPlanting_Field_Farm_Owner_EmailOrderByHarvestDateDescCreatedAtDesc(5L, "owner@example.com"))
                .thenReturn(List.of(kilo, tonne, voidedHarvest));

        var result = service.getFarmSummary(5L, "owner@example.com");

        assertEquals(1, result.totalFields());
        assertEquals(2, result.totalPlantings());
        assertEquals(1L, result.plantingsByStatus().get(CropPlantingStatus.PLANNED));
        assertEquals(1L, result.plantingsByStatus().get(CropPlantingStatus.ACTIVE));
        assertEquals(new BigDecimal("120.50"), result.totalExpenses());
        assertEquals(new BigDecimal("120.50"), result.expensesByCategory().get(ExpenseCategory.LABOUR));
        assertEquals(new BigDecimal("20.000"), result.harvestQuantityByUnit().get(HarvestUnit.KILOGRAM));
        assertEquals(new BigDecimal("2.000"), result.harvestQuantityByUnit().get(HarvestUnit.TONNE));
        assertEquals(2, result.harvestRecordCount());
    }

    @Test
    void summaryRejectsDateRangeWhereFromIsAfterTo() {
        when(farmRepo.findByIdAndOwner_Email(5L, "owner@example.com"))
                .thenReturn(Optional.of(Farm.builder().id(5L).farmName("North Farm").build()));

        var error = assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.getExpenseSummary(5L, "owner@example.com",
                        LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 1)));

        assertEquals(400, error.getStatusCode().value());
        verifyNoInteractions(expenseRepository);
    }

    @Test
    void summaryDoesNotExposeAnotherOwnersFarm() {
        when(farmRepo.findByIdAndOwner_Email(5L, "intruder@example.com")).thenReturn(Optional.empty());

        assertThrows(FarmNotFoundException.class,
                () -> service.getPlantingSummary(5L, "intruder@example.com"));
        verifyNoInteractions(cropPlantingRepo);
    }
}
