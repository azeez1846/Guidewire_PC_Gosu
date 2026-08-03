package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UWEscalationWorkflowEngine {
    private static final Logger LOGGER = Logger.getLogger(UWEscalationWorkflowEngine.class.getName());
    private static final UWEscalationWorkflowEngine instance = new UWEscalationWorkflowEngine();

    private UWEscalationWorkflowEngine() {}

    public static UWEscalationWorkflowEngine getInstance() {
        return instance;
    }

    public EscalationResult processUWEscalation(PolicyPeriod period, BigDecimal totalInsuredValue, int riskScore) {
        EscalationResult result = new EscalationResult();
        if (period == null) return result;

        result.setPolicyNumber(period.getPolicyNumber());
        result.setTotalInsuredValue(totalInsuredValue != null ? totalInsuredValue : new BigDecimal("5000000.00"));
        result.setRiskScore(riskScore);

        List<String> requiredApprovers = new ArrayList<>();
        requiredApprovers.add("Level1_Underwriter");

        if (totalInsuredValue != null && totalInsuredValue.compareTo(new BigDecimal("10000000.00")) > 0) {
            requiredApprovers.add("Level2_UW_Manager");
            result.setEscalationReason("Total Insured Value exceeds $10M threshold ($" + totalInsuredValue + ")");
        }

        if (riskScore >= 70) {
            requiredApprovers.add("Level3_VP_Underwriting");
            result.setEscalationReason(result.getEscalationReason() != null ?
                    result.getEscalationReason() + " & Critical Fraud Risk Score (" + riskScore + " pts)" :
                    "Critical Fraud Risk Score (" + riskScore + " pts)");
        }

        result.setRequiredApproverLevels(requiredApprovers);
        result.setDualSignOffRequired(requiredApprovers.size() > 1);

        if (result.isDualSignOffRequired()) {
            result.setApprovalStatus("PENDING_SENIOR_APPROVAL");
        } else {
            result.setApprovalStatus("APPROVED_LEVEL1");
        }

        LOGGER.log(Level.INFO, "UW Escalation processed for policy {0}: Status={1}, Approvers={2}",
                new Object[]{period.getPolicyNumber(), result.getApprovalStatus(), requiredApprovers});

        return result;
    }

    public static class EscalationResult implements Serializable {
        private static final long serialVersionUID = 1L;

        private String policyNumber;
        private BigDecimal totalInsuredValue = BigDecimal.ZERO;
        private int riskScore;
        private String approvalStatus;
        private String escalationReason;
        private boolean dualSignOffRequired;
        private List<String> requiredApproverLevels = new ArrayList<>();
        private Date lastEvaluatedDate = new Date();

        public String getPolicyNumber() { return policyNumber; }
        public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

        public BigDecimal getTotalInsuredValue() { return totalInsuredValue; }
        public void setTotalInsuredValue(BigDecimal totalInsuredValue) { this.totalInsuredValue = totalInsuredValue; }

        public int getRiskScore() { return riskScore; }
        public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

        public String getApprovalStatus() { return approvalStatus; }
        public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

        public String getEscalationReason() { return escalationReason; }
        public void setEscalationReason(String escalationReason) { this.escalationReason = escalationReason; }

        public boolean isDualSignOffRequired() { return dualSignOffRequired; }
        public void setDualSignOffRequired(boolean dualSignOffRequired) { this.dualSignOffRequired = dualSignOffRequired; }

        public List<String> getRequiredApproverLevels() { return requiredApproverLevels; }
        public void setRequiredApproverLevels(List<String> requiredApproverLevels) { this.requiredApproverLevels = requiredApproverLevels; }

        public Date getLastEvaluatedDate() { return lastEvaluatedDate; }
    }
}
