package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.PolicyDiffEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class PolicyDiffEngineTest {

    @Test
    @DisplayName("Test Endorsement Rating Line-Item Diff Calculation")
    public void testEndorsementDiffCalculation() {
        PolicyDiffEngine engine = PolicyDiffEngine.getInstance();
        assertNotNull(engine);

        PolicyPeriod base = new PolicyPeriod();
        base.setPolicyNumber("POL-MTA-1001");
        base.setJobNumber("BASE-JOB-01");
        base.setTotalPremium(new BigDecimal("2000.00"));

        PolicyPeriod revised = new PolicyPeriod();
        revised.setPolicyNumber("POL-MTA-1001");
        revised.setJobNumber("REV-JOB-02");
        revised.setTotalPremium(new BigDecimal("2600.00"));

        var result = engine.calculateEndorsementDiff(base, revised, 0.50);

        assertNotNull(result);
        assertEquals("POL-MTA-1001", result.policyNumber());
        assertEquals(new BigDecimal("600.00"), result.netDeltaPremium());
        assertEquals(new BigDecimal("300.00"), result.netProratedPremium());
        assertFalse(result.lineItemDiffs().isEmpty(), "Line item diff list should contain itemized coverage deltas");
    }
}
