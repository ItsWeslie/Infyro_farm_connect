package com.infyro.ifc.mapper;

import com.infyro.ifc.dto.expense.ExpenseResponse;
import com.infyro.ifc.model.Expense;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class ExpenseMapper implements Function<Expense, ExpenseResponse> {

    @Override
    public ExpenseResponse apply(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .farmId(expense.getFarm().getId())
                .cropPlantingId(expense.getCropPlanting().getId())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .category(expense.getCategory())
                .description(expense.getDescription())
                .build();
    }

    public ExpenseResponse response(Expense e) {
        return new ExpenseResponse(e.getId(), e.getFarm().getId(),
                e.getCropPlanting() == null ? null : e.getCropPlanting().getId(),
                e.getAmount(), e.getExpenseDate(), e.getCategory(), e.getDescription(),
                e.getCreatedAt(), e.getUpdatedAt(), e.isVoided(),
                e.isVoided() ? "Cancelled" : "Active", e.getVoidedAt(), e.getVoidReason());
    }
}
