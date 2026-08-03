package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.TRIARatingEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Terrorism Risk Insurance Act (TRIA) Opt-In/Opt-Out Engine Tests")
public class TRIARatingEngineTest {

    @Test
    @DisplayName("Should apply 3.5% TRIA surcharge and attach certified terrorism coverage when opted in")
    public void testTRIAOptInSurcharge() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-TRIA-2001");
        period.setTotalPremium(new BigDecimal("10000.00"));

        TRIARatingEngine.TRIAResult res = TRIARatingEngine.getInstance()
                .evaluateTRIAOption(period, true, 0.035);

        assertNotNull(res);
        assertTrue(res.isOptInTerrorismCoverage());
        assertEquals(new BigDecimal("350.00"), res.getTriaPremiumSurcharge());
        assertEquals(new BigDecimal("10350.00"), res.getFinalTotalPremium());
        assertTrue(res.getAttachedEndorsement().contains("TRIA-COV-2026"));
    }
}
