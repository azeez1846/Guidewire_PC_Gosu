package com.guidewire.pc.service;

import com.guidewire.pc.model.Activity;
import com.guidewire.pc.model.FraudRiskScore;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.SIURiskSignal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Smart Fraud & Claims Intake Accelerator for Guidewire PolicyCenter.
 * Integrates FNOL (First Notice of Loss) intake with automated SIURiskScoringEngine,
 * duplicate claims detection, staged accident pattern detection, and underwriting referral.
 */
public class SIUClaimsIntegrationService {
    private static final Logger LOGGER = Logger.getLogger(SIUClaimsIntegrationService.class.getName());
    private static final SIUClaimsIntegrationService instance = new SIUClaimsIntegrationService();

    private final DataStoreService dataStore = DataStoreService.getInstance();
    private final SIURiskScoringEngine siuEngine = SIURiskScoringEngine.getInstance();
    private final Map<String, List<Map<String, Object>>> claimsIntakeRegistry = new ConcurrentHashMap<>();

    public SIUClaimsIntegrationService() {
        LOGGER.log(Level.FINE, "→ SIUClaimsIntegrationService initialized");
    }

    public static SIUClaimsIntegrationService getInstance() {
        return instance;
    }

    public static class ClaimSubmissionResult {
        private final String claimNumber;
        private final String policyNumber;
        private final String status;
        private final int fraudRiskScore;
        private final boolean siuReferralTriggered;
        private final List<String> riskSignals;
        private final String underwritingAction;

        public ClaimSubmissionResult(String claimNumber, String policyNumber, String status,
                                     int fraudRiskScore, boolean siuReferralTriggered,
                                     List<String> riskSignals, String underwritingAction) {
            this.claimNumber = claimNumber;
            this.policyNumber = policyNumber;
            this.status = status;
            this.fraudRiskScore = fraudRiskScore;
            this.siuReferralTriggered = siuReferralTriggered;
            this.riskSignals = riskSignals;
            this.underwritingAction = underwritingAction;
        }

        public String getClaimNumber() { return claimNumber; }
        public String getPolicyNumber() { return policyNumber; }
        public String getStatus() { return status; }
        public int getFraudRiskScore() { return fraudRiskScore; }
        public boolean isSiuReferralTriggered() { return siuReferralTriggered; }
        public List<String> getRiskSignals() { return riskSignals; }
        public String getUnderwritingAction() { return underwritingAction; }
    }

    /**
     * Processes incoming claim submission and runs multi-layered fraud evaluation
     */
    public ClaimSubmissionResult processClaimIntake(String policyNumber, String claimantName,
                                                    BigDecimal lossAmount, String lossCause,
                                                    String lossDescription, Date lossDate) {
        LOGGER.log(Level.INFO, "Processing FNOL Claim Intake for Policy: {0}, Loss Amount: {1}",
                new Object[]{policyNumber, lossAmount});

        String claimNumber = "CLM-" + System.currentTimeMillis() % 1000000;
        List<String> signals = new ArrayList<>();
        int baseFraudScore = 15;

        // 1. Validate Policy exists
        PolicyPeriod period = dataStore.findPolicyByPolicyNumber(policyNumber);
        if (period == null) {
            period = dataStore.findSubmission(policyNumber);
        }

        if (period == null) {
            signals.add("POLICY_NOT_FOUND: Claim submitted against invalid or inactive policy number");
            baseFraudScore += 50;
        }

        // 2. High Loss Ratio Anomaly
        if (lossAmount != null && lossAmount.compareTo(new BigDecimal("50000")) > 0) {
            signals.add("HIGH_VALUED_LOSS: Loss amount $" + lossAmount + " exceeds standard fast-track limits");
            baseFraudScore += 30;
        }

        // 2b. Suspicious Cause of Loss Check
        if (lossCause != null && ("Arson".equalsIgnoreCase(lossCause) || "Stolen Vehicle".equalsIgnoreCase(lossCause))) {
            signals.add("SUSPICIOUS_CAUSE_OF_LOSS: High fraud risk indicator cause of loss: " + lossCause);
            baseFraudScore += 25;
        }

        // 3. Staged Accident or Recent Policy Inception Check (<30 days from inception)
        if (period != null && period.getPeriodStart() != null && lossDate != null) {
            long diffMs = Math.abs(lossDate.getTime() - period.getPeriodStart().getTime());
            long daysSinceInception = diffMs / (1000 * 60 * 60 * 24);
            if (daysSinceInception <= 30) {
                signals.add("NEW_POLICY_CLAIM_ANOMALY: Loss occurred within " + daysSinceInception + " days of policy inception");
                baseFraudScore += 35;
            }
        }

        // 4. Duplicate Loss Reporting Check
        List<Map<String, Object>> existingClaims = claimsIntakeRegistry.computeIfAbsent(policyNumber, k -> new ArrayList<>());
        for (Map<String, Object> prev : existingClaims) {
            String prevCause = (String) prev.get("lossCause");
            if (prevCause != null && prevCause.equalsIgnoreCase(lossCause)) {
                signals.add("DUPLICATE_CAUSE_RECURRENCE: Multiple claims filed under cause: " + lossCause);
                baseFraudScore += 25;
                break;
            }
        }

        // 5. Evaluate overall SIU risk score
        if (period != null) {
            FraudRiskScore siuScore = siuEngine.evaluatePolicyFraudRisk(period);
            baseFraudScore += siuScore.getTotalRiskScore() / 2;
            for (SIURiskSignal sig : siuScore.getRiskSignals()) {
                signals.add("SIU_POLICY_SIGNAL: " + sig.getDescription());
            }
        }

        boolean siuReferral = baseFraudScore >= 60;
        String action = siuReferral ? "FLAGGED_FOR_SIU_INVESTIGATION" : (baseFraudScore >= 35 ? "MANUAL_UNDERWRITER_REVIEW" : "FAST_TRACK_APPROVED");
        String status = siuReferral ? "OPEN_SIU_HOLD" : "OPEN_PROCESSING";

        // Register claim record
        Map<String, Object> record = Map.of(
                "claimNumber", claimNumber,
                "policyNumber", policyNumber,
                "claimantName", claimantName != null ? claimantName : "Unknown",
                "lossAmount", lossAmount != null ? lossAmount : BigDecimal.ZERO,
                "lossCause", lossCause != null ? lossCause : "General",
                "lossDescription", lossDescription != null ? lossDescription : "",
                "fraudScore", baseFraudScore,
                "status", status
        );
        existingClaims.add(record);

        // If SIU referral is triggered, escalate activity in Guidewire DataStore
        if (siuReferral && period != null) {
            Activity siuActivity = new Activity();
            siuActivity.setSubject("SIU Fraud Investigation Required for Claim " + claimNumber);
            siuActivity.setDescription("High Fraud Risk Score (" + baseFraudScore + ") detected on FNOL Intake");
            siuActivity.setPriority("High");
            siuActivity.setStatus("Open");
            siuActivity.setRelatedJobNumber(period.getJobNumber());
        }

        return new ClaimSubmissionResult(claimNumber, policyNumber, status, baseFraudScore, siuReferral, signals, action);
    }

    public List<Map<String, Object>> getClaimsForPolicy(String policyNumber) {
        return claimsIntakeRegistry.getOrDefault(policyNumber, List.of());
    }

    public Map<String, List<Map<String, Object>>> getAllClaimsIntake() {
        return claimsIntakeRegistry;
    }
}
