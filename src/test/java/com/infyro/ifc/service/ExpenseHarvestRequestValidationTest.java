package com.infyro.ifc.service;

import com.infyro.ifc.dto.expense.ExpenseRequest;
import com.infyro.ifc.dto.harvest.HarvestRecordRequest;
import com.infyro.ifc.enums.ExpenseCategory;
import com.infyro.ifc.enums.HarvestUnit;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseHarvestRequestValidationTest {
    private static Validator validator;
    private static jakarta.validation.ValidatorFactory factory;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @Test
    void acceptsExpenseAmountAtMinimumAndMaximumPrecision() {
        assertTrue(validator.validate(expense("0.01")).isEmpty());
        assertTrue(validator.validate(expense("999999999999.99")).isEmpty());
    }

    @Test
    void rejectsExpenseAmountOutsidePositiveTwoDecimalBoundary() {
        assertFalse(validator.validate(expense("0.00")).isEmpty());
        assertFalse(validator.validate(expense("-0.01")).isEmpty());
        assertFalse(validator.validate(expense("1.001")).isEmpty());
        assertFalse(validator.validate(expense("1000000000000.00")).isEmpty());
    }

    @Test
    void acceptsHarvestQuantityAtMinimumAndMaximumPrecision() {
        assertTrue(validator.validate(harvest("0.001")).isEmpty());
        assertTrue(validator.validate(harvest("99999999999.999")).isEmpty());
    }

    @Test
    void rejectsHarvestQuantityOutsidePositiveThreeDecimalBoundary() {
        assertFalse(validator.validate(harvest("0.000")).isEmpty());
        assertFalse(validator.validate(harvest("-0.001")).isEmpty());
        assertFalse(validator.validate(harvest("1.0001")).isEmpty());
        assertFalse(validator.validate(harvest("100000000000.000")).isEmpty());
    }

    private static ExpenseRequest expense(String amount) {
        return new ExpenseRequest(null, new BigDecimal(amount), LocalDate.now(),
                ExpenseCategory.FERTILIZER, "Fertilizer");
    }

    private static HarvestRecordRequest harvest(String quantity) {
        return new HarvestRecordRequest(LocalDate.now(), new BigDecimal(quantity),
                HarvestUnit.KILOGRAM, null);
    }
}
