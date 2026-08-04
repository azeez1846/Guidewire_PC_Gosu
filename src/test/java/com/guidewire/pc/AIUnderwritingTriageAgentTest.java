package com.guidewire.pc;

import com.guidewire.pc.agent.AIUnderwritingTriageAgent;
import com.guidewire.pc.agent.AIUnderwritingTriageAgent.TriageDecision;
import com.guidewire.pc.service.ClaimCenterIntegrationEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class AIUnderwritingTriageAgentTest {

    @Test
    @DisplayName("Test Straight-Through Processing (STP) Binding Decision")
    public void testStraightThroughBind() {
        AIUnderwritingTriageAgent agent = new AIUnderwritingTriageAgent();
        TriageDecision decision = agent.evaluateSubmission("SUB-001", "POL-100", "PersonalAuto", new BigDecimal("1500.00"), 90, false);

        assertEquals("STRAIGHT_THROUGH_BIND", decision.getRecommendation());
        assertFalse(decision.isEscalationRequired());
        assertTrue(decision.getRiskScore() < 45);
    }

    @Test
    @DisplayName("Test Underwriting Referral Escalation for High Flood Zone")
    public void testUnderwritingReferral() {
        AIUnderwritingTriageAgent agent = new AIUnderwritingTriageAgent();
        TriageDecision decision = agent.evaluateSubmission("SUB-002", "POL-200", "CommercialAuto", new BigDecimal("3500.00"), 70, true);

        assertEquals("UW_REFERRAL", decision.getRecommendation());
        assertTrue(decision.isEscalationRequired());
        assertTrue(decision.getRationale().stream().anyMatch(r -> r.contains("Zone A High Flood Exposure")));
    }

    @Test
    @DisplayName("Test Hard Decline Decision when Loss Ratio & Claims Hold Present")
    public void testHardDecline() {
        ClaimCenterIntegrationEngine claimsEngine = new ClaimCenterIntegrationEngine();
        String polNum = "POL-300";
        BigDecimal premium = new BigDecimal("2000.00");
        claimsEngine.ingestFNOL(polNum, "FIRE", new BigDecimal("1800.00"), "Severe structure fire");

        AIUnderwritingTriageAgent agent = new AIUnderwritingTriageAgent(claimsEngine);
        TriageDecision decision = agent.evaluateSubmission("SUB-003", polNum, "CommercialProperty", premium, 40, true);

        assertEquals("DECLINE", decision.getRecommendation());
        assertTrue(decision.isEscalationRequired());
        assertTrue(decision.getRiskScore() >= 75);
    }
}
