package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.CURatingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LOB 5: Commercial Umbrella Data Model & Rating Tests")
public class CommercialUmbrellaLOBTest {

    private PolicyPeriod policyPeriod;

    @BeforeEach
    public void setUp() {
        GosuBridge.initGosuEngine(new File("."));
        policyPeriod = new PolicyPeriod();
        policyPeriod.setPolicyNumber("POL-UMB-50005");
        policyPeriod.setProductCode("CommercialUmbrella");
        policyPeriod.setBaseState("CA");
        policyPeriod.setStatus("Draft");
    }

    @Test
    @DisplayName("Test 1: Standard Commercial Umbrella Rating ($1M Limit, $10,000 SIR, 2 Underlying Policies)")
    public void testCommercialUmbrellaStandardRating() {
        // 1st million = $1,500 base -> Tax (+5%) = $75.00 -> Total = $1,575.00
        BigDecimal premium = CURatingService.rateCommercialUmbrella(policyPeriod, new BigDecimal("1000000.00"), new BigDecimal("10000.00"), 2);

        assertNotNull(premium);
        assertEquals(new BigDecimal("1575.00"), premium);
    }

    @Test
    @DisplayName("Test 2: High Limit & Multi-Underlying Policy Commercial Umbrella Rating ($5M Limit, 4 Underlying Policies)")
    public void testCommercialUmbrellaMultiPolicyRating() {
        // 1st million = $1,500 + (4 * $800) = $4,700 base -> 4 policies (+20% surcharge) = $5,640.00 -> Tax (+5%) = $282.00 -> Total = $5,922.00
        BigDecimal premium = CURatingService.rateCommercialUmbrella(policyPeriod, new BigDecimal("5000000.00"), new BigDecimal("10000.00"), 4);

        assertNotNull(premium);
        assertEquals(new BigDecimal("5922.00"), premium);
    }

    @Test
    @DisplayName("Test 3: Commercial Umbrella Validation Success")
    public void testUmbrellaValidationSuccess() {
        List<String> errors = CURatingService.validateCommercialUmbrellaLine(policyPeriod, new BigDecimal("2000000.00"), 2);

        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Test 4: Commercial Umbrella Validation Low Limit Failure (< $1M)")
    public void testUmbrellaValidationLowLimitFailure() {
        List<String> errors = CURatingService.validateCommercialUmbrellaLine(policyPeriod, new BigDecimal("500000.00"), 2);

        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("must be at least $1,000,000")));
    }
}
