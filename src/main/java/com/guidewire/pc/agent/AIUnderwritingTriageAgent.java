package com.guidewire.pc.agent;

import com.guidewire.pc.service.ClaimCenterIntegrationEngine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Autonomous AI Underwriting Triage Agent using AGY Agentic Patterns.
 * Multi-agent orchestration for underwriting evaluation, risk scoring,
 * FNOL history inspection, and activity referral escalation.
 */
public class AIUnderwritingTriageAgent {
    private static final Logger LOGGER = Logger.getLogger(AIUnderwritingTriageAgent.class.getName());

    public static class TriageDecision {
        private final String submissionId;
        private final String recommendation; // STRAIGHT_THROUGH_BIND, UW_REFERRAL, DECLINE
        private final int riskScore; // 0-100
        private final List<String> rationale;
        private final boolean escalationRequired;

        public TriageDecision(String submissionId, String recommendation, int riskScore, List<String> rationale, boolean escalationRequired) {
            this.submissionId = submissionId;
            this.recommendation = recommendation;
            this.riskScore = riskScore;
            this.rationale = rationale;
            this.escalationRequired = escalationRequired;
        }

        public String getSubmissionId() { return submissionId; }
        public String getRecommendation() { return recommendation; }
        public int getRiskScore() { return riskScore; }
        public List<String> getRationale() { return rationale; }
        public boolean isEscalationRequired() { return escalationRequired; }
    }

    private final ClaimCenterIntegrationEngine claimEngine;

    public AIUnderwritingTriageAgent() {
        this.claimEngine = new ClaimCenterIntegrationEngine();
    }

    public AIUnderwritingTriageAgent(ClaimCenterIntegrationEngine claimEngine) {
        this.claimEngine = claimEngine;
    }

    public TriageDecision evaluateSubmission(String submissionId, String policyNumber, String lineOfBusiness, BigDecimal annualPremium, int driverScore, boolean highFloodZone) {
        LOGGER.log(Level.FINE, "→ AIUnderwritingTriageAgent.evaluateSubmission: submissionId={0}, policy={1}, lob={2}",
                new Object[]{submissionId, policyNumber, lineOfBusiness});
        List<String> rationale = new ArrayList<>();
        int riskScore = 20; // Base score

        // 1. Telematics & Driver Score Sub-agent Check
        if (driverScore < 60) {
            riskScore += 35;
            rationale.add("Low Telematics Driver Behavior Score (" + driverScore + "/100)");
        } else if (driverScore >= 85) {
            riskScore -= 10;
            rationale.add("Superior Telematics Driver Score (" + driverScore + "/100) applied discount");
        }

        // 2. Geospatial Flood Zone Sub-agent Check
        if (highFloodZone) {
            riskScore += 25;
            rationale.add("Property located in Zone A High Flood Exposure Region");
        }

        // 3. Claim History Sub-agent Check
        if (policyNumber != null) {
            ClaimCenterIntegrationEngine.PolicyLossSummary summary = claimEngine.evaluatePolicyLossSummary(policyNumber, annualPremium);
            if (summary.isUnderwritingHoldRequired()) {
                riskScore += 40;
                rationale.add("Underwriting Hold triggered by claim history: " + summary.getHoldReason());
            }
        }

        // 4. Decision Synthesis
        String recommendation;
        boolean escalationRequired = false;

        if (riskScore >= 75) {
            recommendation = "DECLINE";
            escalationRequired = true;
            rationale.add("Accumulated Risk Score (" + riskScore + ") exceeds maximum binding threshold (75)");
        } else if (riskScore >= 45) {
            recommendation = "UW_REFERRAL";
            escalationRequired = true;
            rationale.add("Accumulated Risk Score (" + riskScore + ") requires Senior Underwriter Manual Review");
        } else {
            recommendation = "STRAIGHT_THROUGH_BIND";
            rationale.add("Risk Score (" + riskScore + ") is within Straight-Through Processing bounds");
        }

        LOGGER.log(Level.INFO, "AI Underwriting Triage Decision for submission {0}: recommendation={1}, score={2}, escalationRequired={3}",
                new Object[]{submissionId, recommendation, riskScore, escalationRequired});

        return new TriageDecision(submissionId, recommendation, riskScore, rationale, escalationRequired);
    }
}
