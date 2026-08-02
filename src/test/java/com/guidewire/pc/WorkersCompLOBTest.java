package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.WCRatingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LOB 1: Workers Compensation Data Model & Rating Tests")
public class WorkersCompLOBTest {

    private PolicyPeriod policyPeriod;

    @BeforeEach
    public void setUp() {
        GosuBridge.initGosuEngine(new File("."));
        policyPeriod = new PolicyPeriod();
        policyPeriod.setPolicyNumber("POL-WC-10001");
        policyPeriod.setProductCode("WorkersComp");
        policyPeriod.setBaseState("TX");
        policyPeriod.setStatus("Draft");
    }

    @Test
    @DisplayName("Test 1: Workers Comp Premium Rating ($500,000 Payroll @ $2.50 rate, E-Mod 0.95)")
    public void testWCRatingCalculation() {
        BigDecimal payroll = new BigDecimal("500000.00");
        BigDecimal emod = new BigDecimal("0.950");
        BigDecimal rate = new BigDecimal("2.50");

        // Manual Prem = (500,000 / 100) * 2.50 = 12,500.00
        // Standard Prem = 12,500.00 * 0.950 = 11,875.00
        // Tax & Expense (+8%) = 950.00 -> Total = 12,825.00
        BigDecimal premium = WCRatingService.rateWorkersComp(policyPeriod, payroll, emod, rate);

        assertNotNull(premium);
        assertEquals(new BigDecimal("12825.00"), premium);
    }

    @Test
    @DisplayName("Test 2: Workers Comp Validation Rules Success")
    public void testWCValidationSuccess() {
        List<String> errors = WCRatingService.validateWorkersCompLine(policyPeriod, new BigDecimal("250000.00"), new BigDecimal("0.900"));

        assertNotNull(errors);
        assertTrue(errors.isEmpty(), "Valid Workers Comp line parameters should pass validation");
    }

    @Test
    @DisplayName("Test 3: Workers Comp Validation Invalid Payroll Failure")
    public void testWCValidationZeroPayrollFailure() {
        List<String> errors = WCRatingService.validateWorkersCompLine(policyPeriod, BigDecimal.ZERO, new BigDecimal("0.900"));

        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Estimated annual payroll must be greater than 0")));
    }

    @Test
    @DisplayName("Test 4: Workers Comp Validation Invalid Experience Mod Failure")
    public void testWCValidationInvalidEModFailure() {
        List<String> errors = WCRatingService.validateWorkersCompLine(policyPeriod, new BigDecimal("100000.00"), new BigDecimal("3.500"));

        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("outside acceptable NCCI range")));
    }
}
