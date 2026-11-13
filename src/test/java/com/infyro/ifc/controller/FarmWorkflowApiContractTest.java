package com.infyro.ifc.controller;

import com.infyro.ifc.controller.expense.ExpenseController;
import com.infyro.ifc.controller.farm.FarmSummaryController;
import com.infyro.ifc.controller.harvest.HarvestRecordController;
import com.infyro.ifc.dto.expense.ExpenseRequest;
import com.infyro.ifc.dto.expense.ExpenseResponse;
import com.infyro.ifc.dto.harvest.HarvestRecordRequest;
import com.infyro.ifc.dto.harvest.HarvestRecordResponse;
import com.infyro.ifc.exception.GlobalExceptionHandler;
import com.infyro.ifc.service.expense.ExpenseService;
import com.infyro.ifc.service.farm.FarmSummaryService;
import com.infyro.ifc.service.HarvestRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.LocalDate;

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
        when(expenseService.createExpense(eq(12L), eq("owner@example.com"), any(ExpenseRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(mock(ExpenseResponse.class)));
        expenseMvc.perform(post("/api/farms/12/expenses")
                        .principal((Principal) () -> "owner@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cropPlantingId":31,"amount":125.50,"expenseDate":"2026-10-20",
                                 "category":"SEEDS_AND_SEEDLINGS","description":"  Seed purchase  "}
                                """))
                .andExpect(status().isCreated());

        verify(expenseService).createExpense(eq(12L), eq("owner@example.com"), any(ExpenseRequest.class));
        verifyNoMoreInteractions(expenseService);
    }

    @Test
    void harvestCreationUsesPlantingRouteAndAuthenticatedOwner() throws Exception {
        when(harvestService.create(eq(31L), eq("owner@example.com"), any(HarvestRecordRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(mock(HarvestRecordResponse.class)));
        harvestMvc.perform(post("/api/crop-plantings/31/harvest-records")
                        .principal((Principal) () -> "owner@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"harvestDate":"2026-10-20","quantity":12.500,"unit":"KILOGRAM","notes":"First pick"}
                                """))
                .andExpect(status().isCreated());

        verify(harvestService).create(eq(31L), eq("owner@example.com"), any(HarvestRecordRequest.class));
        verifyNoMoreInteractions(harvestService);
    }

    @Test
    void cancellingExpenseReturnsNoContentAndUsesAuthenticatedOwner() throws Exception {
        expenseMvc.perform(delete("/api/farms/12/expenses/41")
                        .principal((Principal) () -> "owner@example.com"))
                .andExpect(status().isOk());

        verify(expenseService).deleteExpenseByIdAndFarmId(41L, 12L, "owner@example.com");
        verifyNoMoreInteractions(expenseService);
    }

    @Test
    void cancellingHarvestReturnsNoContentAndUsesAuthenticatedOwner() throws Exception {
        harvestMvc.perform(delete("/api/crop-plantings/31/harvest-records/51")
                        .principal((Principal) () -> "owner@example.com"))
                .andExpect(status().isNoContent());

        verify(harvestService).deleteByPlantingIdAndHarvestRecordId(51L, 31L, "owner@example.com");
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
