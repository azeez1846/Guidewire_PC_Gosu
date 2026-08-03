package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UWRatingOverrideEngine {
    private static final Logger LOGGER = Logger.getLogger(UWRatingOverrideEngine.class.getName());
    private static final UWRatingOverrideEngine instance = new UWRatingOverrideEngine();

    private UWRatingOverrideEngine() {}

    public static UWRatingOverrideEngine getInstance() {
        return instance;
    }

    public OverrideResult applyRatingOverride(PolicyPeriod period, double scheduleCreditPercentage, BigDecimal manualRateOverride, String underwriterUser, String overrideReason) {
        OverrideResult result = new OverrideResult();
        if (period == null || period.getTotalPremium() == null) return result;

        BigDecimal basePrem = period.getTotalPremium();
        result.setPolicyNumber(period.getPolicyNumber());
        result.setOriginalPremium(basePrem);
        result.setUnderwriterUser(underwriterUser);
        result.setOverrideReason(overrideReason);
        result.setScheduleCreditPercentage(scheduleCreditPercentage);

        // Limit schedule credit/debit to [-25%, +25%]
        if (scheduleCreditPercentage < -0.25) scheduleCreditPercentage = -0.25;
        if (scheduleCreditPercentage > 0.25) scheduleCreditPercentage = 0.25;

        BigDecimal scheduleAdjFactor = BigDecimal.valueOf(1.0 + scheduleCreditPercentage);
        BigDecimal adjustedPrem = basePrem.multiply(scheduleAdjFactor).setScale(2, RoundingMode.HALF_UP);

        if (manualRateOverride != null && manualRateOverride.compareTo(BigDecimal.ZERO) > 0) {
            adjustedPrem = manualRateOverride;
            result.setManualOverrideApplied(true);
        }

        BigDecimal delta = adjustedPrem.subtract(basePrem);
        result.setAdjustedPremium(adjustedPrem);
        result.setPremiumDelta(delta);

        LOGGER.log(Level.INFO, "UW Rating Override applied to policy {0} by {1}: ModPrem=${2} (Delta=${3}) Reason: {4}",
                new Object[]{period.getPolicyNumber(), underwriterUser, adjustedPrem, delta, overrideReason});

        return result;
    }

    public static class OverrideResult {
        private String policyNumber;
        private String underwriterUser;
        private String overrideReason;
        private double scheduleCreditPercentage;
        private boolean manualOverrideApplied;
        private BigDecimal originalPremium = BigDecimal.ZERO;
        private BigDecimal adjustedPremium = BigDecimal.ZERO;
        private BigDecimal premiumDelta = BigDecimal.ZERO;

        public String getPolicyNumber() { return policyNumber; }
        public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

        public String getUnderwriterUser() { return underwriterUser; }
        public void setUnderwriterUser(String underwriterUser) { this.underwriterUser = underwriterUser; }

        public String getOverrideReason() { return overrideReason; }
        public void setOverrideReason(String overrideReason) { this.overrideReason = overrideReason; }

        public double getScheduleCreditPercentage() { return scheduleCreditPercentage; }
        public void setScheduleCreditPercentage(double scheduleCreditPercentage) { this.scheduleCreditPercentage = scheduleCreditPercentage; }

        public boolean isManualOverrideApplied() { return manualOverrideApplied; }
        public void setManualOverrideApplied(boolean manualOverrideApplied) { this.manualOverrideApplied = manualOverrideApplied; }

        public BigDecimal getOriginalPremium() { return originalPremium; }
        public void setOriginalPremium(BigDecimal originalPremium) { this.originalPremium = originalPremium; }

        public BigDecimal getAdjustedPremium() { return adjustedPremium; }
        public void setAdjustedPremium(BigDecimal adjustedPremium) { this.adjustedPremium = adjustedPremium; }

        public BigDecimal getPremiumDelta() { return premiumDelta; }
        public void setPremiumDelta(BigDecimal premiumDelta) { this.premiumDelta = premiumDelta; }
    }
}
