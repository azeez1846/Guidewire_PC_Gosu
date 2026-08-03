package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommercialAuditEngine {
    private static final Logger LOGGER = Logger.getLogger(CommercialAuditEngine.class.getName());
    private static final CommercialAuditEngine instance = new CommercialAuditEngine();

    private CommercialAuditEngine() {}

    public static CommercialAuditEngine getInstance() {
        return instance;
    }

    public AuditResult processFinalAudit(PolicyPeriod period, BigDecimal actualAuditedExposure, BigDecimal estimatedExposure, boolean isNonCompliant) {
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

        public String getPolicyNumber() { return policyNumber; }
        public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

        public String getAuditStatus() { return auditStatus; }
        public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }

        public BigDecimal getEstimatedExposure() { return estimatedExposure; }
        public void setEstimatedExposure(BigDecimal estimatedExposure) { this.estimatedExposure = estimatedExposure; }

        public BigDecimal getActualAuditedExposure() { return actualAuditedExposure; }
        public void setActualAuditedExposure(BigDecimal actualAuditedExposure) { this.actualAuditedExposure = actualAuditedExposure; }

        public BigDecimal getEstimatedPremium() { return estimatedPremium; }
        public void setEstimatedPremium(BigDecimal estimatedPremium) { this.estimatedPremium = estimatedPremium; }

        public BigDecimal getAuditedEarnedPremium() { return auditedEarnedPremium; }
        public void setAuditedEarnedPremium(BigDecimal auditedEarnedPremium) { this.auditedEarnedPremium = auditedEarnedPremium; }

        public BigDecimal getAuditAdjustmentAmount() { return auditAdjustmentAmount; }
        public void setAuditAdjustmentAmount(BigDecimal auditAdjustmentAmount) { this.auditAdjustmentAmount = auditAdjustmentAmount; }

        public BigDecimal getAncPenaltyCharge() { return ancPenaltyCharge; }
        public void setAncPenaltyCharge(BigDecimal ancPenaltyCharge) { this.ancPenaltyCharge = ancPenaltyCharge; }
    }
}
