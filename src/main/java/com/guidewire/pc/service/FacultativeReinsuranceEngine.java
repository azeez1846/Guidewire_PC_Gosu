package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Advanced Reinsurance Cession & Facultative Placement Engine.
 * Handles Quota Share, Surplus Treaties, Excess of Loss (XOL), and Facultative Certificates.
 */
public class FacultativeReinsuranceEngine {
    private static final Logger LOGGER = Logger.getLogger(FacultativeReinsuranceEngine.class.getName());
    private static final FacultativeReinsuranceEngine instance = new FacultativeReinsuranceEngine();

    // Default Retention Thresholds
    private final BigDecimal STANDARD_RETENTION_CAP = new BigDecimal("2000000.00"); // $2.0M Primary Retention
    private final BigDecimal SURPLUS_TREATY_CAP = new BigDecimal("5000000.00");    // $5.0M Surplus Treaty Limit

    private FacultativeReinsuranceEngine() {
        LOGGER.log(Level.FINE, "FacultativeReinsuranceEngine initialized");
    }

    public static FacultativeReinsuranceEngine getInstance() {
        return instance;
    }

    public ReinsuranceAllocationResult calculateCession(PolicyPeriod period, BigDecimal totalInsuredValue, BigDecimal quotaShareSharePct) {
        if (totalInsuredValue == null) {
            totalInsuredValue = new BigDecimal("10000000.00");
        }
        if (quotaShareSharePct == null) {
            quotaShareSharePct = new BigDecimal("0.20"); // 20% Quota Share Cession
        }

        ReinsuranceAllocationResult result = new ReinsuranceAllocationResult();
        result.setPolicyNumber(period != null ? period.getPolicyNumber() : "POL-RE-8001");
        result.setTotalInsuredValue(totalInsuredValue);
        result.setQuotaSharePct(quotaShareSharePct);

        // 1. Quota Share Cession
        BigDecimal qsCededAmount = totalInsuredValue.multiply(quotaShareSharePct).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netAfterQS = totalInsuredValue.subtract(qsCededAmount);
        result.setQuotaShareCededAmount(qsCededAmount);

        // 2. Net Primary Retention vs Surplus Treaty
        BigDecimal insurerRetention;
        BigDecimal surplusCededAmount;
        BigDecimal facultativeRequiredAmount;

        if (netAfterQS.compareTo(STANDARD_RETENTION_CAP) <= 0) {
            insurerRetention = netAfterQS;
            surplusCededAmount = BigDecimal.ZERO.setScale(2);
            facultativeRequiredAmount = BigDecimal.ZERO.setScale(2);
        } else {
            insurerRetention = STANDARD_RETENTION_CAP;
            BigDecimal excessOverRetention = netAfterQS.subtract(STANDARD_RETENTION_CAP);

            if (excessOverRetention.compareTo(SURPLUS_TREATY_CAP) <= 0) {
                surplusCededAmount = excessOverRetention;
                facultativeRequiredAmount = BigDecimal.ZERO.setScale(2);
            } else {
                surplusCededAmount = SURPLUS_TREATY_CAP;
                facultativeRequiredAmount = excessOverRetention.subtract(SURPLUS_TREATY_CAP);
            }
        }

        result.setInsurerRetentionAmount(insurerRetention);
        result.setSurplusTreatyCededAmount(surplusCededAmount);
        result.setFacultativeRequiredAmount(facultativeRequiredAmount);

        // 3. Facultative Certificate & Placement Status
        if (facultativeRequiredAmount.compareTo(BigDecimal.ZERO) > 0) {
            result.setFacultativeRequired(true);
            result.setFacultativeCertificateId("FAC-CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            result.setPlacementStatus("FACULTATIVE_PLACEMENT_PENDING");
            result.getReinsurers().add(new ReinsurerShare("Swiss Re Facultative Desk", new BigDecimal("0.60"), facultativeRequiredAmount.multiply(new BigDecimal("0.60")).setScale(2, RoundingMode.HALF_UP)));
            result.getReinsurers().add(new ReinsurerShare("Munich Re Special Risks", new BigDecimal("0.40"), facultativeRequiredAmount.multiply(new BigDecimal("0.40")).setScale(2, RoundingMode.HALF_UP)));
        } else {
            result.setFacultativeRequired(false);
            result.setFacultativeCertificateId("N/A - COVERED_BY_TREATY");
            result.setPlacementStatus("AUTOMATIC_TREATY_BOUND");
            result.getReinsurers().add(new ReinsurerShare("Lloyds Treaty Syndicate 2001", new BigDecimal("1.00"), surplusCededAmount));
        }

        LOGGER.log(Level.INFO, "Reinsurance Cession calculated for policy {0}: TIV=${1}, Ceded QS=${2}, Surplus=${3}, Fac Required=${4}",
                new Object[]{result.getPolicyNumber(), totalInsuredValue, qsCededAmount, surplusCededAmount, facultativeRequiredAmount});

        return result;
    }

    public BigDecimal evaluateExcessOfLossRecovery(BigDecimal grossLossAmount, BigDecimal xolAttachmentPoint, BigDecimal xolLimit) {
        if (grossLossAmount == null || xolAttachmentPoint == null || xolLimit == null) {
            return BigDecimal.ZERO;
        }

        if (grossLossAmount.compareTo(xolAttachmentPoint) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal lossAboveAttachment = grossLossAmount.subtract(xolAttachmentPoint);
        return lossAboveAttachment.min(xolLimit).setScale(2, RoundingMode.HALF_UP);
    }

    public static class ReinsuranceAllocationResult {
        private String policyNumber;
        private BigDecimal totalInsuredValue;
        private BigDecimal quotaSharePct;
        private BigDecimal quotaShareCededAmount;
        private BigDecimal insurerRetentionAmount;
        private BigDecimal surplusTreatyCededAmount;
        private BigDecimal facultativeRequiredAmount;
        private boolean facultativeRequired;
        private String facultativeCertificateId;
        private String placementStatus;
        private List<ReinsurerShare> reinsurers = new ArrayList<>();

        public String getPolicyNumber() { return policyNumber; }
        public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

        public BigDecimal getTotalInsuredValue() { return totalInsuredValue; }
        public void setTotalInsuredValue(BigDecimal totalInsuredValue) { this.totalInsuredValue = totalInsuredValue; }

        public BigDecimal getQuotaSharePct() { return quotaSharePct; }
        public void setQuotaSharePct(BigDecimal quotaSharePct) { this.quotaSharePct = quotaSharePct; }

        public BigDecimal getQuotaShareCededAmount() { return quotaShareCededAmount; }
        public void setQuotaShareCededAmount(BigDecimal quotaShareCededAmount) { this.quotaShareCededAmount = quotaShareCededAmount; }

        public BigDecimal getInsurerRetentionAmount() { return insurerRetentionAmount; }
        public void setInsurerRetentionAmount(BigDecimal insurerRetentionAmount) { this.insurerRetentionAmount = insurerRetentionAmount; }

        public BigDecimal getSurplusTreatyCededAmount() { return surplusTreatyCededAmount; }
        public void setSurplusTreatyCededAmount(BigDecimal surplusTreatyCededAmount) { this.surplusTreatyCededAmount = surplusTreatyCededAmount; }

        public BigDecimal getFacultativeRequiredAmount() { return facultativeRequiredAmount; }
        public void setFacultativeRequiredAmount(BigDecimal facultativeRequiredAmount) { this.facultativeRequiredAmount = facultativeRequiredAmount; }

        public boolean isFacultativeRequired() { return facultativeRequired; }
        public void setFacultativeRequired(boolean facultativeRequired) { this.facultativeRequired = facultativeRequired; }

        public String getFacultativeCertificateId() { return facultativeCertificateId; }
        public void setFacultativeCertificateId(String facultativeCertificateId) { this.facultativeCertificateId = facultativeCertificateId; }

        public String getPlacementStatus() { return placementStatus; }
        public void setPlacementStatus(String placementStatus) { this.placementStatus = placementStatus; }

        public List<ReinsurerShare> getReinsurers() { return reinsurers; }
        public void setReinsurers(List<ReinsurerShare> reinsurers) { this.reinsurers = reinsurers; }
    }

    public static class ReinsurerShare {
        private String reinsurerName;
        private BigDecimal sharePercentage;
        private BigDecimal shareAmount;

        public ReinsurerShare(String reinsurerName, BigDecimal sharePercentage, BigDecimal shareAmount) {
            this.reinsurerName = reinsurerName;
            this.sharePercentage = sharePercentage;
            this.shareAmount = shareAmount;
        }

        public String getReinsurerName() { return reinsurerName; }
        public BigDecimal getSharePercentage() { return sharePercentage; }
        public BigDecimal getShareAmount() { return shareAmount; }
    }
}
