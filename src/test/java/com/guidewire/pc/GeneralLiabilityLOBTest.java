package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.GLRatingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LOB 3: General Liability Data Model & Rating Tests")
public class GeneralLiabilityLOBTest {

    private PolicyPeriod policyPeriod;

    @BeforeEach
    public void setUp() {
        GosuBridge.initGosuEngine(new File("."));
        policyPeriod = new PolicyPeriod();
        policyPeriod.setPolicyNumber("POL-GL-30003");
        policyPeriod.setProductCode("GeneralLiability");
        policyPeriod.setBaseState("NY");
        policyPeriod.setStatus("Draft");
    }

    @Test
    @DisplayName("Test 1: Occurrence GL Premium Rating ($1,000,000 Sales Exposure @ $4.50 rate)")
    public void testGLOccurrenceRating() {
        // ($1,000,000 / 1000) * 4.50 = 4,500.00 base -> Tax (+7%) = $315.00 -> Total = $4,815.00
        BigDecimal premium = GLRatingService.rateGeneralLiability(policyPeriod, new BigDecimal("1000000.00"), new BigDecimal("4.50"), false);

        assertNotNull(premium);
        assertEquals(new BigDecimal("4815.00"), premium);
    }

    @Test
    @DisplayName("Test 2: Claims-Made GL Premium Rating (-15% Claims-Made discount)")
    public void testGLClaimsMadeRating() {
        // ($1,000,000 / 1000) * 4.50 = 4,500.00 -> Claims-Made (-15%) = 3,825.00 -> Tax (+7%) = $267.75 -> Total = $4,092.75
        BigDecimal premium = GLRatingService.rateGeneralLiability(policyPeriod, new BigDecimal("1000000.00"), new BigDecimal("4.50"), true);

        assertNotNull(premium);
        assertEquals(new BigDecimal("4092.75"), premium);
    }

    @Test
    @DisplayName("Test 3: General Liability Validation Rules Success")
    public void testGLValidationSuccess() {
        List<String> errors = GLRatingService.validateGeneralLiabilityLine(policyPeriod, new BigDecimal("500000.00"), "Occurrence");

        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Test 4: General Liability Validation Invalid Form Failure")
    public void testGLValidationInvalidFormFailure() {
        List<String> errors = GLRatingService.validateGeneralLiabilityLine(policyPeriod, new BigDecimal("500000.00"), "InvalidForm");

        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Coverage form must be either Occurrence or ClaimsMade")));
    }
}
