package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.CPRatingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LOB 4: Commercial Property Data Model & Rating Tests")
public class CommercialPropertyLOBTest {

    private PolicyPeriod policyPeriod;

    @BeforeEach
    public void setUp() {
        GosuBridge.initGosuEngine(new File("."));
        policyPeriod = new PolicyPeriod();
        policyPeriod.setPolicyNumber("POL-CP-40004");
        policyPeriod.setProductCode("CommercialProperty");
        policyPeriod.setBaseState("IL");
        policyPeriod.setStatus("Draft");
    }

    @Test
    @DisplayName("Test 1: Standard Protection Class Commercial Property Rating ($1M Bldg, $250k BPP @ $0.35 rate)")
    public void testCPStandardRating() {
        // ($1,250,000 / 100) * 0.35 = 4,375.00 base -> Tax (+5%) = $218.75 -> Total = $4,593.75
        BigDecimal premium = CPRatingService.rateCommercialProperty(policyPeriod, new BigDecimal("1000000.00"), new BigDecimal("250000.00"), "3");

        assertNotNull(premium);
        assertEquals(new BigDecimal("4593.75"), premium);
    }

    @Test
    @DisplayName("Test 2: High Protection Class Commercial Property Rating (Prot Class 9 @ $0.85 rate)")
    public void testCPHighProtectionClassRating() {
        // ($1,250,000 / 100) * 0.85 = 10,625.00 base -> Tax (+5%) = $531.25 -> Total = $11,156.25
        BigDecimal premium = CPRatingService.rateCommercialProperty(policyPeriod, new BigDecimal("1000000.00"), new BigDecimal("250000.00"), "9");

        assertNotNull(premium);
        assertEquals(new BigDecimal("11156.25"), premium);
    }

    @Test
    @DisplayName("Test 3: Commercial Property Validation Success (90% Coinsurance)")
    public void testCPValidationSuccess() {
        List<String> errors = CPRatingService.validateCommercialPropertyLine(policyPeriod, new BigDecimal("1000000.00"), 90);

        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Test 4: Commercial Property Validation Low Coinsurance Penalty Failure (< 80%)")
    public void testCPLowCoinsuranceFailure() {
        List<String> errors = CPRatingService.validateCommercialPropertyLine(policyPeriod, new BigDecimal("1000000.00"), 60);

        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Coinsurance percentage must be between 80% and 100%")));
    }
}
