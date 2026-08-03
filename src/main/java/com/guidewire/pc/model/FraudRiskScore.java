package com.guidewire.pc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FraudRiskScore implements Serializable {
    private static final long serialVersionUID = 1L;

    private String policyNumber;
    private String jobNumber;
    private int totalRiskScore; // 0 - 100
    private String riskTier;    // LOW, MEDIUM, HIGH, CRITICAL_SIU
    private boolean siuHoldRequired;
    private final List<SIURiskSignal> riskSignals = new ArrayList<>();
    private Date evaluationTime;

    public FraudRiskScore() {
        this.evaluationTime = new Date();
        this.riskTier = "LOW";
    }

    public FraudRiskScore(String policyNumber, String jobNumber) {
        this();
        this.policyNumber = policyNumber;
        this.jobNumber = jobNumber;
    }

    public void addRiskSignal(SIURiskSignal signal) {
        if (signal != null) {
            this.riskSignals.add(signal);
            this.totalRiskScore += signal.getScoreImpact();
            recalculateTier();
        }
    }

    private void recalculateTier() {
        if (this.totalRiskScore >= 75) {
            this.riskTier = "CRITICAL_SIU";
            this.siuHoldRequired = true;
        } else if (this.totalRiskScore >= 50) {
            this.riskTier = "HIGH";
            this.siuHoldRequired = true;
        } else if (this.totalRiskScore >= 25) {
            this.riskTier = "MEDIUM";
            this.siuHoldRequired = false;
        } else {
            this.riskTier = "LOW";
            this.siuHoldRequired = false;
        }
    }

    // Getters and Setters
    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

    public String getJobNumber() { return jobNumber; }
    public void setJobNumber(String jobNumber) { this.jobNumber = jobNumber; }

    public int getTotalRiskScore() { return totalRiskScore; }
    public void setTotalRiskScore(int totalRiskScore) {
        this.totalRiskScore = totalRiskScore;
        recalculateTier();
    }

    public String getRiskTier() { return riskTier; }
    public boolean isSiuHoldRequired() { return siuHoldRequired; }

    public List<SIURiskSignal> getRiskSignals() { return riskSignals; }
    public Date getEvaluationTime() { return evaluationTime; }
}
