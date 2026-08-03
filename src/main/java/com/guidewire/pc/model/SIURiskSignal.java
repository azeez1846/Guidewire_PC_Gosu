package com.guidewire.pc.model;

import java.io.Serializable;

public class SIURiskSignal implements Serializable {
    private static final long serialVersionUID = 1L;

    private String signalCode;
    private String category; // Identity, PolicyVelocity, TerritoryMismatch, EndorsementDateAnomaly, LossHistory
    private String description;
    private int scoreImpact;

    public SIURiskSignal() {}

    public SIURiskSignal(String signalCode, String category, String description, int scoreImpact) {
        this.signalCode = signalCode;
        this.category = category;
        this.description = description;
        this.scoreImpact = scoreImpact;
    }

    public String getSignalCode() { return signalCode; }
    public void setSignalCode(String signalCode) { this.signalCode = signalCode; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getScoreImpact() { return scoreImpact; }
    public void setScoreImpact(int scoreImpact) { this.scoreImpact = scoreImpact; }
}
