package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SlidingScaleDividendEngine {
    private static final Logger LOGGER = Logger.getLogger(SlidingScaleDividendEngine.class.getName());
    private static final SlidingScaleDividendEngine instance = new SlidingScaleDividendEngine();

    private SlidingScaleDividendEngine() {}

    public static SlidingScaleDividendEngine getInstance() {
        return instance;
    }

    public DividendResult calculatePolicyholderDividend(PolicyPeriod period, BigDecimal annualIncurredLosses) {
        DividendResult result = new DividendResult();
        if (period == null || period.getTotalPremium() == null) return result;

        BigDecimal totalPrem = period.getTotalPremium();
        if (annualIncurredLosses == null) annualIncurredLosses = BigDecimal.ZERO;

        result.setPolicyNumber(period.getPolicyNumber());
        result.setTotalPremium(totalPrem);
        result.setAnnualIncurredLosses(annualIncurredLosses);

        double lossRatio = annualIncurredLosses.divide(totalPrem, 4, RoundingMode.HALF_UP).doubleValue();
        result.setLossRatio(lossRatio);

        double dividendPct = 0.0;
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

        public String getPolicyNumber() { return policyNumber; }
        public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

        public BigDecimal getTotalPremium() { return totalPremium; }
        public void setTotalPremium(BigDecimal totalPremium) { this.totalPremium = totalPremium; }

        public BigDecimal getAnnualIncurredLosses() { return annualIncurredLosses; }
        public void setAnnualIncurredLosses(BigDecimal annualIncurredLosses) { this.annualIncurredLosses = annualIncurredLosses; }

        public double getLossRatio() { return lossRatio; }
        public void setLossRatio(double lossRatio) { this.lossRatio = lossRatio; }

        public double getDividendPercentage() { return dividendPercentage; }
        public void setDividendPercentage(double dividendPercentage) { this.dividendPercentage = dividendPercentage; }

        public String getDividendTier() { return dividendTier; }
        public void setDividendTier(String dividendTier) { this.dividendTier = dividendTier; }

        public BigDecimal getDividendAmount() { return dividendAmount; }
        public void setDividendAmount(BigDecimal dividendAmount) { this.dividendAmount = dividendAmount; }

        public BigDecimal getNetPolicyCost() { return netPolicyCost; }
        public void setNetPolicyCost(BigDecimal netPolicyCost) { this.netPolicyCost = netPolicyCost; }
    }
}
