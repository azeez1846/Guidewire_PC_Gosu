package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommercialAuditEngine {
    private static final Logger LOGGER = Logger.getLogger(CommercialAuditEngine.class.getName());
    private static final CommercialAuditEngine instance = new CommercialAuditEngine();

    private CommercialAuditEngine() {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.CommercialAuditEngine");}

    public static CommercialAuditEngine getInstance() {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.getInstance");
        return instance;
    }

    public AuditResult processFinalAudit(PolicyPeriod period, BigDecimal actualAuditedExposure, BigDecimal estimatedExposure, boolean isNonCompliant) {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.processFinalAudit");
        AuditResult result = new AuditResult();
        if (period == null || period.getTotalPremium() == null) return result;

        BigDecimal estimatedPrem = period.getTotalPremium();
        result.setPolicyNumber(period.getPolicyNumber());
        result.setEstimatedExposure(estimatedExposure);
        result.setEstimatedPremium(estimatedPrem);

        if (isNonCompliant) {
            // Apply Audit Non-Compliance Charge (ANC - 200% Surcharge Penalty)
            BigDecimal ancPenalty = estimatedPrem.multiply(new BigDecimal("2.00")).setScale(2, RoundingMode.HALF_UP);
            result.setAuditStatus("NON_COMPLIANT");
            result.setAncPenaltyCharge(ancPenalty);
            result.setAuditedEarnedPremium(estimatedPrem.add(ancPenalty));
            result.setAuditAdjustmentAmount(ancPenalty);
        } else {
            result.setAuditStatus("COMPLETED");
            result.setActualAuditedExposure(actualAuditedExposure);

            if (estimatedExposure != null && estimatedExposure.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal exposureRatio = actualAuditedExposure.divide(estimatedExposure, 4, RoundingMode.HALF_UP);
                BigDecimal auditedEarned = estimatedPrem.multiply(exposureRatio).setScale(2, RoundingMode.HALF_UP);
                BigDecimal delta = auditedEarned.subtract(estimatedPrem);

                result.setAuditedEarnedPremium(auditedEarned);
                result.setAuditAdjustmentAmount(delta);
            } else {
                result.setAuditedEarnedPremium(estimatedPrem);
                result.setAuditAdjustmentAmount(BigDecimal.ZERO);
            }
        }

        LOGGER.log(Level.INFO, "Commercial Final Audit processed for policy {0}: Status={1}, AdjAmount=${2}",
                new Object[]{period.getPolicyNumber(), result.getAuditStatus(), result.getAuditAdjustmentAmount()});

        return result;
    }

    public static class AuditResult {
        private String policyNumber;
        private String auditStatus;
        private BigDecimal estimatedExposure = BigDecimal.ZERO;
        private BigDecimal actualAuditedExposure = BigDecimal.ZERO;
        private BigDecimal estimatedPremium = BigDecimal.ZERO;
        private BigDecimal auditedEarnedPremium = BigDecimal.ZERO;
        private BigDecimal auditAdjustmentAmount = BigDecimal.ZERO;
        private BigDecimal ancPenaltyCharge = BigDecimal.ZERO;

        public String getPolicyNumber() {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.getPolicyNumber"); return policyNumber; }
        public void setPolicyNumber(String policyNumber) {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.setPolicyNumber"); this.policyNumber = policyNumber; }

        public String getAuditStatus() {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.getAuditStatus"); return auditStatus; }
        public void setAuditStatus(String auditStatus) {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.setAuditStatus"); this.auditStatus = auditStatus; }

        public BigDecimal getEstimatedExposure() {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.getEstimatedExposure"); return estimatedExposure; }
        public void setEstimatedExposure(BigDecimal estimatedExposure) {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.setEstimatedExposure"); this.estimatedExposure = estimatedExposure; }

        public BigDecimal getActualAuditedExposure() {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.getActualAuditedExposure"); return actualAuditedExposure; }
        public void setActualAuditedExposure(BigDecimal actualAuditedExposure) {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.setActualAuditedExposure"); this.actualAuditedExposure = actualAuditedExposure; }

        public BigDecimal getEstimatedPremium() {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.getEstimatedPremium"); return estimatedPremium; }
        public void setEstimatedPremium(BigDecimal estimatedPremium) {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.setEstimatedPremium"); this.estimatedPremium = estimatedPremium; }

        public BigDecimal getAuditedEarnedPremium() {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.getAuditedEarnedPremium"); return auditedEarnedPremium; }
        public void setAuditedEarnedPremium(BigDecimal auditedEarnedPremium) {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.setAuditedEarnedPremium"); this.auditedEarnedPremium = auditedEarnedPremium; }

        public BigDecimal getAuditAdjustmentAmount() {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.getAuditAdjustmentAmount"); return auditAdjustmentAmount; }
        public void setAuditAdjustmentAmount(BigDecimal auditAdjustmentAmount) {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.setAuditAdjustmentAmount"); this.auditAdjustmentAmount = auditAdjustmentAmount; }

        public BigDecimal getAncPenaltyCharge() {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.getAncPenaltyCharge"); return ancPenaltyCharge; }
        public void setAncPenaltyCharge(BigDecimal ancPenaltyCharge) {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.setAncPenaltyCharge"); this.ancPenaltyCharge = ancPenaltyCharge; }
    }
}
