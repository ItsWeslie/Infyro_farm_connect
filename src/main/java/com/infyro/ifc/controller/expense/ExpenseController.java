package com.infyro.ifc.controller.expense;

import com.infyro.ifc.api.APIConstants;
import com.infyro.ifc.dto.api.ApiResponse;
import com.infyro.ifc.dto.expense.*;
import com.infyro.ifc.service.expense.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(APIConstants.EXPENSE)
public class ExpenseController {

    private final ExpenseService service;

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(@PathVariable Long farmId,
                                                  @Valid @RequestBody ExpenseRequest request,
                                                  Principal principal) {
        return service.createExpense(farmId, principal.getName(), request);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAllExpensesByFarm(@PathVariable Long farmId,
                                                      Principal principal) {
        return service.getAllExpensesByFarm(farmId, principal.getName());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseByExpenseIdAndFarmId(@PathVariable Long farmId,
                                                          @PathVariable Long id,
                                                          Principal principal) {
        return service.getExpenseByExpenseIdAndFarmId(id, farmId,principal.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteExpenseByIdAndFarmId(@PathVariable Long farmId,
                                                                  @PathVariable Long id,
                                                                  Principal principal) {

        return service.deleteExpenseByIdAndFarmId(id, farmId,principal.getName());

    }
}
