package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RateImpactCappingEngine {
    private static final Logger LOGGER = Logger.getLogger(RateImpactCappingEngine.class.getName());
    private static final RateImpactCappingEngine instance = new RateImpactCappingEngine();

    private RateImpactCappingEngine() {}

    public static RateImpactCappingEngine getInstance() {
        return instance;
    }

    public RateCapResult applyRenewalRateCap(PolicyPeriod priorTermPeriod, BigDecimal uncappedProposedPremium, double maxRateCapPercentage) {
        RateCapResult result = new RateCapResult();
        if (priorTermPeriod == null || priorTermPeriod.getTotalPremium() == null) return result;

        BigDecimal priorPrem = priorTermPeriod.getTotalPremium();
        if (uncappedProposedPremium == null) uncappedProposedPremium = priorPrem.multiply(new BigDecimal("1.25")).setScale(2, RoundingMode.HALF_UP);
        if (maxRateCapPercentage <= 0) maxRateCapPercentage = 0.10; // Default 10% max renewal increase cap

        result.setPolicyNumber(priorTermPeriod.getPolicyNumber());
        result.setPriorTermPremium(priorPrem);
        result.setUncappedProposedPremium(uncappedProposedPremium);
        result.setMaxRateCapPercentage(maxRateCapPercentage);

        BigDecimal uncappedIncrease = uncappedProposedPremium.subtract(priorPrem);
        double uncappedIncreasePct = uncappedIncrease.divide(priorPrem, 4, RoundingMode.HALF_UP).doubleValue();
        result.setUncappedIncreasePercentage(uncappedIncreasePct);

        if (uncappedIncreasePct > maxRateCapPercentage) {
            result.setRateCapApplied(true);
            BigDecimal maxAllowedIncrease = priorPrem.multiply(BigDecimal.valueOf(maxRateCapPercentage)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal cappedPremium = priorPrem.add(maxAllowedIncrease);
            BigDecimal carrierSubsidy = uncappedProposedPremium.subtract(cappedPremium);

            result.setCappedRenewalPremium(cappedPremium);
            result.setCarrierSubsidyAmount(carrierSubsidy);
        } else {
            result.setRateCapApplied(false);
            result.setCappedRenewalPremium(uncappedProposedPremium);
            result.setCarrierSubsidyAmount(BigDecimal.ZERO);
        }

        LOGGER.log(Level.INFO, "Renewal Rate Impact Cap applied for policy {0}: Prior=${1}, Uncapped=${2}, Capped=${3}, CapApplied={4}",
                new Object[]{priorTermPeriod.getPolicyNumber(), priorPrem, uncappedProposedPremium, result.getCappedRenewalPremium(), result.isRateCapApplied()});

        return result;
    }

    public static class RateCapResult {
        private String policyNumber;
        private BigDecimal priorTermPremium = BigDecimal.ZERO;
        private BigDecimal uncappedProposedPremium = BigDecimal.ZERO;
        private double maxRateCapPercentage;
        private double uncappedIncreasePercentage;
        private boolean rateCapApplied;
        private BigDecimal cappedRenewalPremium = BigDecimal.ZERO;
        private BigDecimal carrierSubsidyAmount = BigDecimal.ZERO;

        public String getPolicyNumber() { return policyNumber; }
        public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

        public BigDecimal getPriorTermPremium() { return priorTermPremium; }
        public void setPriorTermPremium(BigDecimal priorTermPremium) { this.priorTermPremium = priorTermPremium; }

        public BigDecimal getUncappedProposedPremium() { return uncappedProposedPremium; }
        public void setUncappedProposedPremium(BigDecimal uncappedProposedPremium) { this.uncappedProposedPremium = uncappedProposedPremium; }

        public double getMaxRateCapPercentage() { return maxRateCapPercentage; }
        public void setMaxRateCapPercentage(double maxRateCapPercentage) { this.maxRateCapPercentage = maxRateCapPercentage; }

        public double getUncappedIncreasePercentage() { return uncappedIncreasePercentage; }
        public void setUncappedIncreasePercentage(double uncappedIncreasePercentage) { this.uncappedIncreasePercentage = uncappedIncreasePercentage; }

        public boolean isRateCapApplied() { return rateCapApplied; }
        public void setRateCapApplied(boolean rateCapApplied) { this.rateCapApplied = rateCapApplied; }

        public BigDecimal getCappedRenewalPremium() { return cappedRenewalPremium; }
        public void setCappedRenewalPremium(BigDecimal cappedRenewalPremium) { this.cappedRenewalPremium = cappedRenewalPremium; }

        public BigDecimal getCarrierSubsidyAmount() { return carrierSubsidyAmount; }
        public void setCarrierSubsidyAmount(BigDecimal carrierSubsidyAmount) { this.carrierSubsidyAmount = carrierSubsidyAmount; }
    }
}
