package com.guidewire.pc.model;

import java.io.Serializable;
import java.util.logging.Logger;
import java.util.logging.Level;

public class SIURiskSignal implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(SIURiskSignal.class.getName());

    private static final long serialVersionUID = 1L;

    private String signalCode;
    private String category; // Identity, PolicyVelocity, TerritoryMismatch, EndorsementDateAnomaly, LossHistory
    private String description;
    private int scoreImpact;

    public SIURiskSignal() {
        LOGGER.log(Level.FINE, "→ SIURiskSignal.SIURiskSignal");}

    public SIURiskSignal(String signalCode, String category, String description, int scoreImpact) {
        LOGGER.log(Level.FINE, "→ SIURiskSignal.SIURiskSignal");
        this.signalCode = signalCode;
        this.category = category;
        this.description = description;
        this.scoreImpact = scoreImpact;
    }

    public String getSignalCode() {
        LOGGER.log(Level.FINE, "→ SIURiskSignal.getSignalCode"); return signalCode; }
    public void setSignalCode(String signalCode) {
        LOGGER.log(Level.FINE, "→ SIURiskSignal.setSignalCode"); this.signalCode = signalCode; }

    public String getCategory() {
        LOGGER.log(Level.FINE, "→ SIURiskSignal.getCategory"); return category; }
    public void setCategory(String category) {
        LOGGER.log(Level.FINE, "→ SIURiskSignal.setCategory"); this.category = category; }

    public String getDescription() {
        LOGGER.log(Level.FINE, "→ SIURiskSignal.getDescription"); return description; }
    public void setDescription(String description) {
        LOGGER.log(Level.FINE, "→ SIURiskSignal.setDescription"); this.description = description; }

    public int getScoreImpact() {
        LOGGER.log(Level.FINE, "→ SIURiskSignal.getScoreImpact"); return scoreImpact; }
    public void setScoreImpact(int scoreImpact) {
        LOGGER.log(Level.FINE, "→ SIURiskSignal.setScoreImpact"); this.scoreImpact = scoreImpact; }
}
