package com.guidewire.pc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ClaimCenter Integration Engine.
 * Manages First Notice of Loss (FNOL) ingestion, loss ratio evaluation,
 * and automated underwriting hold triggers for Guidewire PolicyCenter.
 */
public class ClaimCenterIntegrationEngine {
    private static final Logger LOGGER = Logger.getLogger(ClaimCenterIntegrationEngine.class.getName());

    private static final ClaimCenterIntegrationEngine instance = new ClaimCenterIntegrationEngine();

    public static class FNOLEvent {
        private final String claimNumber;
        private final String policyNumber;
        private final LocalDate lossDate;
        private final String claimType;
        private final BigDecimal lossAmount;
        private final String status; // OPEN, CLOSED, REOPENED
        private final String description;

        public FNOLEvent(String claimNumber, String policyNumber, LocalDate lossDate, String claimType, BigDecimal lossAmount, String status, String description) {
            this.claimNumber = claimNumber;
            this.policyNumber = policyNumber;
            this.lossDate = lossDate;
            this.claimType = claimType;
            this.lossAmount = lossAmount;
            this.status = status;
            this.description = description;
        }

        public String getClaimNumber() { return claimNumber; }
        public String getPolicyNumber() { return policyNumber; }
        public LocalDate getLossDate() { return lossDate; }
        public String getClaimType() { return claimType; }
        public BigDecimal getLossAmount() { return lossAmount; }
        public String getStatus() { return status; }
        public String getDescription() { return description; }
    }

    public static class PolicyLossSummary {
        private final String policyNumber;
        private final int totalClaims;
        private final int openClaims;
        private final BigDecimal totalIncurredLoss;
        private final BigDecimal totalWrittenPremium;
        private final BigDecimal lossRatioPercentage;
        private final boolean underwritingHoldRequired;
        private final String holdReason;

        public PolicyLossSummary(String policyNumber, int totalClaims, int openClaims, BigDecimal totalIncurredLoss, BigDecimal totalWrittenPremium, BigDecimal lossRatioPercentage, boolean underwritingHoldRequired, String holdReason) {
            this.policyNumber = policyNumber;
            this.totalClaims = totalClaims;
            this.openClaims = openClaims;
            this.totalIncurredLoss = totalIncurredLoss;
            this.totalWrittenPremium = totalWrittenPremium;
            this.lossRatioPercentage = lossRatioPercentage;
            this.underwritingHoldRequired = underwritingHoldRequired;
            this.holdReason = holdReason;
        }

        public String getPolicyNumber() { return policyNumber; }
        public int getTotalClaims() { return totalClaims; }
        public int getOpenClaims() { return openClaims; }
        public BigDecimal getTotalIncurredLoss() { return totalIncurredLoss; }
        public BigDecimal getTotalWrittenPremium() { return totalWrittenPremium; }
        public BigDecimal getLossRatioPercentage() { return lossRatioPercentage; }
        public boolean isUnderwritingHoldRequired() { return underwritingHoldRequired; }
        public String getHoldReason() { return holdReason; }
    }

    public record FNOLEvaluationResult(FNOLEvent fnolEvent, PolicyLossSummary updatedLossSummary) {}

    private final Map<String, List<FNOLEvent>> claimsByPolicy = new HashMap<>();

    public ClaimCenterIntegrationEngine() {}

    public static ClaimCenterIntegrationEngine getInstance() {
        return instance;
    }

    public FNOLEvent ingestFNOL(String policyNumber, String claimType, BigDecimal lossAmount, String description) {
        LOGGER.log(Level.FINE, "→ ClaimCenterIntegrationEngine.ingestFNOL: policy={0}, type={1}, loss=${2}",
                new Object[]{policyNumber, claimType, lossAmount});
        String claimNumber = "CLM-" + System.currentTimeMillis() % 1000000;
        FNOLEvent fnol = new FNOLEvent(claimNumber, policyNumber, LocalDate.now(), claimType, lossAmount, "OPEN", description);
        claimsByPolicy.computeIfAbsent(policyNumber, k -> new ArrayList<>()).add(fnol);
        LOGGER.log(Level.INFO, "Ingested FNOL {0} for policy {1} (Amount=${2})",
                new Object[]{claimNumber, policyNumber, lossAmount});
        return fnol;
    }

    public FNOLEvaluationResult ingestAndEvaluateFNOL(String policyNumber, String claimType, BigDecimal lossAmount, String description, BigDecimal writtenPremium) {
        FNOLEvent fnol = ingestFNOL(policyNumber, claimType, lossAmount, description);
        BigDecimal premium = (writtenPremium != null && writtenPremium.compareTo(BigDecimal.ZERO) > 0) ? writtenPremium : new BigDecimal("2500.00");
        PolicyLossSummary summary = evaluatePolicyLossSummary(policyNumber, premium);
        return new FNOLEvaluationResult(fnol, summary);
    }

    public List<FNOLEvent> getClaimsForPolicy(String policyNumber) {
        return claimsByPolicy.getOrDefault(policyNumber, Collections.emptyList());
    }

    public PolicyLossSummary evaluatePolicyLossSummary(String policyNumber, BigDecimal writtenPremium) {
        LOGGER.log(Level.FINE, "→ ClaimCenterIntegrationEngine.evaluatePolicyLossSummary: policy={0}, prem=${1}",
                new Object[]{policyNumber, writtenPremium});
        List<FNOLEvent> claims = getClaimsForPolicy(policyNumber);
        int totalClaims = claims.size();
        int openClaims = 0;
        BigDecimal totalLoss = BigDecimal.ZERO;

        for (FNOLEvent c : claims) {
            if ("OPEN".equalsIgnoreCase(c.getStatus()) || "REOPENED".equalsIgnoreCase(c.getStatus())) {
                openClaims++;
            }
            totalLoss = totalLoss.add(c.getLossAmount());
        }

        BigDecimal lossRatio = BigDecimal.ZERO;
        if (writtenPremium != null && writtenPremium.compareTo(BigDecimal.ZERO) > 0) {
            lossRatio = totalLoss.multiply(new BigDecimal("100")).divide(writtenPremium, 2, RoundingMode.HALF_UP);
        }

        boolean holdRequired = false;
        StringBuilder reason = new StringBuilder();

        if (lossRatio.compareTo(new BigDecimal("75.00")) > 0) {
            holdRequired = true;
            reason.append("Loss ratio (").append(lossRatio).append("%) exceeds maximum threshold 75%. ");
        }
        if (openClaims >= 3) {
            holdRequired = true;
            reason.append("Multiple open claims (").append(openClaims).append(") detected. ");
        }

        if (holdRequired) {
            LOGGER.log(Level.WARNING, "Underwriting hold triggered for policy {0}: {1}",
                    new Object[]{policyNumber, reason.toString().trim()});
        }

        return new PolicyLossSummary(policyNumber, totalClaims, openClaims, totalLoss, writtenPremium, lossRatio, holdRequired, reason.toString().trim());
    }
}
