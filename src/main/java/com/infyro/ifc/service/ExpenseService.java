package com.infyro.ifc.service;

import com.infyro.ifc.dto.expense.*;
import com.infyro.ifc.model.*;
import com.infyro.ifc.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenses;
    private final FarmRepo farms;
    private final CropPlantingRepo plantings;

    @Transactional
    public ExpenseResponse create(Long farmId, String email, ExpenseRequest request) {
        Farm farm = farms.findByIdAndOwner_Email(farmId, email)
                .orElseThrow(() -> notFound("Farm not found"));
        CropPlanting planting = null;
        if (request.cropPlantingId() != null) {
            planting = plantings.findByIdAndField_Farm_Owner_Email(request.cropPlantingId(), email)
                    .orElseThrow(() -> notFound("Crop planting not found"));
            if (!planting.getField().getFarm().getId().equals(farm.getId()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Planting does not belong to the specified farm");
        }
        Expense expense = Expense.builder().farm(farm).cropPlanting(planting)
                .amount(request.amount()).expenseDate(request.expenseDate())
                .category(request.category()).description(request.description().trim()).build();
        return response(expenses.save(expense));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> listByFarm(Long farmId, String email) {
        if (farms.findByIdAndOwner_Email(farmId, email).isEmpty()) throw notFound("Farm not found");
        return expenses.findAllByFarm_IdAndFarm_Owner_EmailOrderByExpenseDateDescCreatedAtDesc(farmId, email)
                .stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getById(Long id, String email) {
        return response(expenses.findByIdAndFarm_Owner_Email(id, email)
                .orElseThrow(() -> notFound("Expense not found")));
    }

    @Transactional
    public void delete(Long id, String email) {
        Expense expense = expenses.findByIdAndFarm_Owner_Email(id, email)
                .orElseThrow(() -> notFound("Expense not found"));
        expenses.delete(expense);
    }

    private ExpenseResponse response(Expense e) {
        return new ExpenseResponse(e.getId(), e.getFarm().getId(),
                e.getCropPlanting() == null ? null : e.getCropPlanting().getId(),
                e.getAmount(), e.getExpenseDate(), e.getCategory(), e.getDescription(),
                e.getCreatedAt(), e.getUpdatedAt());
    }
    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
