package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiPayeeCommissionEngine {
    private static final Logger LOGGER = Logger.getLogger(MultiPayeeCommissionEngine.class.getName());
    private static final MultiPayeeCommissionEngine instance = new MultiPayeeCommissionEngine();

    private MultiPayeeCommissionEngine() {
        LOGGER.log(Level.FINE, "→ MultiPayeeCommissionEngine.MultiPayeeCommissionEngine");}

    public static MultiPayeeCommissionEngine getInstance() {
        LOGGER.log(Level.FINE, "→ MultiPayeeCommissionEngine.getInstance");
        return instance;
    }

    public BillingCommissionResult calculateMultiPayeeCommission(PolicyPeriod period, String primaryPayee, double primaryPayeeSplit, String secondaryPayee, BigDecimal annualAgencyVolume) {
        LOGGER.log(Level.FINE, "→ MultiPayeeCommissionEngine.calculateMultiPayeeCommission");
        BillingCommissionResult result = new BillingCommissionResult();
        if (period == null || period.getTotalPremium() == null) return result;

        BigDecimal totalPrem = period.getTotalPremium();
        result.setPolicyNumber(period.getPolicyNumber());
        result.setTotalPremium(totalPrem);

        if (primaryPayeeSplit <= 0 || primaryPayeeSplit > 1.0) primaryPayeeSplit = 0.60;
        double secondarySplit = 1.0 - primaryPayeeSplit;

        BigDecimal primaryAmount = totalPrem.multiply(BigDecimal.valueOf(primaryPayeeSplit)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal secondaryAmount = totalPrem.subtract(primaryAmount);

        List<PayeeSplit> payees = new ArrayList<>();
        payees.add(new PayeeSplit(primaryPayee != null ? primaryPayee : "Primary Named Insured", primaryPayeeSplit, primaryAmount));
        if (secondaryPayee != null && !secondaryPayee.trim().isEmpty()) {
            payees.add(new PayeeSplit(secondaryPayee, secondarySplit, secondaryAmount));
        }
        result.setPayeeSplits(payees);

        // Tiered Producer Commission Calculation
        double commissionRate = 0.10; // Default 10%
        if (annualAgencyVolume != null) {
            if (annualAgencyVolume.compareTo(new BigDecimal("500000.00")) > 0) {
                commissionRate = 0.15; // 15% Tier 3
            } else if (annualAgencyVolume.compareTo(new BigDecimal("100000.00")) >= 0) {
                commissionRate = 0.125; // 12.5% Tier 2
            }
        }

        BigDecimal commissionAmt = totalPrem.multiply(BigDecimal.valueOf(commissionRate)).setScale(2, RoundingMode.HALF_UP);
        result.setCommissionRate(commissionRate);
        result.setCommissionAmount(commissionAmt);

        LOGGER.log(Level.INFO, "Multi-Payee Commission calculated for policy {0}: Commission=${1} ({2}%), Payees={3}",
                new Object[]{period.getPolicyNumber(), commissionAmt, commissionRate * 100, payees.size()});

        return result;
    }

    public static class PayeeSplit {
        private final String payeeName;
        private final double splitPercentage;
        private final BigDecimal splitAmount;

        public PayeeSplit(String payeeName, double splitPercentage, BigDecimal splitAmount) {
        LOGGER.log(Level.FINE, "→ MultiPayeeCommissionEngine.PayeeSplit");
            this.payeeName = payeeName;
            this.splitPercentage = splitPercentage;
            this.splitAmount = splitAmount;
        }

        public String getPayeeName() {
        LOGGER.log(Level.FINE, "→ MultiPayeeCommissionEngine.getPayeeName"); return payeeName; }
        public double getSplitPercentage() {
        LOGGER.log(Level.FINE, "→ MultiPayeeCommissionEngine.getSplitPercentage"); return splitPercentage; }
        public BigDecimal getSplitAmount() {
        LOGGER.log(Level.FINE, "→ MultiPayeeCommissionEngine.getSplitAmount"); return splitAmount; }
    }

    public static class BillingCommissionResult {
        private String policyNumber;
        private BigDecimal totalPremium = BigDecimal.ZERO;
        private List<PayeeSplit> payeeSplits = new ArrayList<>();
        private double commissionRate;
        private BigDecimal commissionAmount = BigDecimal.ZERO;

        public String getPolicyNumber() {
        LOGGER.log(Level.FINE, "→ MultiPayeeCommissionEngine.getPolicyNumber"); return policyNumber; }
        public void setPolicyNumber(String policyNumber) {
        LOGGER.log(Level.FINE, "→ MultiPayeeCommissionEngine.setPolicyNumber"); this.policyNumber = policyNumber; }

        public BigDecimal getTotalPremium() {
        LOGGER.log(Level.FINE, "→ MultiPayeeCommissionEngine.getTotalPremium"); return totalPremium; }
        public void setTotalPremium(BigDecimal totalPremium) {
        LOGGER.log(Level.FINE, "→ MultiPayeeCommissionEngine.setTotalPremium"); this.totalPremium = totalPremium; }

        public List<PayeeSplit> getPayeeSplits() {
        LOGGER.log(Level.FINE, "→ MultiPayeeCommissionEngine.getPayeeSplits"); return payeeSplits; }
        public void setPayeeSplits(List<PayeeSplit> payeeSplits) {
        LOGGER.log(Level.FINE, "→ MultiPayeeCommissionEngine.setPayeeSplits"); this.payeeSplits = payeeSplits; }

        public double getCommissionRate() {
        LOGGER.log(Level.FINE, "→ MultiPayeeCommissionEngine.getCommissionRate"); return commissionRate; }
        public void setCommissionRate(double commissionRate) {
        LOGGER.log(Level.FINE, "→ MultiPayeeCommissionEngine.setCommissionRate"); this.commissionRate = commissionRate; }

        public BigDecimal getCommissionAmount() {
        LOGGER.log(Level.FINE, "→ MultiPayeeCommissionEngine.getCommissionAmount"); return commissionAmount; }
        public void setCommissionAmount(BigDecimal commissionAmount) {
        LOGGER.log(Level.FINE, "→ MultiPayeeCommissionEngine.setCommissionAmount"); this.commissionAmount = commissionAmount; }
    }
}
