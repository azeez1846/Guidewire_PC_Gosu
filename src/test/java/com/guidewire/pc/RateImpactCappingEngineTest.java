package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.RateImpactCappingEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Renewal Rate Impact Capping & Transition Smoothing Engine Tests")
public class RateImpactCappingEngineTest {

    @Test
    @DisplayName("Should apply 10% rate cap when uncapped renewal proposed increase is 25%")
    public void testRenewalRateIncreaseCapping() {
        PolicyPeriod prior = new PolicyPeriod();
        prior.setPolicyNumber("POL-CAP-1001");
        prior.setTotalPremium(new BigDecimal("10000.00")); // Prior term $10,000

        RateImpactCappingEngine.RateCapResult res = RateImpactCappingEngine.getInstance()
                .applyRenewalRateCap(prior, new BigDecimal("12500.00"), 0.10); // Proposed $12,500 (+25%), Cap 10%

        assertNotNull(res);
        assertTrue(res.isRateCapApplied());
        assertEquals(0.25, res.getUncappedIncreasePercentage());
        assertEquals(new BigDecimal("11000.00"), res.getCappedRenewalPremium());
        assertEquals(new BigDecimal("1500.00"), res.getCarrierSubsidyAmount());
    }
}
