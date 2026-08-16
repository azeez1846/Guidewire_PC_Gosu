package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommercialAuditEngine {
    private static final Logger LOGGER = Logger.getLogger(CommercialAuditEngine.class.getName());
    private static final CommercialAuditEngine instance = new CommercialAuditEngine();

    private CommercialAuditEngine() {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.CommercialAuditEngine");
    }

    public static CommercialAuditEngine getInstance() {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.getInstance");
        return instance;
    }

    public static class ClassCodeAuditExposure {
        public String classCode;
        public String description;
        public BigDecimal ratePer100;
        public BigDecimal estimatedExposure;
        public BigDecimal auditedExposure;
        public BigDecimal estimatedPremium;
        public BigDecimal auditedPremium;
        public BigDecimal premiumVariance;

        public ClassCodeAuditExposure(String classCode, String description, BigDecimal ratePer100, BigDecimal estimatedExposure, BigDecimal auditedExposure) {
            this.classCode = classCode;
            this.description = description;
            this.ratePer100 = ratePer100;
            this.estimatedExposure = estimatedExposure;
            this.auditedExposure = auditedExposure;

            BigDecimal estUnits = estimatedExposure.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);
            BigDecimal audUnits = auditedExposure.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);

            this.estimatedPremium = estUnits.multiply(ratePer100).setScale(2, RoundingMode.HALF_UP);
            this.auditedPremium = audUnits.multiply(ratePer100).setScale(2, RoundingMode.HALF_UP);
            this.premiumVariance = this.auditedPremium.subtract(this.estimatedPremium);
        }
    }

    public static class MultiClassAuditResult {
        public String policyNumber;
        public String auditType; // PHYSICAL_AUDIT, VOLUNTARY_AUDIT, PHONE_AUDIT
        public String status; // COMPLETED, DISPUTED, REVISED
        public List<ClassCodeAuditExposure> classCodeExposures = new ArrayList<>();
        public BigDecimal totalEstimatedPremium = BigDecimal.ZERO;
        public BigDecimal totalAuditedPremium = BigDecimal.ZERO;
        public BigDecimal stateAssessmentSurcharge = BigDecimal.ZERO; // e.g. 3.5%
        public BigDecimal finalEarnedPremium = BigDecimal.ZERO;
        public BigDecimal netAdjustmentAmount = BigDecimal.ZERO;
        public String adjustmentType; // ADDITIONAL_PREMIUM_DUE, RETURN_PREMIUM_REFUND, NO_CHANGE
        public String disputeReason;
    }

    public MultiClassAuditResult executeMultiClassAudit(String policyNumber, List<ClassCodeAuditExposure> exposures, double stateAssessmentPct) {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.executeMultiClassAudit");
        MultiClassAuditResult result = new MultiClassAuditResult();
        result.policyNumber = policyNumber != null ? policyNumber : "POL-WC-AUDIT-001";
        result.auditType = "PHYSICAL_AUDIT";
        result.status = "COMPLETED";
        result.classCodeExposures = exposures != null ? exposures : new ArrayList<>();

        for (ClassCodeAuditExposure exp : result.classCodeExposures) {
            result.totalEstimatedPremium = result.totalEstimatedPremium.add(exp.estimatedPremium);
            result.totalAuditedPremium = result.totalAuditedPremium.add(exp.auditedPremium);
        }

        BigDecimal assessRate = new BigDecimal(stateAssessmentPct / 100.0);
        result.stateAssessmentSurcharge = result.totalAuditedPremium.multiply(assessRate).setScale(2, RoundingMode.HALF_UP);
        result.finalEarnedPremium = result.totalAuditedPremium.add(result.stateAssessmentSurcharge);

        BigDecimal origTotalWithAssess = result.totalEstimatedPremium.multiply(BigDecimal.ONE.add(assessRate)).setScale(2, RoundingMode.HALF_UP);
        result.netAdjustmentAmount = result.finalEarnedPremium.subtract(origTotalWithAssess);

        if (result.netAdjustmentAmount.compareTo(BigDecimal.ZERO) > 0) {
            result.adjustmentType = "ADDITIONAL_PREMIUM_DUE";
        } else if (result.netAdjustmentAmount.compareTo(BigDecimal.ZERO) < 0) {
            result.adjustmentType = "RETURN_PREMIUM_REFUND";
        } else {
            result.adjustmentType = "NO_CHANGE";
        }

        return result;
    }

    public MultiClassAuditResult processAuditDispute(MultiClassAuditResult originalAudit, String disputeReason, BigDecimal revisedAuditedExposure) {
        LOGGER.log(Level.FINE, "→ CommercialAuditEngine.processAuditDispute");
        if (originalAudit == null) return new MultiClassAuditResult();

        originalAudit.status = "DISPUTED";
        originalAudit.disputeReason = disputeReason != null ? disputeReason : "Policyholder contested clerical vs fieldwork payroll allocation";

        if (revisedAuditedExposure != null && !originalAudit.classCodeExposures.isEmpty()) {
            ClassCodeAuditExposure first = originalAudit.classCodeExposures.get(0);
            first.auditedExposure = revisedAuditedExposure;
            BigDecimal audUnits = revisedAuditedExposure.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);
            first.auditedPremium = audUnits.multiply(first.ratePer100).setScale(2, RoundingMode.HALF_UP);
            first.premiumVariance = first.auditedPremium.subtract(first.estimatedPremium);

            // Recompute
            originalAudit.totalAuditedPremium = BigDecimal.ZERO;
            for (ClassCodeAuditExposure exp : originalAudit.classCodeExposures) {
                originalAudit.totalAuditedPremium = originalAudit.totalAuditedPremium.add(exp.auditedPremium);
            }
            originalAudit.status = "REVISED";
            originalAudit.finalEarnedPremium = originalAudit.totalAuditedPremium.add(originalAudit.stateAssessmentSurcharge);
            originalAudit.netAdjustmentAmount = originalAudit.finalEarnedPremium.subtract(originalAudit.totalEstimatedPremium);
        }

        return originalAudit;
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
            return policyNumber;
        }
        public void setPolicyNumber(String policyNumber) {
            this.policyNumber = policyNumber;
        }

        public String getAuditStatus() {
            return auditStatus;
        }
        public void setAuditStatus(String auditStatus) {
            this.auditStatus = auditStatus;
        }

        public BigDecimal getEstimatedExposure() {
            return estimatedExposure;
        }
        public void setEstimatedExposure(BigDecimal estimatedExposure) {
            this.estimatedExposure = estimatedExposure;
        }

        public BigDecimal getActualAuditedExposure() {
            return actualAuditedExposure;
        }
        public void setActualAuditedExposure(BigDecimal actualAuditedExposure) {
            this.actualAuditedExposure = actualAuditedExposure;
        }

        public BigDecimal getEstimatedPremium() {
            return estimatedPremium;
        }
        public void setEstimatedPremium(BigDecimal estimatedPremium) {
            this.estimatedPremium = estimatedPremium;
        }

        public BigDecimal getAuditedEarnedPremium() {
            return auditedEarnedPremium;
        }
        public void setAuditedEarnedPremium(BigDecimal auditedEarnedPremium) {
            this.auditedEarnedPremium = auditedEarnedPremium;
        }

        public BigDecimal getAuditAdjustmentAmount() {
            return auditAdjustmentAmount;
        }
        public void setAuditAdjustmentAmount(BigDecimal auditAdjustmentAmount) {
            this.auditAdjustmentAmount = auditAdjustmentAmount;
        }

        public BigDecimal getAncPenaltyCharge() {
            return ancPenaltyCharge;
        }
        public void setAncPenaltyCharge(BigDecimal ancPenaltyCharge) {
            this.ancPenaltyCharge = ancPenaltyCharge;
        }
    }
}
