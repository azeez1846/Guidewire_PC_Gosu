package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.FloodZoneRatingEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Flood Zone Risk Rating & Elevation Certificate Engine Tests")
public class FloodZoneRatingEngineTest {

    @Test
    @DisplayName("Should apply 30% elevation credit and 10% vent credit for structure elevated >= 2ft above BFE")
    public void testElevatedStructureFloodCredit() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-FLOOD-5001");
        period.setTotalPremium(new BigDecimal("3000.00"));

        FloodZoneRatingEngine.FloodResult res = FloodZoneRatingEngine.getInstance()
                .rateFloodZoneRisk(period, "Zone A", 14.0, 12.0, true); // +2ft elevation diff, has vents

        assertNotNull(res);
        assertEquals(2.0, res.getElevationDifferentialFt());
        assertEquals(-0.40, res.getRateAdjustmentPct()); // -30% elevation -10% vent credit
        assertEquals(new BigDecimal("-1200.00"), res.getAdjustmentAmount());
        assertEquals(new BigDecimal("1800.00"), res.getFinalFloodPremium());
    }
}
