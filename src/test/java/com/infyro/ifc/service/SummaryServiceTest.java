package com.infyro.ifc.service;

import com.infyro.ifc.enums.CropPlantingStatus;
import com.infyro.ifc.enums.ExpenseCategory;
import com.infyro.ifc.enums.HarvestUnit;
import com.infyro.ifc.exception.FarmNotFoundException;
import com.infyro.ifc.model.*;
import com.infyro.ifc.repository.*;
import com.infyro.ifc.service.farm.FarmSummaryService;
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
class SummaryServiceTest {
    @Mock FarmRepo farmRepo;
    @Mock FieldRepo fieldRepo;
    @Mock CropPlantingRepo cropPlantingRepo;
    @Mock
    ExpenseRepo expenseRepo;
    @Mock
    HarvestRecordRepo harvestRecordRepo;
    @InjectMocks
    FarmSummaryService service;

    @Test
    void farmSummaryUsesAggregateQueriesAndGroupsHarvestsByUnit() {
        Farm farm = Farm.builder().id(5L).farmName("North Farm").build();

        when(farmRepo.findByIdAndOwner_Email(5L, "owner@example.com")).thenReturn(Optional.of(farm));
        when(fieldRepo.countByFarm(5L, "owner@example.com")).thenReturn(1L);
        CropPlantingRepo.StatusCount plannedCount = mock(CropPlantingRepo.StatusCount.class);
        when(plannedCount.getStatus()).thenReturn(CropPlantingStatus.PLANNED);
        when(plannedCount.getTotal()).thenReturn(1L);
        CropPlantingRepo.StatusCount activeCount = mock(CropPlantingRepo.StatusCount.class);
        when(activeCount.getStatus()).thenReturn(CropPlantingStatus.ACTIVE);
        when(activeCount.getTotal()).thenReturn(1L);
        when(cropPlantingRepo.countByFarmGroupedByStatus(5L, "owner@example.com"))
                .thenReturn(List.of(plannedCount, activeCount));
        ExpenseRepo.CategoryTotal labourTotal = mock(ExpenseRepo.CategoryTotal.class);
        when(labourTotal.getCategory()).thenReturn(ExpenseCategory.LABOUR);
        when(labourTotal.getTotal()).thenReturn(new BigDecimal("120.50"));
        when(labourTotal.getRecordCount()).thenReturn(1L);
        when(expenseRepo.summarizeByFarm(5L, "owner@example.com", null, null))
                .thenReturn(List.of(labourTotal));
        HarvestRecordRepo.UnitTotal kiloTotal = mock(HarvestRecordRepo.UnitTotal.class);
        when(kiloTotal.getUnit()).thenReturn(HarvestUnit.KILOGRAM);
        when(kiloTotal.getTotal()).thenReturn(new BigDecimal("20.000"));
        when(kiloTotal.getRecordCount()).thenReturn(1L);
        HarvestRecordRepo.UnitTotal tonneTotal = mock(HarvestRecordRepo.UnitTotal.class);
        when(tonneTotal.getUnit()).thenReturn(HarvestUnit.TONNE);
        when(tonneTotal.getTotal()).thenReturn(new BigDecimal("2.000"));
        when(tonneTotal.getRecordCount()).thenReturn(1L);
        when(harvestRecordRepo.summarizeByFarm(5L, "owner@example.com", null, null))
                .thenReturn(List.of(kiloTotal, tonneTotal));

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
        verifyNoInteractions(expenseRepo);
    }

    @Test
    void summaryDoesNotExposeAnotherOwnersFarm() {
        when(farmRepo.findByIdAndOwner_Email(5L, "intruder@example.com")).thenReturn(Optional.empty());

        assertThrows(FarmNotFoundException.class,
                () -> service.getPlantingSummary(5L, "intruder@example.com"));
        verifyNoInteractions(cropPlantingRepo);
    }
}
