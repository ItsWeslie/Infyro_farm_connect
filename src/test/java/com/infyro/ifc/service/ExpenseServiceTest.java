package com.infyro.ifc.service;

import com.infyro.ifc.dto.expense.ExpenseRequest;
import com.infyro.ifc.dto.expense.ExpenseResponse;
import com.infyro.ifc.enums.ExpenseCategory;
import com.infyro.ifc.model.*;
import com.infyro.ifc.repository.*;
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
class ExpenseServiceTest {
    @Mock ExpenseRepository expenses;
    @Mock FarmRepo farms;
    @Mock CropPlantingRepo plantings;
    @InjectMocks ExpenseService service;

    @Test
    void rejectsFarmNotOwnedByCaller() {
        when(farms.findByIdAndOwner_Email(10L, "owner@example.com")).thenReturn(Optional.empty());
        ExpenseRequest request = new ExpenseRequest(null, new BigDecimal("10.00"), LocalDate.now(),
                ExpenseCategory.OTHER, "supplies");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.create(10L, "owner@example.com", request));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(expenses, never()).save(any());
    }

    @Test
    void rejectsPlantingFromAnotherFarm() {
        Farm requestedFarm = Farm.builder().id(10L).build();
        Farm otherFarm = Farm.builder().id(20L).build();
        Field field = Field.builder().farm(otherFarm).build();
        CropPlanting planting = CropPlanting.builder().id(3L).field(field).build();
        when(farms.findByIdAndOwner_Email(10L, "owner@example.com")).thenReturn(Optional.of(requestedFarm));
        when(plantings.findByIdAndField_Farm_Owner_Email(3L, "owner@example.com")).thenReturn(Optional.of(planting));
        ExpenseRequest request = new ExpenseRequest(3L, new BigDecimal("10.00"), LocalDate.now(),
                ExpenseCategory.OTHER, "supplies");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.create(10L, "owner@example.com", request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(expenses, never()).save(any());
    }

    @Test
    void rejectsReadingExpenseOwnedByAnotherUser() {
        when(expenses.findByIdAndFarm_Owner_Email(99L, "attacker@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.getById(99L, "attacker@example.com"));
    }

    @Test
    void rejectsDeletingExpenseOwnedByAnotherUser() {
        when(expenses.findByIdAndFarm_Owner_Email(99L, "attacker@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.delete(99L, "attacker@example.com"));

        verify(expenses, never()).delete(any(Expense.class));
    }

    @Test
    void rejectsListingExpensesForUnownedFarm() {
        when(farms.findByIdAndOwner_Email(10L, "attacker@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.listByFarm(10L, "attacker@example.com"));

        verify(expenses, never())
                .findAllByFarm_IdAndFarm_Owner_EmailOrderByExpenseDateDescCreatedAtDesc(
                        anyLong(), anyString());
    }

    @Test
    void createsFarmExpenseAndTrimsDescription() {
        Farm farm = Farm.builder().id(10L).build();
        when(farms.findByIdAndOwner_Email(10L, "owner@example.com")).thenReturn(Optional.of(farm));
        when(expenses.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense e = invocation.getArgument(0);
            e.setId(99L);
            return e;
        });
        ExpenseResponse response = service.create(10L, "owner@example.com",
                new ExpenseRequest(null, new BigDecimal("12.50"), LocalDate.now(), ExpenseCategory.OTHER, "  tools  "));
        assertEquals(99L, response.id());
        assertEquals(10L, response.farmId());
        assertEquals("tools", response.description());
        assertNull(response.cropPlantingId());
    }
}
