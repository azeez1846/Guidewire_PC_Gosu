package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProrationRefundEngine {
    private static final Logger LOGGER = Logger.getLogger(ProrationRefundEngine.class.getName());
    private static final ProrationRefundEngine instance = new ProrationRefundEngine();

    private ProrationRefundEngine() {}

    public static ProrationRefundEngine getInstance() {
        return instance;
    }

    public RefundCalculationResult calculateCancellationRefund(PolicyPeriod period, long daysInForce, long totalTermDays, boolean isInsuredInitiated) {
        RefundCalculationResult result = new RefundCalculationResult();
        if (period == null || period.getTotalPremium() == null) return result;

        BigDecimal totalPrem = period.getTotalPremium();
        if (totalTermDays <= 0) totalTermDays = 365;
        if (daysInForce < 0) daysInForce = 0;
        if (daysInForce > totalTermDays) daysInForce = totalTermDays;

        double proRataFactor = (double) daysInForce / (double) totalTermDays;
        BigDecimal earnedPrem = totalPrem.multiply(BigDecimal.valueOf(proRataFactor)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal proRataRefund = totalPrem.subtract(earnedPrem);

        result.setDaysInForce(daysInForce);
        result.setTotalTermDays(totalTermDays);
        result.setProRataFactor(proRataFactor);
        result.setEarnedPremium(earnedPrem);

        if (isInsuredInitiated) {
            // Apply Short-Rate Penalty Table (90% pro-rata refund factor)
            double shortRateRefundFactor = (1.0 - proRataFactor) * 0.90;
            BigDecimal shortRateRefund = totalPrem.multiply(BigDecimal.valueOf(shortRateRefundFactor)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal shortRateEarned = totalPrem.subtract(shortRateRefund);
            result.setCancellationType("Short-Rate (Insured-Initiated)");
            result.setRefundAmount(shortRateRefund);
            result.setEarnedPremium(shortRateEarned);
            result.setShortRatePenalty(proRataRefund.subtract(shortRateRefund));
        } else {
            result.setCancellationType("Pro-Rata (Carrier-Initiated)");
            result.setRefundAmount(proRataRefund);
            result.setShortRatePenalty(BigDecimal.ZERO);
        }

        LOGGER.log(Level.INFO, "Proration Refund calculated for policy {0}: Type={1}, Days={2}/{3}, Refund=${4}",
                new Object[]{period.getPolicyNumber(), result.getCancellationType(), daysInForce, totalTermDays, result.getRefundAmount()});

        return result;
    }

    public static class RefundCalculationResult {
        private String cancellationType;
        private long daysInForce;
        private long totalTermDays;
        private double proRataFactor;
        private BigDecimal earnedPremium = BigDecimal.ZERO;
        private BigDecimal refundAmount = BigDecimal.ZERO;
        private BigDecimal shortRatePenalty = BigDecimal.ZERO;

        public String getCancellationType() { return cancellationType; }
        public void setCancellationType(String cancellationType) { this.cancellationType = cancellationType; }

        public long getDaysInForce() { return daysInForce; }
        public void setDaysInForce(long daysInForce) { this.daysInForce = daysInForce; }

        public long getTotalTermDays() { return totalTermDays; }
        public void setTotalTermDays(long totalTermDays) { this.totalTermDays = totalTermDays; }

        public double getProRataFactor() { return proRataFactor; }
        public void setProRataFactor(double proRataFactor) { this.proRataFactor = proRataFactor; }

        public BigDecimal getEarnedPremium() { return earnedPremium; }
        public void setEarnedPremium(BigDecimal earnedPremium) { this.earnedPremium = earnedPremium; }

        public BigDecimal getRefundAmount() { return refundAmount; }
        public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

        public BigDecimal getShortRatePenalty() { return shortRatePenalty; }
        public void setShortRatePenalty(BigDecimal shortRatePenalty) { this.shortRatePenalty = shortRatePenalty; }
    }
}
