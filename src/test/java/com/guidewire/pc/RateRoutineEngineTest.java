package com.guidewire.pc;

import com.guidewire.pc.constants.PCConstants;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.RateRoutineEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Guidewire OOTB Rate Routine & Rate Table Matrix Engine Tests")
public class RateRoutineEngineTest {

    @Test
    @DisplayName("Should execute Rate Routine pipeline for Commercial Auto in CA")
    public void testRateRoutineCommercialAuto() {
        PolicyPeriod period = new PolicyPeriod();
        period.setJobNumber("S000_RATE_ROUTINE_1");
        period.setProductCode(PCConstants.PRODUCT_COMMERCIAL_AUTO);
        period.setBaseState("CA");
        period.setCollisionDeductible("$1000");

        RateRoutineEngine.RateRoutineResult result = RateRoutineEngine.getInstance().executeRateRoutine(period);
        assertNotNull(result);
        assertNotNull(result.getFinalPremium());
        assertTrue(result.getFinalPremium().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(result.getExecutionSteps().size() >= 5);
        assertTrue(result.getExecutionSteps().get(0).contains("Step 1"));
    }

    @Test
    @DisplayName("Should enforce minimum statutory premium cap if routine output is below cap")
    public void testRateRoutineMinimumCap() {
        PolicyPeriod period = new PolicyPeriod();
        period.setJobNumber("S000_RATE_ROUTINE_2");
        period.setProductCode("PersonalAuto");
        period.setBaseState("CA");

        RateRoutineEngine.RateRoutineResult result = RateRoutineEngine.getInstance().executeRateRoutine(period);
        assertNotNull(result);
        assertTrue(result.getFinalPremium().compareTo(new BigDecimal("250.00")) >= 0);
    }
}
