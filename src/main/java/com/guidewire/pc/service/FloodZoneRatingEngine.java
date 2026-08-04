package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FloodZoneRatingEngine {
    private static final Logger LOGGER = Logger.getLogger(FloodZoneRatingEngine.class.getName());
    private static final FloodZoneRatingEngine instance = new FloodZoneRatingEngine();

    private FloodZoneRatingEngine() {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.FloodZoneRatingEngine");}

    public static FloodZoneRatingEngine getInstance() {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.getInstance");
        return instance;
    }

    public FloodResult rateFloodZoneRisk(PolicyPeriod period, String floodZone, double lowestFloorElevationFt, double baseFloodElevationBFE, boolean hasFloodProofVents) {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.rateFloodZoneRisk");
        FloodResult result = new FloodResult();
        if (period == null) return result;

        BigDecimal basePrem = period.getTotalPremium() != null ? period.getTotalPremium() : new BigDecimal("3500.00");
        if (floodZone == null) floodZone = "Zone A";

        result.setPolicyNumber(period.getPolicyNumber());
        result.setBaseFloodPremium(basePrem);
        result.setFloodZone(floodZone);
        result.setLowestFloorElevationFt(lowestFloorElevationFt);
        result.setBaseFloodElevationBFE(baseFloodElevationBFE);
        result.setHasFloodProofVents(hasFloodProofVents);

        double elevationDiff = lowestFloorElevationFt - baseFloodElevationBFE;
        result.setElevationDifferentialFt(elevationDiff);

        double rateAdjustmentPct;
        String ratingCategory;

        if ("Zone X".equalsIgnoreCase(floodZone)) {
            rateAdjustmentPct = -0.50; // Preferred Risk Policy 50% discount
            ratingCategory = "PREFERRED_RISK_POLICY_ZONE_X (-50%)";
        } else if (elevationDiff >= 2.0) {
            rateAdjustmentPct = -0.30; // 30% elevation credit
            ratingCategory = "ELEVATED_STRUCTURE_CREDIT (-30%)";
        } else if (elevationDiff < 0.0) {
            rateAdjustmentPct = 0.50; // 50% below BFE surcharge
            ratingCategory = "BELOW_BFE_HIGH_RISK_SURCHARGE (+50%)";
        } else {
            rateAdjustmentPct = 0.00;
            ratingCategory = "AT_BFE_STANDARD_RATE (0%)";
        }

        if (hasFloodProofVents && rateAdjustmentPct < 0.50) {
            rateAdjustmentPct -= 0.10; // Additional 10% vent credit
        }

        result.setRateAdjustmentPct(rateAdjustmentPct);
        result.setRatingCategory(ratingCategory);

        BigDecimal adjustmentAmt = basePrem.multiply(BigDecimal.valueOf(rateAdjustmentPct)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalPrem = basePrem.add(adjustmentAmt);

        result.setAdjustmentAmount(adjustmentAmt);
        result.setFinalFloodPremium(finalPrem);

        LOGGER.log(Level.INFO, "Flood Zone Risk rated for policy {0}: Zone={1}, ElevDiff={2}ft, Category={3}, FinalPrem=${4}",
                new Object[]{period.getPolicyNumber(), floodZone, elevationDiff, ratingCategory, finalPrem});

        return result;
    }

    public static class FloodResult {
        private String policyNumber;
        private BigDecimal baseFloodPremium = BigDecimal.ZERO;
        private String floodZone;
        private double lowestFloorElevationFt;
        private double baseFloodElevationBFE;
        private double elevationDifferentialFt;
        private boolean hasFloodProofVents;
        private double rateAdjustmentPct;
        private String ratingCategory;
        private BigDecimal adjustmentAmount = BigDecimal.ZERO;
        private BigDecimal finalFloodPremium = BigDecimal.ZERO;

        public String getPolicyNumber() {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.getPolicyNumber"); return policyNumber; }
        public void setPolicyNumber(String policyNumber) {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.setPolicyNumber"); this.policyNumber = policyNumber; }

        public BigDecimal getBaseFloodPremium() {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.getBaseFloodPremium"); return baseFloodPremium; }
        public void setBaseFloodPremium(BigDecimal baseFloodPremium) {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.setBaseFloodPremium"); this.baseFloodPremium = baseFloodPremium; }

        public String getFloodZone() {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.getFloodZone"); return floodZone; }
        public void setFloodZone(String floodZone) {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.setFloodZone"); this.floodZone = floodZone; }

        public double getLowestFloorElevationFt() {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.getLowestFloorElevationFt"); return lowestFloorElevationFt; }
        public void setLowestFloorElevationFt(double lowestFloorElevationFt) {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.setLowestFloorElevationFt"); this.lowestFloorElevationFt = lowestFloorElevationFt; }

        public double getBaseFloodElevationBFE() {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.getBaseFloodElevationBFE"); return baseFloodElevationBFE; }
        public void setBaseFloodElevationBFE(double baseFloodElevationBFE) {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.setBaseFloodElevationBFE"); this.baseFloodElevationBFE = baseFloodElevationBFE; }

        public double getElevationDifferentialFt() {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.getElevationDifferentialFt"); return elevationDifferentialFt; }
        public void setElevationDifferentialFt(double elevationDifferentialFt) {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.setElevationDifferentialFt"); this.elevationDifferentialFt = elevationDifferentialFt; }

        public boolean isHasFloodProofVents() {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.isHasFloodProofVents"); return hasFloodProofVents; }
        public void setHasFloodProofVents(boolean hasFloodProofVents) {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.setHasFloodProofVents"); this.hasFloodProofVents = hasFloodProofVents; }

        public double getRateAdjustmentPct() {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.getRateAdjustmentPct"); return rateAdjustmentPct; }
        public void setRateAdjustmentPct(double rateAdjustmentPct) {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.setRateAdjustmentPct"); this.rateAdjustmentPct = rateAdjustmentPct; }

        public String getRatingCategory() {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.getRatingCategory"); return ratingCategory; }
        public void setRatingCategory(String ratingCategory) {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.setRatingCategory"); this.ratingCategory = ratingCategory; }

        public BigDecimal getAdjustmentAmount() {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.getAdjustmentAmount"); return adjustmentAmount; }
        public void setAdjustmentAmount(BigDecimal adjustmentAmount) {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.setAdjustmentAmount"); this.adjustmentAmount = adjustmentAmount; }

        public BigDecimal getFinalFloodPremium() {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.getFinalFloodPremium"); return finalFloodPremium; }
        public void setFinalFloodPremium(BigDecimal finalFloodPremium) {
        LOGGER.log(Level.FINE, "→ FloodZoneRatingEngine.setFinalFloodPremium"); this.finalFloodPremium = finalFloodPremium; }
    }
}
