package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TelematicsRatingEngine {
    private static final Logger LOGGER = Logger.getLogger(TelematicsRatingEngine.class.getName());
    private static final TelematicsRatingEngine instance = new TelematicsRatingEngine();

    private TelematicsRatingEngine() {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.TelematicsRatingEngine");}

    public static TelematicsRatingEngine getInstance() {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.getInstance");
        return instance;
    }

    public TelematicsResult evaluateTelematicsDrivingScore(PolicyPeriod period, double hardBrakesPer1k, double rapidAccelerationsPer1k, double lateNightDrivingPct, double speedingEventsPer1k) {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.evaluateTelematicsDrivingScore");
        TelematicsResult result = new TelematicsResult();
        if (period == null) return result;

        BigDecimal basePrem = period.getTotalPremium() != null ? period.getTotalPremium() : new BigDecimal("2500.00");
        result.setPolicyNumber(period.getPolicyNumber());
        result.setBasePremium(basePrem);
        result.setHardBrakesPer1k(hardBrakesPer1k);
        result.setRapidAccelerationsPer1k(rapidAccelerationsPer1k);
        result.setLateNightDrivingPct(lateNightDrivingPct);
        result.setSpeedingEventsPer1k(speedingEventsPer1k);

        // Safety score formula
        double deduction = (hardBrakesPer1k * 2.0) + (rapidAccelerationsPer1k * 1.5) + (lateNightDrivingPct * 100.0 * 0.5) + (speedingEventsPer1k * 3.0);
        double safetyScore = Math.max(0.0, Math.min(100.0, 100.0 - deduction));
        result.setSafetyScore(safetyScore);

        double rateAdjustmentPct;
        String tier;

        if (safetyScore >= 85.0) {
            rateAdjustmentPct = -0.20; // 20% discount
            tier = "EXCELLENT_DRIVER_DISCOUNT (-20%)";
        } else if (safetyScore >= 70.0) {
            rateAdjustmentPct = -0.10; // 10% discount
            tier = "GOOD_DRIVER_DISCOUNT (-10%)";
        } else if (safetyScore >= 50.0) {
            rateAdjustmentPct = 0.00; // Standard
            tier = "STANDARD_DRIVER (0%)";
        } else {
            rateAdjustmentPct = 0.15; // 15% surcharge
            tier = "HIGH_RISK_SURCHARGE (+15%)";
        }

        result.setRateAdjustmentPct(rateAdjustmentPct);
        result.setDrivingTier(tier);

        BigDecimal adjustmentAmt = basePrem.multiply(BigDecimal.valueOf(rateAdjustmentPct)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal adjustedPrem = basePrem.add(adjustmentAmt);

        result.setAdjustmentAmount(adjustmentAmt);
        result.setAdjustedPremium(adjustedPrem);

        LOGGER.log(Level.INFO, "Telematics score evaluated for policy {0}: Score={1}, Tier={2}, BasePrem=${3}, AdjustedPrem=${4}",
                new Object[]{period.getPolicyNumber(), safetyScore, tier, basePrem, adjustedPrem});

        return result;
    }

    public static class TelematicsResult {
        private String policyNumber;
        private BigDecimal basePremium = BigDecimal.ZERO;
        private double hardBrakesPer1k;
        private double rapidAccelerationsPer1k;
        private double lateNightDrivingPct;
        private double speedingEventsPer1k;
        private double safetyScore;
        private double rateAdjustmentPct;
        private String drivingTier;
        private BigDecimal adjustmentAmount = BigDecimal.ZERO;
        private BigDecimal adjustedPremium = BigDecimal.ZERO;

        public String getPolicyNumber() {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.getPolicyNumber"); return policyNumber; }
        public void setPolicyNumber(String policyNumber) {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.setPolicyNumber"); this.policyNumber = policyNumber; }

        public BigDecimal getBasePremium() {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.getBasePremium"); return basePremium; }
        public void setBasePremium(BigDecimal basePremium) {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.setBasePremium"); this.basePremium = basePremium; }

        public double getHardBrakesPer1k() {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.getHardBrakesPer1k"); return hardBrakesPer1k; }
        public void setHardBrakesPer1k(double hardBrakesPer1k) {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.setHardBrakesPer1k"); this.hardBrakesPer1k = hardBrakesPer1k; }

        public double getRapidAccelerationsPer1k() {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.getRapidAccelerationsPer1k"); return rapidAccelerationsPer1k; }
        public void setRapidAccelerationsPer1k(double rapidAccelerationsPer1k) {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.setRapidAccelerationsPer1k"); this.rapidAccelerationsPer1k = rapidAccelerationsPer1k; }

        public double getLateNightDrivingPct() {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.getLateNightDrivingPct"); return lateNightDrivingPct; }
        public void setLateNightDrivingPct(double lateNightDrivingPct) {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.setLateNightDrivingPct"); this.lateNightDrivingPct = lateNightDrivingPct; }

        public double getSpeedingEventsPer1k() {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.getSpeedingEventsPer1k"); return speedingEventsPer1k; }
        public void setSpeedingEventsPer1k(double speedingEventsPer1k) {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.setSpeedingEventsPer1k"); this.speedingEventsPer1k = speedingEventsPer1k; }

        public double getSafetyScore() {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.getSafetyScore"); return safetyScore; }
        public void setSafetyScore(double safetyScore) {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.setSafetyScore"); this.safetyScore = safetyScore; }

        public double getRateAdjustmentPct() {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.getRateAdjustmentPct"); return rateAdjustmentPct; }
        public void setRateAdjustmentPct(double rateAdjustmentPct) {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.setRateAdjustmentPct"); this.rateAdjustmentPct = rateAdjustmentPct; }

        public String getDrivingTier() {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.getDrivingTier"); return drivingTier; }
        public void setDrivingTier(String drivingTier) {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.setDrivingTier"); this.drivingTier = drivingTier; }

        public BigDecimal getAdjustmentAmount() {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.getAdjustmentAmount"); return adjustmentAmount; }
        public void setAdjustmentAmount(BigDecimal adjustmentAmount) {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.setAdjustmentAmount"); this.adjustmentAmount = adjustmentAmount; }

        public BigDecimal getAdjustedPremium() {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.getAdjustedPremium"); return adjustedPremium; }
        public void setAdjustedPremium(BigDecimal adjustedPremium) {
        LOGGER.log(Level.FINE, "→ TelematicsRatingEngine.setAdjustedPremium"); this.adjustedPremium = adjustedPremium; }
    }
}
