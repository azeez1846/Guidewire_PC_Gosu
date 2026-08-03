package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.UWRatingOverrideEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Underwriter Manual Rate Override & Schedule Rating Ledger Tests")
public class UWRatingOverrideEngineTest {

    @Test
    @DisplayName("Should apply 10% schedule credit discount")
    public void testScheduleCreditDiscount() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-OVR-101");
        period.setTotalPremium(new BigDecimal("10000.00"));

        UWRatingOverrideEngine.OverrideResult res = UWRatingOverrideEngine.getInstance()
                .applyRatingOverride(period, -0.10, null, "su", "Superior safety controls");

        assertNotNull(res);
        assertEquals(new BigDecimal("9000.00"), res.getAdjustedPremium());
        assertEquals(new BigDecimal("-1000.00"), res.getPremiumDelta());
        assertFalse(res.isManualOverrideApplied());
    }

    @Test
    @DisplayName("Should apply explicit manual rate override")
    public void testManualRateOverride() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-OVR-202");
        period.setTotalPremium(new BigDecimal("10000.00"));

        UWRatingOverrideEngine.OverrideResult res = UWRatingOverrideEngine.getInstance()
                .applyRatingOverride(period, 0.0, new BigDecimal("7500.00"), "su_mgr", "Manager override for competitive retention");

        assertNotNull(res);
        assertEquals(new BigDecimal("7500.00"), res.getAdjustedPremium());
        assertTrue(res.isManualOverrideApplied());
    }
}
