package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExperienceRatingEngine {
    private static final Logger LOGGER = Logger.getLogger(ExperienceRatingEngine.class.getName());
    private static final ExperienceRatingEngine instance = new ExperienceRatingEngine();

    private ExperienceRatingEngine() {}

    public static ExperienceRatingEngine getInstance() {
        return instance;
    }

    public EmodResult calculateExperienceMod(PolicyPeriod period, BigDecimal actual3YrLosses, BigDecimal expected3YrLosses) {
        EmodResult result = new EmodResult();
        if (period == null || period.getTotalPremium() == null) return result;

        BigDecimal basePrem = period.getTotalPremium();
        result.setPolicyNumber(period.getPolicyNumber());
        result.setBasePremium(basePrem);

        if (expected3YrLosses == null || expected3YrLosses.compareTo(BigDecimal.ZERO) <= 0) {
            expected3YrLosses = new BigDecimal("20000.00");
        }
        if (actual3YrLosses == null) {
            actual3YrLosses = BigDecimal.ZERO;
        }

        result.setActual3YrLosses(actual3YrLosses);
        result.setExpected3YrLosses(expected3YrLosses);

        double emodFactor = actual3YrLosses.divide(expected3YrLosses, 4, RoundingMode.HALF_UP).doubleValue();

        // Cap EMOD factor between [0.65, 1.85]
        if (emodFactor < 0.65) emodFactor = 0.65;
        if (emodFactor > 1.85) emodFactor = 1.85;

        BigDecimal emodModifiedPrem = basePrem.multiply(BigDecimal.valueOf(emodFactor)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal delta = emodModifiedPrem.subtract(basePrem);

        result.setEmodFactor(emodFactor);
        result.setEmodModifiedPremium(emodModifiedPrem);
        result.setPremiumAdjustment(delta);

        if (emodFactor < 1.0) {
            result.setModRatingTier("FAVORABLE_CREDIT");
        } else if (emodFactor > 1.0) {
            result.setModRatingTier("UNFAVORABLE_DEBIT");
        } else {
            result.setModRatingTier("NEUTRAL_STANDARD");
        }

        LOGGER.log(Level.INFO, "Experience Rating Mod evaluated for policy {0}: EMOD={1} ({2}), Adjusted Premium=${3}",
                new Object[]{period.getPolicyNumber(), emodFactor, result.getModRatingTier(), emodModifiedPrem});

        return result;
    }

    public static class EmodResult {
        private String policyNumber;
        private String modRatingTier;
        private double emodFactor;
        private BigDecimal actual3YrLosses = BigDecimal.ZERO;
        private BigDecimal expected3YrLosses = BigDecimal.ZERO;
        private BigDecimal basePremium = BigDecimal.ZERO;
        private BigDecimal emodModifiedPremium = BigDecimal.ZERO;
        private BigDecimal premiumAdjustment = BigDecimal.ZERO;

        public String getPolicyNumber() { return policyNumber; }
        public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

        public String getModRatingTier() { return modRatingTier; }
        public void setModRatingTier(String modRatingTier) { this.modRatingTier = modRatingTier; }

        public double getEmodFactor() { return emodFactor; }
        public void setEmodFactor(double emodFactor) { this.emodFactor = emodFactor; }

        public BigDecimal getActual3YrLosses() { return actual3YrLosses; }
        public void setActual3YrLosses(BigDecimal actual3YrLosses) { this.actual3YrLosses = actual3YrLosses; }

        public BigDecimal getExpected3YrLosses() { return expected3YrLosses; }
        public void setExpected3YrLosses(BigDecimal expected3YrLosses) { this.expected3YrLosses = expected3YrLosses; }

        public BigDecimal getBasePremium() { return basePremium; }
        public void setBasePremium(BigDecimal basePremium) { this.basePremium = basePremium; }

        public BigDecimal getEmodModifiedPremium() { return emodModifiedPremium; }
        public void setEmodModifiedPremium(BigDecimal emodModifiedPremium) { this.emodModifiedPremium = emodModifiedPremium; }

        public BigDecimal getPremiumAdjustment() { return premiumAdjustment; }
        public void setPremiumAdjustment(BigDecimal premiumAdjustment) { this.premiumAdjustment = premiumAdjustment; }
    }
}
