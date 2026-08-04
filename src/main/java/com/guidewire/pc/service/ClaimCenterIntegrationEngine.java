package com.guidewire.pc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * ClaimCenter Integration Engine.
 * Manages First Notice of Loss (FNOL) ingestion, loss ratio evaluation,
 * and automated underwriting hold triggers for Guidewire PolicyCenter.
 */
public class ClaimCenterIntegrationEngine {

    public static class FNOLEvent {
        private String claimNumber;
        private String policyNumber;
        private LocalDate lossDate;
        private String claimType;
        private BigDecimal lossAmount;
        private String status; // OPEN, CLOSED, REOPENED
        private String description;

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
        private String policyNumber;
        private int totalClaims;
        private int openClaims;
        private BigDecimal totalIncurredLoss;
        private BigDecimal totalWrittenPremium;
        private BigDecimal lossRatioPercentage;
        private boolean underwritingHoldRequired;
        private String holdReason;

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

    private final Map<String, List<FNOLEvent>> claimsByPolicy = new HashMap<>();

    public FNOLEvent ingestFNOL(String policyNumber, String claimType, BigDecimal lossAmount, String description) {
        String claimNumber = "CLM-" + System.currentTimeMillis() % 1000000;
        FNOLEvent fnol = new FNOLEvent(claimNumber, policyNumber, LocalDate.now(), claimType, lossAmount, "OPEN", description);
        claimsByPolicy.computeIfAbsent(policyNumber, k -> new ArrayList<>()).add(fnol);
        return fnol;
    }

    public List<FNOLEvent> getClaimsForPolicy(String policyNumber) {
        return claimsByPolicy.getOrDefault(policyNumber, Collections.emptyList());
    }

    public PolicyLossSummary evaluatePolicyLossSummary(String policyNumber, BigDecimal writtenPremium) {
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

        return new PolicyLossSummary(policyNumber, totalClaims, openClaims, totalLoss, writtenPremium, lossRatio, holdRequired, reason.toString().trim());
    }
}
