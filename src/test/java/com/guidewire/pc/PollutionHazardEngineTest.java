package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.PollutionHazardEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Environmental Impairment Liability Hazard Engine Tests")
public class PollutionHazardEngineTest {

    @Test
    @DisplayName("Should rate high environmental hazard and require $50,000 containment deductible")
    public void testHighPollutionHazardAssessment() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-EIL-3001");
        period.setTotalPremium(new BigDecimal("10000.00"));

        PollutionHazardEngine.PollutionResult res = PollutionHazardEngine.getInstance()
                .assessPollutionHazard(period, 4, 8, 0.4, 25); // 4 USTs, 8 Chem score, <1 mi to water, >20yr old

        assertNotNull(res);
        assertTrue(res.getEnvironmentalHazardMultiplier() >= 2.0);
        assertEquals(new BigDecimal("50000.00"), res.getRecommendedContainmentDeductible());
        assertTrue(res.getHazardCategory().contains("HIGH_ENVIRONMENTAL_HAZARD"));
    }
}
