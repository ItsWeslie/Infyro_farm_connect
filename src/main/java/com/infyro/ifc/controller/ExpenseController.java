package com.infyro.ifc.controller;

import com.infyro.ifc.dto.expense.*;
import com.infyro.ifc.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/farms/{farmId}/expenses")
public class ExpenseController {
    private final ExpenseService service;

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@PathVariable Long farmId,
            @Valid @RequestBody ExpenseRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(farmId, principal.getName(), request));
    }
    @GetMapping
    public List<ExpenseResponse> list(@PathVariable Long farmId, Principal principal) {
        return service.listByFarm(farmId, principal.getName());
    }
    @GetMapping("/{id}")
    public ExpenseResponse get(@PathVariable Long farmId, @PathVariable Long id, Principal principal) {
        ExpenseResponse expense = service.getById(id, principal.getName());
        if (!expense.farmId().equals(farmId)) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found");
        return expense;
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long farmId, @PathVariable Long id, Principal principal) {
        ExpenseResponse expense = service.getById(id, principal.getName());
        if (!expense.farmId().equals(farmId)) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found");
        service.delete(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
