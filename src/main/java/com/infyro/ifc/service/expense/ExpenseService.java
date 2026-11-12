package com.infyro.ifc.service.expense;

import com.infyro.ifc.dto.api.ApiResponse;
import com.infyro.ifc.dto.expense.*;
import com.infyro.ifc.exception.CropPlantingNotFoundException;
import com.infyro.ifc.exception.ExpenseNotFoundException;
import com.infyro.ifc.exception.FarmNotFoundException;
import com.infyro.ifc.mapper.ExpenseMapper;
import com.infyro.ifc.model.*;
import com.infyro.ifc.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {

    private final ExpenseRepo expenseRepo;
    private final FarmRepo farmRepo;
    private final CropPlantingRepo cropPlantingRepo;
    private final ExpenseMapper expenseMapper;

    @Transactional
    public ResponseEntity<ExpenseResponse> createExpense(Long farmId, String email, ExpenseRequest request) {

        Farm farm = farmRepo.findByIdAndOwner_Email(farmId, email)
                .orElseThrow(() -> new FarmNotFoundException("Farm not found"));

        CropPlanting planting = null;

        if (request.cropPlantingId() != null) {

            planting = cropPlantingRepo.findByIdAndField_Farm_Owner_Email(request.cropPlantingId(), email)
                    .orElseThrow(() -> new CropPlantingNotFoundException("Crop planting not found"));

            if (!planting.getField().getFarm().getId().equals(farm.getId())) {

                log.error("ExpenseService-create Failed!," +
                        "Planting does not belong to the specified farm \n Request_data={}",request);

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Planting does not belong to the specified farm");
            }
        }

        Expense expense = Expense.builder()
                .farm(farm)
                .cropPlanting(planting)
                .amount(request.amount())
                .expenseDate(request.expenseDate())
                .category(request.category())
                .description(request.description().trim())
                .build();

        log.info("ExpenseService-create Expense noted! \n Expense={} ",expense.toString());

        ExpenseResponse expenseResponse = expenseMapper.apply(expenseRepo.save(expense));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(expenseResponse);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<ExpenseResponse>> getAllExpensesByFarm(Long farmId, String email) {

        if (farmRepo.findByIdAndOwner_Email(farmId, email).isEmpty()) {
            log.error("ExpenseService-listByFarm Failed!,Farm not found with the ID={}",farmId);
            throw new FarmNotFoundException("Farm not found");
        }

        List<ExpenseResponse> expenseResponses = expenseRepo
                .findByFarm(farmId, email)
                .stream()
                .map(expenseMapper::response)
                .toList();

        return ResponseEntity.ok(expenseResponses);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ExpenseResponse> getExpenseByExpenseIdAndFarmId(Long id, Long farmId ,String email) {

        Expense expense = expenseRepo.findByIdAndFarm_Owner_Email(id, email)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found"));

        if (!expense.getFarm().getId().equals(farmId))
            throw new ExpenseNotFoundException("Expense not found");

        log.info("ExpenseService-getById Executed!,ID={}",id);
        return ResponseEntity.ok(expenseMapper.response(expense));

    }

    @Transactional
    public ResponseEntity<ApiResponse> deleteExpenseByIdAndFarmId(Long id,Long farmId ,String email) {

        Expense expense = expenseRepo.findByIdAndFarm_Owner_Email(id, email)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found"));

        if (!expense.getFarm().getId().equals(farmId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found");

        if (expense.isVoided()) {
            log.error("ExpenseService-delete Failed!,Expense is already cancelled \n Expense={}",expense);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Expense is already cancelled");
        }

        expense.setVoided(true);
        expense.setVoidedAt(Instant.now());
        expense.setVoidReason("Cancelled by owner; create a replacement expense to correct this entry");

        expenseRepo.save(expense);

        log.info("ExpenseService-delete Expense deleted!,Expense={}",expense);
        return ResponseEntity.ok(new ApiResponse("Expense deleted successfully"));
    }
}
