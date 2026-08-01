package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.ProducerCode;
import com.guidewire.pc.service.ProducerCommissionService;
import com.guidewire.pc.service.ProducerValidationRules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 4: Producer Code & Agency Commission Engine Tests")
public class ProducerCommissionTest {

    private PolicyPeriod policyPeriod;
    private ProducerCode producerCode;

    @BeforeEach
    public void setUp() {
        GosuBridge.initGosuEngine(new File("."));
        policyPeriod = new PolicyPeriod();
        policyPeriod.setPolicyNumber("POL-PROD-4004");
        policyPeriod.setJobType("Submission");
        policyPeriod.setBaseState("TX");
        policyPeriod.setTotalPremium(new BigDecimal("10000.00"));

        producerCode = new ProducerCode("PR-1001", "Active", "Gold", new BigDecimal("15.00"), new BigDecimal("10.00"), "TX,FL,CA");
    }

    @Test
    @DisplayName("Test 1: New Business Commission Calculation (15%)")
    public void testNewBusinessCommissionCalculation() {
        BigDecimal commission = ProducerCommissionService.calculateCommission(policyPeriod, producerCode);

        assertNotNull(commission);
        assertEquals(new BigDecimal("1500.00"), commission);
    }

    @Test
    @DisplayName("Test 2: Renewal Commission Calculation (10%)")
    public void testRenewalCommissionCalculation() {
        policyPeriod.setJobType("Renewal");

        BigDecimal commission = ProducerCommissionService.calculateCommission(policyPeriod, producerCode);

        assertNotNull(commission);
        assertEquals(new BigDecimal("1000.00"), commission);
    }

    @Test
    @DisplayName("Test 3: Active Producer License Validation Success")
    public void testActiveProducerValidationPass() {
        List<String> errors = ProducerValidationRules.validateProducerForPolicy(policyPeriod, producerCode);

        assertNotNull(errors);
        assertTrue(errors.isEmpty(), "Active producer licensed in state should have no validation errors");
    }

    @Test
    @DisplayName("Test 4: Suspended Producer Validation Failure")
    public void testSuspendedProducerValidationFailure() {
        producerCode.setProducerStatus("Suspended");

        List<String> errors = ProducerValidationRules.validateProducerForPolicy(policyPeriod, producerCode);

        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("PRODUCER_INACTIVE")));
    }

    @Test
    @DisplayName("Test 5: Unlicensed State Jurisdiction Validation Failure")
    public void testUnlicensedStateProducerValidationFailure() {
        policyPeriod.setBaseState("NY"); // Producer only licensed in TX,FL,CA

        List<String> errors = ProducerValidationRules.validateProducerForPolicy(policyPeriod, producerCode);

        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("PRODUCER_NOT_LICENSED_IN_STATE")));
    }
}
