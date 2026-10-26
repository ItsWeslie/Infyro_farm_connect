package com.infyro.ifc.controller;

import com.infyro.ifc.dto.expense.ExpenseRequest;
import com.infyro.ifc.dto.farm.ExpenseSummaryResponse;
import com.infyro.ifc.dto.farm.HarvestSummaryResponse;
import com.infyro.ifc.dto.farm.PlantingSummaryResponse;
import com.infyro.ifc.dto.farm.FarmSummaryResponse;
import com.infyro.ifc.dto.harvest.HarvestRecordRequest;
import com.infyro.ifc.exception.GlobalExceptionHandler;
import com.infyro.ifc.service.ExpenseService;
import com.infyro.ifc.service.FarmSummaryService;
import com.infyro.ifc.service.HarvestRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.LocalDate;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FarmWorkflowApiContractTest {
    private final ExpenseService expenseService = mock(ExpenseService.class);
    private final HarvestRecordService harvestService = mock(HarvestRecordService.class);
    private final FarmSummaryService summaryService = mock(FarmSummaryService.class);

    private final MockMvc expenseMvc = MockMvcBuilders.standaloneSetup(new ExpenseController(expenseService))
            .setControllerAdvice(new GlobalExceptionHandler()).build();
    private final MockMvc harvestMvc = MockMvcBuilders.standaloneSetup(new HarvestRecordController(harvestService))
            .setControllerAdvice(new GlobalExceptionHandler()).build();
    private final MockMvc summaryMvc = MockMvcBuilders.standaloneSetup(new FarmSummaryController(summaryService))
            .setControllerAdvice(new GlobalExceptionHandler()).build();

    @Test
    void expenseCreationUsesFarmRouteAndAuthenticatedOwner() throws Exception {
        expenseMvc.perform(post("/api/farms/12/expenses")
                        .principal((Principal) () -> "owner@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cropPlantingId":31,"amount":125.50,"expenseDate":"2026-10-20",
                                 "category":"SEEDS","description":"  Seed purchase  "}
                                """))
                .andExpect(status().isCreated());

        verify(expenseService).create(eq(12L), eq("owner@example.com"), any(ExpenseRequest.class));
        verifyNoMoreInteractions(expenseService);
    }

    @Test
    void harvestCreationUsesPlantingRouteAndAuthenticatedOwner() throws Exception {
        harvestMvc.perform(post("/api/crop-plantings/31/harvest-records")
                        .principal((Principal) () -> "owner@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"harvestDate":"2026-10-20","quantity":12.500,"unit":"KG","notes":"First pick"}
                                """))
                .andExpect(status().isCreated());

        verify(harvestService).create(eq(31L), eq("owner@example.com"), any(HarvestRecordRequest.class));
        verifyNoMoreInteractions(harvestService);
    }

    @Test
    void cancellingExpenseReturnsNoContentAndUsesAuthenticatedOwner() throws Exception {
        when(expenseService.getById(41L, "owner@example.com"))
                .thenReturn(null);
        expenseMvc.perform(delete("/api/farms/12/expenses/41")
                        .principal((Principal) () -> "owner@example.com"))
                .andExpect(status().isNoContent());

        verify(expenseService).getById(41L, "owner@example.com");
        verify(expenseService).delete(41L, "owner@example.com");
        verifyNoMoreInteractions(expenseService);
    }

    @Test
    void cancellingHarvestReturnsNoContentAndUsesAuthenticatedOwner() throws Exception {
        when(harvestService.getById(51L, "owner@example.com"))
                .thenReturn(null);
        harvestMvc.perform(delete("/api/crop-plantings/31/harvest-records/51")
                        .principal((Principal) () -> "owner@example.com"))
                .andExpect(status().isNoContent());

        verify(harvestService).getById(51L, "owner@example.com");
        verify(harvestService).delete(51L, "owner@example.com");
        verifyNoMoreInteractions(harvestService);
    }

    @Test
    void expenseSummaryForwardsInclusiveDateRangeAndOwner() throws Exception {
        summaryMvc.perform(get("/api/farms/12/expenses/summary")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31")
                        .principal((Principal) () -> "owner@example.com"))
                .andExpect(status().isOk());

        verify(summaryService).getExpenseSummary(12L, "owner@example.com",
                LocalDate.parse("2026-10-01"), LocalDate.parse("2026-10-31"));
        verifyNoMoreInteractions(summaryService);
    }

    @Test
    void invalidDateRangeReturnsStructuredBadRequest() throws Exception {
        when(summaryService.getExpenseSummary(12L, "owner@example.com",
                LocalDate.parse("2026-10-31"), LocalDate.parse("2026-10-01")))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "'from' date must be on or before 'to' date"));

        summaryMvc.perform(get("/api/farms/12/expenses/summary")
                        .param("from", "2026-10-31")
                        .param("to", "2026-10-01")
                        .principal((Principal) () -> "owner@example.com"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

        verify(summaryService).getExpenseSummary(12L, "owner@example.com",
                LocalDate.parse("2026-10-31"), LocalDate.parse("2026-10-01"));
        verifyNoMoreInteractions(summaryService);
    }
}
