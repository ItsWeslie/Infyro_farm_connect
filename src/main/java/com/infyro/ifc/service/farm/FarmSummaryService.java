package com.infyro.ifc.service.farm;

import com.infyro.ifc.dto.farm.*;
import com.infyro.ifc.enums.CropPlantingStatus;
import com.infyro.ifc.enums.ExpenseCategory;
import com.infyro.ifc.enums.HarvestUnit;
import com.infyro.ifc.exception.FarmNotFoundException;
import com.infyro.ifc.model.Farm;
import com.infyro.ifc.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FarmSummaryService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final FarmRepo farmRepo;
    private final FieldRepo fieldRepo;
    private final CropPlantingRepo cropPlantingRepo;
    private final ExpenseRepo expenseRepo;
    private final HarvestRecordRepo harvestRecordRepo;

    public FarmSummaryResponse getFarmSummary(Long farmId, String email) {

        Farm farm = getOwnedFarm(farmId, email);

        long totalFields = fieldRepo.countByFarm(farmId, email);

        List<CropPlantingRepo.StatusCount> plantingTotals =
                cropPlantingRepo.countByFarmGroupedByStatus(farmId, email);

        Map<CropPlantingStatus, Long> plantingCounts = plantingCounts(plantingTotals);

        long totalPlantings = plantingCounts
                .values()
                .stream()
                .mapToLong(Long::longValue)
                .sum();

        List<ExpenseRepo.CategoryTotal> expenseTotals =
                expenseRepo.summarizeByFarm(farmId, email, null, null);

        Map<ExpenseCategory, BigDecimal> expensesByCategory = expenseTotalsByCategory(expenseTotals);
        BigDecimal totalExpenses = sumExpenseTotals(expenseTotals);

        List<HarvestRecordRepo.UnitTotal> harvestTotals =
                harvestRecordRepo.summarizeByFarm(farmId, email, null, null);

        Map<HarvestUnit, BigDecimal> harvestsByUnit = harvestTotalsByUnit(harvestTotals);

        long harvestRecordCount = harvestTotals.stream()
                .mapToLong(total -> total.getRecordCount() == null ? 0L : total.getRecordCount())
                .sum();

        FarmSummaryResponse response = new FarmSummaryResponse(
                farm.getId(), farm.getFarmName(), totalFields, totalPlantings,
                plantingCounts, totalExpenses, expensesByCategory, harvestsByUnit,
                harvestRecordCount);

        log.info("FarmSummaryService-getFarmSummary Executed for the FarmID={} Data={}",
                farmId, response);
        return response;
    }

    public ExpenseSummaryResponse getExpenseSummary(Long farmId,
                                                    String email,
                                                    LocalDate from,
                                                    LocalDate to) {
        getOwnedFarm(farmId, email);
        validateDateRange(from, to);

        List<ExpenseRepo.CategoryTotal> totals =
                expenseRepo.summarizeByFarm(farmId, email, from, to);

        Map<ExpenseCategory, BigDecimal> byCategory = expenseTotalsByCategory(totals);

        long recordCount = totals.stream()
                .mapToLong(total -> total.getRecordCount() == null ? 0L : total.getRecordCount())
                .sum();

        BigDecimal totalAmount = sumExpenseTotals(totals);

        ExpenseSummaryResponse response = ExpenseSummaryResponse.builder()
                .farmId(farmId)
                .from(from)
                .to(to)
                .recordCount(recordCount)
                .totalAmount(totalAmount)
                .byCategory(byCategory)
                .build();

        log.info("FarmSummaryService-getExpenseSummary Executed for the FarmID={} Expense_Data={}",
                farmId, response);

        return response;
    }

    public HarvestSummaryResponse getHarvestSummary(Long farmId,
                                                    String email,
                                                    LocalDate from,
                                                    LocalDate to) {

        getOwnedFarm(farmId, email);
        validateDateRange(from, to);

        List<HarvestRecordRepo.UnitTotal> totals =
                harvestRecordRepo.summarizeByFarm(farmId, email, from, to);

        Map<HarvestUnit, BigDecimal> quantityByUnit = harvestTotalsByUnit(totals);

        long recordCount = totals.stream()
                .mapToLong(total -> total.getRecordCount() == null ? 0L : total.getRecordCount())
                .sum();

        HarvestSummaryResponse response =
                new HarvestSummaryResponse(farmId, from, to, recordCount, quantityByUnit);

        log.info("FarmSummaryService-getHarvestSummary Executed for the FarmID={} Harvest_Data={}",
                farmId, response);
        return response;
    }

    public PlantingSummaryResponse getPlantingSummary(Long farmId, String email) {

        getOwnedFarm(farmId, email);

        Map<CropPlantingStatus, Long> counts =
                plantingCounts(cropPlantingRepo.countByFarmGroupedByStatus(farmId, email));

        long totalPlantings = counts
                .values()
                .stream()
                .mapToLong(Long::longValue)
                .sum();

        PlantingSummaryResponse response =
                new PlantingSummaryResponse(farmId, totalPlantings, counts);

        log.info("FarmSummaryService-getPlantingSummary Executed for the FarmID={} Planting_Data={}",
                farmId, response);

        return response;
    }

    private Farm getOwnedFarm(Long farmId, String email) {
        return farmRepo.findByIdAndOwner_Email(farmId, email)
                .orElseThrow(() -> new FarmNotFoundException("Farm not found with ID: " + farmId));
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "'from' date must be on or before 'to' date");
        }
    }

    private Map<CropPlantingStatus, Long> plantingCounts(
            List<CropPlantingRepo.StatusCount> totals) {

        Map<CropPlantingStatus, Long> counts = new EnumMap<>(CropPlantingStatus.class);

        for (CropPlantingStatus status : CropPlantingStatus.values()) {
            counts.put(status, 0L);
        }

        for (CropPlantingRepo.StatusCount total : totals) {
            if (total.getStatus() != null && total.getTotal() != null) {
                counts.put(total.getStatus(), total.getTotal());
            }
        }

        return counts;
    }

    private Map<ExpenseCategory, BigDecimal> expenseTotalsByCategory(
            List<ExpenseRepo.CategoryTotal> totals) {

        Map<ExpenseCategory, BigDecimal> result = new EnumMap<>(ExpenseCategory.class);

        for (ExpenseRepo.CategoryTotal total : totals) {
            if (total.getCategory() != null && total.getTotal() != null) {
                result.put(total.getCategory(), total.getTotal());
            }
        }
        return result;
    }

    private BigDecimal sumExpenseTotals(List<ExpenseRepo.CategoryTotal> totals) {
        return totals.stream()
                .map(ExpenseRepo.CategoryTotal::getTotal)
                .filter(value -> value != null)
                .reduce(ZERO, BigDecimal::add);
    }

    private Map<HarvestUnit, BigDecimal> harvestTotalsByUnit(
            List<HarvestRecordRepo.UnitTotal> totals) {
        Map<HarvestUnit, BigDecimal> result = new EnumMap<>(HarvestUnit.class);
        for (HarvestRecordRepo.UnitTotal total : totals) {
            if (total.getUnit() != null && total.getTotal() != null) {
                result.put(total.getUnit(), total.getTotal());
            }
        }
        return result;
    }
}
