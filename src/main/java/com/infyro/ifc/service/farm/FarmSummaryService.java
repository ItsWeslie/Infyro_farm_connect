package com.infyro.ifc.service.farm;

import com.infyro.ifc.dto.farm.*;
import com.infyro.ifc.enums.CropPlantingStatus;
import com.infyro.ifc.enums.ExpenseCategory;
import com.infyro.ifc.enums.HarvestUnit;
import com.infyro.ifc.exception.FarmNotFoundException;
import com.infyro.ifc.model.*;
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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
        List<CropPlanting> plantings = cropPlantingRepo.findAllByField_Farm_IdAndField_Farm_Owner_Email(farmId, email);
        List<Expense> expenses = activeExpenses(expenseRepo.findAllByFarm_IdAndFarm_Owner_EmailOrderByExpenseDateDescCreatedAtDesc(farmId, email));
        List<HarvestRecord> harvests = activeHarvests(harvestRecordRepo.findAllByCropPlanting_Field_Farm_IdAndCropPlanting_Field_Farm_Owner_EmailOrderByHarvestDateDescCreatedAtDesc(farmId, email));
        log.info("FarmSummaryService-getFarmSummary Executed for the FarmID={} \n Data={}",farmId,new FarmSummaryResponse(farm.getId(), farm.getFarmName(),
                fieldRepo.findAllByFarm_IdAndFarm_Owner_Email(farmId, email).size(),
                plantings.size(), plantingCounts(plantings), sumExpenses(expenses),
                expensesByCategory(expenses), harvestsByUnit(harvests), harvests.size()));
        return new FarmSummaryResponse(farm.getId(), farm.getFarmName(),
                fieldRepo.findAllByFarm_IdAndFarm_Owner_Email(farmId, email).size(),
                plantings.size(), plantingCounts(plantings), sumExpenses(expenses),
                expensesByCategory(expenses), harvestsByUnit(harvests), harvests.size());
    }

    public ExpenseSummaryResponse getExpenseSummary(Long farmId, String email, LocalDate from, LocalDate to) {
        getOwnedFarm(farmId, email);
        validateDateRange(from, to);
        List<Expense> expenses = activeExpenses(expenseRepo.findAllByFarm_IdAndFarm_Owner_EmailOrderByExpenseDateDescCreatedAtDesc(farmId, email))
                .stream().filter(withinRange(Expense::getExpenseDate, from, to)).toList();
        log.info("FarmSummaryService-getExpenseSummary Executed for the FarmID={} \n Expense_Data={}",farmId,new ExpenseSummaryResponse(farmId, from, to, expenses.size(), sumExpenses(expenses), expensesByCategory(expenses)));
        return new ExpenseSummaryResponse(farmId, from, to, expenses.size(), sumExpenses(expenses), expensesByCategory(expenses));
    }

    public HarvestSummaryResponse getHarvestSummary(Long farmId, String email, LocalDate from, LocalDate to) {
        getOwnedFarm(farmId, email);
        validateDateRange(from, to);
        List<HarvestRecord> harvests = activeHarvests(harvestRecordRepo.findAllByCropPlanting_Field_Farm_IdAndCropPlanting_Field_Farm_Owner_EmailOrderByHarvestDateDescCreatedAtDesc(farmId, email))
                .stream().filter(withinRange(HarvestRecord::getHarvestDate, from, to)).toList();
        log.info("FarmSummaryService-getHarvestSummary Executed for the FarmID={} \n Harvest_Data={}",farmId,new HarvestSummaryResponse(farmId, from, to, harvests.size(), harvestsByUnit(harvests)));
        return new HarvestSummaryResponse(farmId, from, to, harvests.size(), harvestsByUnit(harvests));
    }

    public PlantingSummaryResponse getPlantingSummary(Long farmId, String email) {
        getOwnedFarm(farmId, email);
        List<CropPlanting> plantings = cropPlantingRepo.findAllByField_Farm_IdAndField_Farm_Owner_Email(farmId, email);
        log.info("FarmSummaryService-getPlantingSummary Executed for the FarmID={} \n Planting_Data={}",farmId,new PlantingSummaryResponse(farmId, plantings.size(), plantingCounts(plantings)));
        return new PlantingSummaryResponse(farmId, plantings.size(), plantingCounts(plantings));
    }

    private Farm getOwnedFarm(Long farmId, String email) {
        log.info("FarmSummaryService-getOwnedFarm Executed for the FarmID={}",farmId);
        return farmRepo.findByIdAndOwner_Email(farmId, email)
                .orElseThrow(() -> new FarmNotFoundException("Farm not found with ID: " + farmId));
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            log.error("FarmSummaryService-validateDateRange Failed 'from' date {} must be on or before 'to' date {}",from,to);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' date must be on or before 'to' date");
        }
        log.info("FarmSummaryService-validateDateRange Executed!");
    }

    private <T> Predicate<T> withinRange(Function<T, LocalDate> dateGetter, LocalDate from, LocalDate to) {
        return item -> {
            LocalDate date = dateGetter.apply(item);
            return (from == null || !date.isBefore(from)) && (to == null || !date.isAfter(to));
        };
    }

    private List<Expense> activeExpenses(List<Expense> records) {
        return records.stream().filter(record -> !record.isVoided()).toList();
    }

    private List<HarvestRecord> activeHarvests(List<HarvestRecord> records) {
        return records.stream().filter(record -> !record.isVoided()).toList();
    }

    private BigDecimal sumExpenses(List<Expense> records) {
        return records.stream().map(Expense::getAmount).filter(value -> value != null).reduce(ZERO, BigDecimal::add);
    }

    private Map<ExpenseCategory, BigDecimal> expensesByCategory(List<Expense> records) {
        Map<ExpenseCategory, BigDecimal> totals = new EnumMap<>(ExpenseCategory.class);
        for (Expense record : records) {
            if (record.getCategory() != null && record.getAmount() != null)
                totals.merge(record.getCategory(), record.getAmount(), BigDecimal::add);
        }
        return totals;
    }

    private Map<HarvestUnit, BigDecimal> harvestsByUnit(List<HarvestRecord> records) {
        Map<HarvestUnit, BigDecimal> totals = new EnumMap<>(HarvestUnit.class);
        for (HarvestRecord record : records) {
            if (record.getUnit() != null && record.getQuantity() != null)
                totals.merge(record.getUnit(), record.getQuantity(), BigDecimal::add);
        }
        return totals;
    }

    private Map<CropPlantingStatus, Long> plantingCounts(List<CropPlanting> plantings) {
        Map<CropPlantingStatus, Long> counts = new EnumMap<>(CropPlantingStatus.class);
        for (CropPlantingStatus status : CropPlantingStatus.values()) counts.put(status, 0L);
        plantings.stream().collect(Collectors.groupingBy(CropPlanting::getStatus, Collectors.counting())).forEach(counts::put);
        return counts;
    }
}
