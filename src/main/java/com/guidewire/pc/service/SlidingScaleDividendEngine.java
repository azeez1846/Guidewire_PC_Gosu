package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SlidingScaleDividendEngine {
    private static final Logger LOGGER = Logger.getLogger(SlidingScaleDividendEngine.class.getName());
    private static final SlidingScaleDividendEngine instance = new SlidingScaleDividendEngine();

    private SlidingScaleDividendEngine() {
        LOGGER.log(Level.FINE, "→ SlidingScaleDividendEngine.SlidingScaleDividendEngine");}

    public static SlidingScaleDividendEngine getInstance() {
        LOGGER.log(Level.FINE, "→ SlidingScaleDividendEngine.getInstance");
        return instance;
    }

    public DividendResult calculatePolicyholderDividend(PolicyPeriod period, BigDecimal annualIncurredLosses) {
        LOGGER.log(Level.FINE, "→ SlidingScaleDividendEngine.calculatePolicyholderDividend");
        DividendResult result = new DividendResult();
        if (period == null || period.getTotalPremium() == null) return result;

        BigDecimal totalPrem = period.getTotalPremium();
        if (annualIncurredLosses == null) annualIncurredLosses = BigDecimal.ZERO;

        result.setPolicyNumber(period.getPolicyNumber());
        result.setTotalPremium(totalPrem);
        result.setAnnualIncurredLosses(annualIncurredLosses);

        double lossRatio = annualIncurredLosses.divide(totalPrem, 4, RoundingMode.HALF_UP).doubleValue();
        result.setLossRatio(lossRatio);

        double dividendPct;
        String tierName;

        if (lossRatio < 0.30) {
            dividendPct = 0.15; // 15% dividend return
            tierName = "EXCELLENT_LOW_LOSS_TIER (15% Return)";
        } else if (lossRatio <= 0.50) {
            dividendPct = 0.08; // 8% dividend return
            tierName = "MODERATE_LOSS_TIER (8% Return)";
        } else {
            dividendPct = 0.0;
            tierName = "HIGH_LOSS_TIER (0% Return)";
        }

        BigDecimal dividendAmt = totalPrem.multiply(BigDecimal.valueOf(dividendPct)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netCost = totalPrem.subtract(dividendAmt);

        result.setDividendPercentage(dividendPct);
        result.setDividendTier(tierName);
        result.setDividendAmount(dividendAmt);
        result.setNetPolicyCost(netCost);

        LOGGER.log(Level.INFO, "Sliding Scale Dividend calculated for policy {0}: LossRatio={1}%, Dividend=${2} ({3})",
                new Object[]{period.getPolicyNumber(), lossRatio * 100, dividendAmt, tierName});

        return result;
    }

    public static class DividendResult {
        private String policyNumber;
        private BigDecimal totalPremium = BigDecimal.ZERO;
        private BigDecimal annualIncurredLosses = BigDecimal.ZERO;
        private double lossRatio;
        private double dividendPercentage;
        private String dividendTier;
        private BigDecimal dividendAmount = BigDecimal.ZERO;
        private BigDecimal netPolicyCost = BigDecimal.ZERO;

        public String getPolicyNumber() {
        LOGGER.log(Level.FINE, "→ SlidingScaleDividendEngine.getPolicyNumber"); return policyNumber; }
        public void setPolicyNumber(String policyNumber) {
        LOGGER.log(Level.FINE, "→ SlidingScaleDividendEngine.setPolicyNumber"); this.policyNumber = policyNumber; }

        public BigDecimal getTotalPremium() {
        LOGGER.log(Level.FINE, "→ SlidingScaleDividendEngine.getTotalPremium"); return totalPremium; }
        public void setTotalPremium(BigDecimal totalPremium) {
        LOGGER.log(Level.FINE, "→ SlidingScaleDividendEngine.setTotalPremium"); this.totalPremium = totalPremium; }

        public BigDecimal getAnnualIncurredLosses() {
        LOGGER.log(Level.FINE, "→ SlidingScaleDividendEngine.getAnnualIncurredLosses"); return annualIncurredLosses; }
        public void setAnnualIncurredLosses(BigDecimal annualIncurredLosses) {
        LOGGER.log(Level.FINE, "→ SlidingScaleDividendEngine.setAnnualIncurredLosses"); this.annualIncurredLosses = annualIncurredLosses; }

        public double getLossRatio() {
        LOGGER.log(Level.FINE, "→ SlidingScaleDividendEngine.getLossRatio"); return lossRatio; }
        public void setLossRatio(double lossRatio) {
        LOGGER.log(Level.FINE, "→ SlidingScaleDividendEngine.setLossRatio"); this.lossRatio = lossRatio; }

        public double getDividendPercentage() {
        LOGGER.log(Level.FINE, "→ SlidingScaleDividendEngine.getDividendPercentage"); return dividendPercentage; }
        public void setDividendPercentage(double dividendPercentage) {
        LOGGER.log(Level.FINE, "→ SlidingScaleDividendEngine.setDividendPercentage"); this.dividendPercentage = dividendPercentage; }

        public String getDividendTier() {
        LOGGER.log(Level.FINE, "→ SlidingScaleDividendEngine.getDividendTier"); return dividendTier; }
        public void setDividendTier(String dividendTier) {
        LOGGER.log(Level.FINE, "→ SlidingScaleDividendEngine.setDividendTier"); this.dividendTier = dividendTier; }

        public BigDecimal getDividendAmount() {
        LOGGER.log(Level.FINE, "→ SlidingScaleDividendEngine.getDividendAmount"); return dividendAmount; }
        public void setDividendAmount(BigDecimal dividendAmount) {
        LOGGER.log(Level.FINE, "→ SlidingScaleDividendEngine.setDividendAmount"); this.dividendAmount = dividendAmount; }

        public BigDecimal getNetPolicyCost() {
        LOGGER.log(Level.FINE, "→ SlidingScaleDividendEngine.getNetPolicyCost"); return netPolicyCost; }
        public void setNetPolicyCost(BigDecimal netPolicyCost) {
        LOGGER.log(Level.FINE, "→ SlidingScaleDividendEngine.setNetPolicyCost"); this.netPolicyCost = netPolicyCost; }
    }
}
