package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.TelematicsRatingEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Auto Fleet Telematics Driving Behavior UBI Engine Tests")
public class TelematicsRatingEngineTest {

    @Test
    @DisplayName("Should apply 20% UBI discount for excellent telematics driving safety score (>= 85)")
    public void testExcellentTelematicsScoreDiscount() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-UBI-1001");
        period.setTotalPremium(new BigDecimal("2000.00"));

        TelematicsRatingEngine.TelematicsResult res = TelematicsRatingEngine.getInstance()
                .evaluateTelematicsDrivingScore(period, 1.0, 0.5, 0.02, 0.0); // Clean driving

        assertNotNull(res);
        assertTrue(res.getSafetyScore() >= 85.0);
        assertEquals(-0.20, res.getRateAdjustmentPct());
        assertEquals(new BigDecimal("-400.00"), res.getAdjustmentAmount());
        assertEquals(new BigDecimal("1600.00"), res.getAdjustedPremium());
    }
}
