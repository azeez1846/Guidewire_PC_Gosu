package com.guidewire.pc;

import com.guidewire.pc.service.ClaimCenterIntegrationEngine;
import com.guidewire.pc.service.ClaimCenterIntegrationEngine.FNOLEvent;
import com.guidewire.pc.service.ClaimCenterIntegrationEngine.PolicyLossSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ClaimCenterIntegrationEngineTest {

    @Test
    @DisplayName("Test FNOL Ingestion and Policy Loss Summary Evaluation")
    public void testFNOLIngestionAndLossRatio() {
        ClaimCenterIntegrationEngine engine = new ClaimCenterIntegrationEngine();
        String policyNumber = "POL-TEST-CLM-100";
        BigDecimal writtenPremium = new BigDecimal("10000.00");

        // 1. Ingest FNOL #1
        FNOLEvent claim1 = engine.ingestFNOL(policyNumber, "COLLISION", new BigDecimal("3000.00"), "Rear-end collision at intersection");
        assertNotNull(claim1.getClaimNumber());
        assertEquals("OPEN", claim1.getStatus());

        // Initial summary check (30% loss ratio)
        PolicyLossSummary summary1 = engine.evaluatePolicyLossSummary(policyNumber, writtenPremium);
        assertEquals(1, summary1.getTotalClaims());
        assertEquals(1, summary1.getOpenClaims());
        assertEquals(new BigDecimal("3000.00"), summary1.getTotalIncurredLoss());
        assertEquals(new BigDecimal("30.00"), summary1.getLossRatioPercentage());
        assertFalse(summary1.isUnderwritingHoldRequired());

        // 2. Ingest FNOL #2 and #3 to breach threshold
        engine.ingestFNOL(policyNumber, "PROPERTY_DAMAGE", new BigDecimal("5000.00"), "Windstorm roof damage");
        engine.ingestFNOL(policyNumber, "THEFT", new BigDecimal("1000.00"), "Vehicle catalytic converter stolen");

        List<FNOLEvent> allClaims = engine.getClaimsForPolicy(policyNumber);
        assertEquals(3, allClaims.size());

        // Final summary check (90% loss ratio + 3 open claims -> Underwriting Hold required)
        PolicyLossSummary summary2 = engine.evaluatePolicyLossSummary(policyNumber, writtenPremium);
        assertEquals(3, summary2.getTotalClaims());
        assertEquals(3, summary2.getOpenClaims());
        assertEquals(new BigDecimal("9000.00"), summary2.getTotalIncurredLoss());
        assertEquals(new BigDecimal("90.00"), summary2.getLossRatioPercentage());
        assertTrue(summary2.isUnderwritingHoldRequired());
        assertTrue(summary2.getHoldReason().contains("Loss ratio (90.00%) exceeds maximum threshold 75%"));
        assertTrue(summary2.getHoldReason().contains("Multiple open claims (3) detected"));
    }
}
