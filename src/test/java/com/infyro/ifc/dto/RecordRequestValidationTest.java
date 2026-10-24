package com.infyro.ifc.dto;

import com.infyro.ifc.dto.expense.ExpenseRequest;
import com.infyro.ifc.dto.harvest.HarvestRecordRequest;
import com.infyro.ifc.enums.ExpenseCategory;
import com.infyro.ifc.enums.HarvestUnit;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RecordRequestValidationTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void rejectsZeroExpenseAmount() {
        ExpenseRequest request = new ExpenseRequest(null, BigDecimal.ZERO, LocalDate.now(),
                ExpenseCategory.OTHER, "Supplies");
        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsBlankExpenseDescription() {
        ExpenseRequest request = new ExpenseRequest(null, new BigDecimal("10.00"), LocalDate.now(),
                ExpenseCategory.OTHER, "   ");
        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsFutureExpenseDate() {
        ExpenseRequest request = new ExpenseRequest(null, new BigDecimal("10.00"),
                LocalDate.now().plusDays(1), ExpenseCategory.OTHER, "Supplies");
        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsZeroHarvestQuantity() {
        HarvestRecordRequest request = new HarvestRecordRequest(LocalDate.now(), BigDecimal.ZERO,
                HarvestUnit.KILOGRAM, "Harvest");
        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsFutureHarvestDate() {
        HarvestRecordRequest request = new HarvestRecordRequest(LocalDate.now().plusDays(1),
                new BigDecimal("2.500"), HarvestUnit.KILOGRAM, "Harvest");
        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void acceptsValidExpenseRequest() {
        ExpenseRequest request = new ExpenseRequest(null, new BigDecimal("100.00"), LocalDate.now(),
                ExpenseCategory.OTHER, "Seeds");
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void acceptsValidHarvestRequest() {
        HarvestRecordRequest request = new HarvestRecordRequest(LocalDate.now(),
                new BigDecimal("2.500"), HarvestUnit.KILOGRAM, "Morning harvest");
        assertTrue(validator.validate(request).isEmpty());
    }
}
