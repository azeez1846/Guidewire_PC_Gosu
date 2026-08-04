package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeductibleBuybackEngine {
    private static final Logger LOGGER = Logger.getLogger(DeductibleBuybackEngine.class.getName());
    private static final DeductibleBuybackEngine instance = new DeductibleBuybackEngine();

    private DeductibleBuybackEngine() {
        LOGGER.log(Level.FINE, "→ DeductibleBuybackEngine.DeductibleBuybackEngine");}

    public static DeductibleBuybackEngine getInstance() {
        LOGGER.log(Level.FINE, "→ DeductibleBuybackEngine.getInstance");
        return instance;
    }

    public BuybackResult calculateDeductibleBuyback(PolicyPeriod period, BigDecimal originalDeductible, BigDecimal targetDeductible) {
        LOGGER.log(Level.FINE, "→ DeductibleBuybackEngine.calculateDeductibleBuyback");
        BuybackResult result = new BuybackResult();
        if (period == null || period.getTotalPremium() == null) return result;

        if (originalDeductible == null || originalDeductible.compareTo(BigDecimal.ZERO) <= 0) {
            originalDeductible = new BigDecimal("10000.00");
        }
        if (targetDeductible == null || targetDeductible.compareTo(BigDecimal.ZERO) <= 0) {
            targetDeductible = new BigDecimal("1000.00");
        }

        BigDecimal basePrem = period.getTotalPremium();
        result.setPolicyNumber(period.getPolicyNumber());
        result.setBasePremium(basePrem);
        result.setOriginalDeductible(originalDeductible);
        result.setTargetDeductible(targetDeductible);

        if (targetDeductible.compareTo(originalDeductible) >= 0) {
            result.setBuybackSurchargePct(0.0);
            result.setSurchargeAmount(BigDecimal.ZERO);
            result.setRevisedTotalPremium(basePrem);
            return result;
        }

        BigDecimal reductionAmount = originalDeductible.subtract(targetDeductible);
        double reductionRatio = reductionAmount.divide(originalDeductible, 4, RoundingMode.HALF_UP).doubleValue();
        double surchargeFactor = reductionRatio * 0.20; // 20% max surcharge for 100% buyback

        BigDecimal surchargeAmt = basePrem.multiply(BigDecimal.valueOf(surchargeFactor)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal revisedTotal = basePrem.add(surchargeAmt);

        result.setBuybackSurchargePct(surchargeFactor);
        result.setSurchargeAmount(surchargeAmt);
        result.setRevisedTotalPremium(revisedTotal);

        LOGGER.log(Level.INFO, "Deductible Buyback calculated for policy {0}: ${1} -> ${2}, Surcharge=${3} ({4}%)",
                new Object[]{period.getPolicyNumber(), originalDeductible, targetDeductible, surchargeAmt, surchargeFactor * 100});

        return result;
    }

    public static class BuybackResult {
        private String policyNumber;
        private BigDecimal originalDeductible = BigDecimal.ZERO;
        private BigDecimal targetDeductible = BigDecimal.ZERO;
        private double buybackSurchargePct;
        private BigDecimal basePremium = BigDecimal.ZERO;
        private BigDecimal surchargeAmount = BigDecimal.ZERO;
        private BigDecimal revisedTotalPremium = BigDecimal.ZERO;

        public String getPolicyNumber() {
        LOGGER.log(Level.FINE, "→ DeductibleBuybackEngine.getPolicyNumber"); return policyNumber; }
        public void setPolicyNumber(String policyNumber) {
        LOGGER.log(Level.FINE, "→ DeductibleBuybackEngine.setPolicyNumber"); this.policyNumber = policyNumber; }

        public BigDecimal getOriginalDeductible() {
        LOGGER.log(Level.FINE, "→ DeductibleBuybackEngine.getOriginalDeductible"); return originalDeductible; }
        public void setOriginalDeductible(BigDecimal originalDeductible) {
        LOGGER.log(Level.FINE, "→ DeductibleBuybackEngine.setOriginalDeductible"); this.originalDeductible = originalDeductible; }

        public BigDecimal getTargetDeductible() {
        LOGGER.log(Level.FINE, "→ DeductibleBuybackEngine.getTargetDeductible"); return targetDeductible; }
        public void setTargetDeductible(BigDecimal targetDeductible) {
        LOGGER.log(Level.FINE, "→ DeductibleBuybackEngine.setTargetDeductible"); this.targetDeductible = targetDeductible; }

        public double getBuybackSurchargePct() {
        LOGGER.log(Level.FINE, "→ DeductibleBuybackEngine.getBuybackSurchargePct"); return buybackSurchargePct; }
        public void setBuybackSurchargePct(double buybackSurchargePct) {
        LOGGER.log(Level.FINE, "→ DeductibleBuybackEngine.setBuybackSurchargePct"); this.buybackSurchargePct = buybackSurchargePct; }

        public BigDecimal getBasePremium() {
        LOGGER.log(Level.FINE, "→ DeductibleBuybackEngine.getBasePremium"); return basePremium; }
        public void setBasePremium(BigDecimal basePremium) {
        LOGGER.log(Level.FINE, "→ DeductibleBuybackEngine.setBasePremium"); this.basePremium = basePremium; }

        public BigDecimal getSurchargeAmount() {
        LOGGER.log(Level.FINE, "→ DeductibleBuybackEngine.getSurchargeAmount"); return surchargeAmount; }
        public void setSurchargeAmount(BigDecimal surchargeAmount) {
        LOGGER.log(Level.FINE, "→ DeductibleBuybackEngine.setSurchargeAmount"); this.surchargeAmount = surchargeAmount; }

        public BigDecimal getRevisedTotalPremium() {
        LOGGER.log(Level.FINE, "→ DeductibleBuybackEngine.getRevisedTotalPremium"); return revisedTotalPremium; }
        public void setRevisedTotalPremium(BigDecimal revisedTotalPremium) {
        LOGGER.log(Level.FINE, "→ DeductibleBuybackEngine.setRevisedTotalPremium"); this.revisedTotalPremium = revisedTotalPremium; }
    }
}
