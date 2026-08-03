package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.ExperienceRatingEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NCCI Experience Rating Modification (EMOD) Engine Tests")
public class ExperienceRatingEngineTest {

    @Test
    @DisplayName("Should calculate favorable credit EMOD factor for low loss history")
    public void testFavorableCreditEMOD() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-EMOD-101");
        period.setTotalPremium(new BigDecimal("10000.00"));

        ExperienceRatingEngine.EmodResult res = ExperienceRatingEngine.getInstance()
                .calculateExperienceMod(period, new BigDecimal("16000.00"), new BigDecimal("20000.00"));

        assertNotNull(res);
        assertEquals(0.80, res.getEmodFactor());
        assertEquals("FAVORABLE_CREDIT", res.getModRatingTier());
        assertEquals(new BigDecimal("8000.00"), res.getEmodModifiedPremium());
        assertEquals(new BigDecimal("-2000.00"), res.getPremiumAdjustment());
    }
}
