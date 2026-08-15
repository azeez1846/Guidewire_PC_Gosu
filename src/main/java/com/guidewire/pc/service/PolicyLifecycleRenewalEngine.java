package com.guidewire.pc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Policy Lifecycle Renewal & Mid-Term Endorsement Engine.
 * Evaluates automated policy renewal eligibility, applies inflation adjustments,
 * and handles pro-rata mid-term endorsement premium calculations.
 */
public class PolicyLifecycleRenewalEngine {
    private static final Logger LOGGER = Logger.getLogger(PolicyLifecycleRenewalEngine.class.getName());

    public static class RenewalResult {
        private final String policyNumber;
        private final String renewalJobNumber;
        private final boolean eligible;
        private final BigDecimal previousPremium;
        private final BigDecimal newRenewalPremium;
        private final BigDecimal inflationRatePercentage;
        private final String statusMessage;

        public RenewalResult(String policyNumber, String renewalJobNumber, boolean eligible, BigDecimal previousPremium, BigDecimal newRenewalPremium, BigDecimal inflationRatePercentage, String statusMessage) {
            this.policyNumber = policyNumber;
            this.renewalJobNumber = renewalJobNumber;
            this.eligible = eligible;
            this.previousPremium = previousPremium;
            this.newRenewalPremium = newRenewalPremium;
            this.inflationRatePercentage = inflationRatePercentage;
            this.statusMessage = statusMessage;
        }

        public String getPolicyNumber() { return policyNumber; }
        public String getRenewalJobNumber() { return renewalJobNumber; }
        public boolean isEligible() { return eligible; }
        public BigDecimal getPreviousPremium() { return previousPremium; }
        public BigDecimal getNewRenewalPremium() { return newRenewalPremium; }
        public BigDecimal getInflationRatePercentage() { return inflationRatePercentage; }
        public String getStatusMessage() { return statusMessage; }
    }

    public static class MTACalculationResult {
        private final String policyNumber;
        private final BigDecimal originalAnnualPremium;
        private final BigDecimal newAnnualPremium;
        private final long totalPolicyDays;
        private final long remainingDays;
        private final BigDecimal proRataFactor;
        private final BigDecimal proratedAdditionalPremium;

        public MTACalculationResult(String policyNumber, BigDecimal originalAnnualPremium, BigDecimal newAnnualPremium, long totalPolicyDays, long remainingDays, BigDecimal proRataFactor, BigDecimal proratedAdditionalPremium) {
            this.policyNumber = policyNumber;
            this.originalAnnualPremium = originalAnnualPremium;
            this.newAnnualPremium = newAnnualPremium;
            this.totalPolicyDays = totalPolicyDays;
            this.remainingDays = remainingDays;
            this.proRataFactor = proRataFactor;
            this.proratedAdditionalPremium = proratedAdditionalPremium;
        }

        public String getPolicyNumber() { return policyNumber; }
        public BigDecimal getOriginalAnnualPremium() { return originalAnnualPremium; }
        public BigDecimal getNewAnnualPremium() { return newAnnualPremium; }
        public long getTotalPolicyDays() { return totalPolicyDays; }
        public long getRemainingDays() { return remainingDays; }
        public BigDecimal getProRataFactor() { return proRataFactor; }
        public BigDecimal getProratedAdditionalPremium() { return proratedAdditionalPremium; }
    }

    public RenewalResult evaluateAndCreateRenewal(String policyNumber, BigDecimal currentPremium, ClaimCenterIntegrationEngine.PolicyLossSummary lossSummary, BigDecimal baseInflationPercent) {
        LOGGER.log(Level.FINE, "→ PolicyLifecycleRenewalEngine.evaluateAndCreateRenewal: policy={0}, currentPrem={1}, inflation={2}%",
                new Object[]{policyNumber, currentPremium, baseInflationPercent});
        String renewalJobNumber = "REN-" + System.currentTimeMillis() % 1000000;

        if (lossSummary != null && lossSummary.isUnderwritingHoldRequired()) {
            LOGGER.log(Level.WARNING, "Renewal ineligible for policy {0} due to UW hold: {1}",
                    new Object[]{policyNumber, lossSummary.getHoldReason()});
            return new RenewalResult(
                policyNumber,
                renewalJobNumber,
                false,
                currentPremium,
                currentPremium,
                baseInflationPercent,
                "Ineligible for automated renewal due to underwriting hold: " + lossSummary.getHoldReason()
            );
        }

        BigDecimal factor = BigDecimal.ONE.add(baseInflationPercent.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        BigDecimal newPremium = currentPremium.multiply(factor).setScale(2, RoundingMode.HALF_UP);

        LOGGER.log(Level.INFO, "Renewal created for policy {0}: job={1}, newPrem=${2}",
                new Object[]{policyNumber, renewalJobNumber, newPremium});

        return new RenewalResult(
            policyNumber,
            renewalJobNumber,
            true,
            currentPremium,
            newPremium,
            baseInflationPercent,
            "Policy eligible for automated renewal with " + baseInflationPercent + "% rate adjustment."
        );
    }

    public MTACalculationResult calculateMidTermEndorsement(String policyNumber, BigDecimal currentAnnualPremium, BigDecimal updatedAnnualPremium, LocalDate policyEffectiveDate, LocalDate policyExpirationDate, LocalDate endorsementEffectiveDate) {
        LOGGER.log(Level.FINE, "→ PolicyLifecycleRenewalEngine.calculateMidTermEndorsement: policy={0}, currPrem={1}, updatedPrem={2}",
                new Object[]{policyNumber, currentAnnualPremium, updatedAnnualPremium});
        long totalDays = ChronoUnit.DAYS.between(policyEffectiveDate, policyExpirationDate);
        if (totalDays <= 0) totalDays = 365;

        long remainingDays = ChronoUnit.DAYS.between(endorsementEffectiveDate, policyExpirationDate);
        if (remainingDays < 0) remainingDays = 0;
        if (remainingDays > totalDays) remainingDays = totalDays;

        BigDecimal proRataFactor = new BigDecimal(remainingDays).divide(new BigDecimal(totalDays), 6, RoundingMode.HALF_UP);
        BigDecimal fullDelta = updatedAnnualPremium.subtract(currentAnnualPremium);
        BigDecimal proratedDelta = fullDelta.multiply(proRataFactor).setScale(2, RoundingMode.HALF_UP);

        LOGGER.log(Level.INFO, "Mid-term endorsement calculated for policy {0}: remainingDays={1}/{2}, proratedDelta=${3}",
                new Object[]{policyNumber, remainingDays, totalDays, proratedDelta});

        return new MTACalculationResult(
            policyNumber,
            currentAnnualPremium,
            updatedAnnualPremium,
            totalDays,
            remainingDays,
            proRataFactor,
            proratedDelta
        );
    }
}
