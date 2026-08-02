package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.CommercialAutoRatingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LOB 2: Commercial Auto Data Model & Rating Tests")
public class CommercialAutoLOBTest {

    private PolicyPeriod policyPeriod;

    @BeforeEach
    public void setUp() {
        GosuBridge.initGosuEngine(new File("."));
        policyPeriod = new PolicyPeriod();
        policyPeriod.setPolicyNumber("POL-AUTO-20002");
        policyPeriod.setProductCode("CommercialAuto");
        policyPeriod.setBaseState("FL");
        policyPeriod.setStatus("Draft");
    }

    @Test
    @DisplayName("Test 1: Non-Fleet Local Commercial Auto Rating (3 Vehicles @ $1,200/veh)")
    public void testCommercialAutoNonFleetRating() {
        // 3 vehicles * $1200 = $3600 base -> Tax (+6%) = $216 -> Total = $3816.00
        BigDecimal premium = CommercialAutoRatingService.rateCommercialAuto(policyPeriod, 3, false, "Local");

        assertNotNull(premium);
        assertEquals(new BigDecimal("3816.00"), premium);
    }

    @Test
    @DisplayName("Test 2: Fleet Long Distance Commercial Auto Rating (6 Vehicles @ $2,200/veh with -10% Fleet Discount)")
    public void testCommercialAutoFleetLongDistanceRating() {
        // 6 vehicles * $2200 = $13,200 -> Fleet disc (-10%) = $11,880.00 -> Tax (+6%) = $712.80 -> Total = $12,592.80
        BigDecimal premium = CommercialAutoRatingService.rateCommercialAuto(policyPeriod, 6, true, "LongDistance");

        assertNotNull(premium);
        assertEquals(new BigDecimal("12592.80"), premium);
    }

    @Test
    @DisplayName("Test 3: Commercial Auto Validation Rules Success")
    public void testAutoValidationSuccess() {
        List<String> errors = CommercialAutoRatingService.validateCommercialAutoLine(policyPeriod, 5, "Local");

        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Test 4: Commercial Auto Validation Zero Vehicles Failure")
    public void testAutoValidationZeroVehiclesFailure() {
        List<String> errors = CommercialAutoRatingService.validateCommercialAutoLine(policyPeriod, 0, "Local");

        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("must have at least 1 registered vehicle")));
    }
}
