package com.infyro.ifc.controller;

import com.infyro.ifc.dto.farm.ExpenseSummaryResponse;
import com.infyro.ifc.dto.farm.FarmSummaryResponse;
import com.infyro.ifc.dto.farm.HarvestSummaryResponse;
import com.infyro.ifc.dto.farm.PlantingSummaryResponse;
import com.infyro.ifc.enums.CropPlantingStatus;
import com.infyro.ifc.enums.ExpenseCategory;
import com.infyro.ifc.enums.HarvestUnit;
import com.infyro.ifc.service.FarmSummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class FarmSummaryControllerTest {

    private FarmSummaryService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(FarmSummaryService.class);
        mockMvc = standaloneSetup(new FarmSummaryController(service)).build();
    }

    @Test
    void farmSummaryEndpointReturnsSummaryContract() throws Exception {
        when(service.getFarmSummary(7L, "owner@example.com")).thenReturn(
                new FarmSummaryResponse(7L, "North Farm", 3, 4,
                        Map.of(CropPlantingStatus.ACTIVE, 2L, CropPlantingStatus.PLANNED, 2L),
                        new BigDecimal("120.50"),
                        Map.of(ExpenseCategory.LABOUR, new BigDecimal("120.50")),
                        Map.of(HarvestUnit.KILOGRAM, new BigDecimal("20.000")), 1));

        mockMvc.perform(get("/api/farms/7/summary").principal(() -> "owner@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.farmId").value(7))
                .andExpect(jsonPath("$.farmName").value("North Farm"))
                .andExpect(jsonPath("$.totalFields").value(3))
                .andExpect(jsonPath("$.totalPlantings").value(4))
                .andExpect(jsonPath("$.totalExpenses").value(120.50))
                .andExpect(jsonPath("$.harvestRecordCount").value(1))
                .andExpect(jsonPath("$.plantingsByStatus.ACTIVE").value(2))
                .andExpect(jsonPath("$.expensesByCategory.LABOUR").value(120.50))
                .andExpect(jsonPath("$.harvestQuantityByUnit.KILOGRAM").value(20.000));

        verify(service).getFarmSummary(7L, "owner@example.com");
    }

    @Test
    void expenseSummaryParsesInclusiveDateParametersAndReturnsContract() throws Exception {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);
        when(service.getExpenseSummary(7L, "owner@example.com", from, to)).thenReturn(
                new ExpenseSummaryResponse(7L, from, to, 2, new BigDecimal("75.00"),
                        Map.of(ExpenseCategory.LABOUR, new BigDecimal("75.00"))));

        mockMvc.perform(get("/api/farms/7/expenses/summary")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31")
                        .principal(() -> "owner@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.farmId").value(7))
                .andExpect(jsonPath("$.from").value("2026-10-01"))
                .andExpect(jsonPath("$.to").value("2026-10-31"))
                .andExpect(jsonPath("$.recordCount").value(2))
                .andExpect(jsonPath("$.totalAmount").value(75.00))
                .andExpect(jsonPath("$.byCategory.LABOUR").value(75.00));

        verify(service).getExpenseSummary(7L, "owner@example.com", from, to);
    }

    @Test
    void harvestSummaryPassesDateFiltersAndKeepsQuantitiesGroupedByUnit() throws Exception {
        LocalDate from = LocalDate.of(2026, 9, 1);
        LocalDate to = LocalDate.of(2026, 9, 30);
        when(service.getHarvestSummary(7L, "owner@example.com", from, to)).thenReturn(
                new HarvestSummaryResponse(7L, from, to, 2,
                        Map.of(HarvestUnit.KILOGRAM, new BigDecimal("20.000"),
                                HarvestUnit.TONNE, new BigDecimal("1.500"))));

        mockMvc.perform(get("/api/farms/7/harvests/summary")
                        .param("from", "2026-09-01")
                        .param("to", "2026-09-30")
                        .principal(() -> "owner@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordCount").value(2))
                .andExpect(jsonPath("$.quantityByUnit.KILOGRAM").value(20.000))
                .andExpect(jsonPath("$.quantityByUnit.TONNE").value(1.500));

        verify(service).getHarvestSummary(7L, "owner@example.com", from, to);
    }

    @Test
    void plantingSummaryEndpointReturnsStatusCounts() throws Exception {
        when(service.getPlantingSummary(7L, "owner@example.com")).thenReturn(
                new PlantingSummaryResponse(7L, 3,
                        Map.of(CropPlantingStatus.ACTIVE, 2L, CropPlantingStatus.COMPLETED, 1L)));

        mockMvc.perform(get("/api/farms/7/plantings/summary").principal(() -> "owner@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.farmId").value(7))
                .andExpect(jsonPath("$.totalPlantings").value(3))
                .andExpect(jsonPath("$.countsByStatus.ACTIVE").value(2))
                .andExpect(jsonPath("$.countsByStatus.COMPLETED").value(1));

        verify(service).getPlantingSummary(7L, "owner@example.com");
    }

    @Test
    void expenseSummaryRejectsMalformedDateParameter() throws Exception {
        mockMvc.perform(get("/api/farms/7/expenses/summary")
                        .param("from", "not-a-date")
                        .principal(() -> "owner@example.com"))
                .andExpect(status().isBadRequest());
    }
}
