package com.guidewire.pc;

import com.guidewire.pc.service.ClaimCenterIntegrationEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ClaimCenterIntegrationTest {

    @Test
    @DisplayName("Test ClaimCenter FNOL Ingestion and Underwriting Hold Activation")
    public void testFNOLIngestionAndHoldActivation() {
        ClaimCenterIntegrationEngine engine = ClaimCenterIntegrationEngine.getInstance();
        assertNotNull(engine);

        String policyNum = "POL-CC-TEST-881";
        BigDecimal writtenPrem = new BigDecimal("2000.00");

        // Ingest small claim ($500) -> 25% loss ratio -> no hold
        var result1 = engine.ingestAndEvaluateFNOL(policyNum, "WIND_HAIL", new BigDecimal("500.00"), "Windshield crack", writtenPrem);
        assertNotNull(result1);
        assertEquals(policyNum, result1.updatedLossSummary().getPolicyNumber());
        assertFalse(result1.updatedLossSummary().isUnderwritingHoldRequired(), "Loss ratio 25% should not trigger underwriting hold");

        // Ingest large claim ($1200) -> cumulative $1700 / $2000 = 85% loss ratio -> trigger hold!
        var result2 = engine.ingestAndEvaluateFNOL(policyNum, "COLLISION", new BigDecimal("1200.00"), "Rear-end collision", writtenPrem);
        assertTrue(result2.updatedLossSummary().isUnderwritingHoldRequired(), "Loss ratio > 75% must trigger automated underwriting hold");
        assertTrue(result2.updatedLossSummary().getHoldReason().contains("Loss ratio"), "Hold reason should cite loss ratio threshold");
    }
}
