package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PollutionHazardEngine {
    private static final Logger LOGGER = Logger.getLogger(PollutionHazardEngine.class.getName());
    private static final PollutionHazardEngine instance = new PollutionHazardEngine();

    private PollutionHazardEngine() {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.PollutionHazardEngine");}

    public static PollutionHazardEngine getInstance() {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.getInstance");
        return instance;
    }

    public PollutionResult assessPollutionHazard(PolicyPeriod period, int ustCount, int chemicalHazardScore, double proximityToWaterwayMiles, int facilityAgeYears) {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.assessPollutionHazard");
        PollutionResult result = new PollutionResult();
        if (period == null) return result;

        BigDecimal basePrem = period.getTotalPremium() != null ? period.getTotalPremium() : new BigDecimal("12000.00");

        result.setPolicyNumber(period.getPolicyNumber());
        result.setBasePremium(basePrem);
        result.setUstCount(ustCount);
        result.setChemicalHazardScore(chemicalHazardScore);
        result.setProximityToWaterwayMiles(proximityToWaterwayMiles);
        result.setFacilityAgeYears(facilityAgeYears);

        double multiplier = 1.0;
        multiplier += (ustCount * 0.10);
        multiplier += (chemicalHazardScore * 0.05);
        if (facilityAgeYears > 20) multiplier += 0.20;
        if (proximityToWaterwayMiles < 1.0) multiplier += 0.25;

        result.setEnvironmentalHazardMultiplier(multiplier);

        BigDecimal ratedPrem = basePrem.multiply(BigDecimal.valueOf(multiplier)).setScale(2, RoundingMode.HALF_UP);
        result.setRatedEnvironmentalPremium(ratedPrem);

        BigDecimal requiredDeductible;
        String hazardCategory;

        if (multiplier >= 2.0) {
            hazardCategory = "HIGH_ENVIRONMENTAL_HAZARD (Severe Risk)";
            requiredDeductible = new BigDecimal("50000.00");
        } else if (multiplier >= 1.4) {
            hazardCategory = "MODERATE_ENVIRONMENTAL_HAZARD (Elevated Risk)";
            requiredDeductible = new BigDecimal("25000.00");
        } else {
            hazardCategory = "LOW_ENVIRONMENTAL_HAZARD (Standard Risk)";
            requiredDeductible = new BigDecimal("5000.00");
        }

        result.setHazardCategory(hazardCategory);
        result.setRecommendedContainmentDeductible(requiredDeductible);

        LOGGER.log(Level.INFO, "Pollution Hazard assessed for policy {0}: Multiplier={1}, Category={2}, RatedPrem=${3}, RecDeductible=${4}",
                new Object[]{period.getPolicyNumber(), multiplier, hazardCategory, ratedPrem, requiredDeductible});

        return result;
    }

    public static class PollutionResult {
        private String policyNumber;
        private BigDecimal basePremium = BigDecimal.ZERO;
        private int ustCount;
        private int chemicalHazardScore;
        private double proximityToWaterwayMiles;
        private int facilityAgeYears;
        private double environmentalHazardMultiplier;
        private BigDecimal ratedEnvironmentalPremium = BigDecimal.ZERO;
        private String hazardCategory;
        private BigDecimal recommendedContainmentDeductible = BigDecimal.ZERO;

        public String getPolicyNumber() {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.getPolicyNumber"); return policyNumber; }
        public void setPolicyNumber(String policyNumber) {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.setPolicyNumber"); this.policyNumber = policyNumber; }

        public BigDecimal getBasePremium() {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.getBasePremium"); return basePremium; }
        public void setBasePremium(BigDecimal basePremium) {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.setBasePremium"); this.basePremium = basePremium; }

        public int getUstCount() {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.getUstCount"); return ustCount; }
        public void setUstCount(int ustCount) {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.setUstCount"); this.ustCount = ustCount; }

        public int getChemicalHazardScore() {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.getChemicalHazardScore"); return chemicalHazardScore; }
        public void setChemicalHazardScore(int chemicalHazardScore) {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.setChemicalHazardScore"); this.chemicalHazardScore = chemicalHazardScore; }

        public double getProximityToWaterwayMiles() {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.getProximityToWaterwayMiles"); return proximityToWaterwayMiles; }
        public void setProximityToWaterwayMiles(double proximityToWaterwayMiles) {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.setProximityToWaterwayMiles"); this.proximityToWaterwayMiles = proximityToWaterwayMiles; }

        public int getFacilityAgeYears() {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.getFacilityAgeYears"); return facilityAgeYears; }
        public void setFacilityAgeYears(int facilityAgeYears) {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.setFacilityAgeYears"); this.facilityAgeYears = facilityAgeYears; }

        public double getEnvironmentalHazardMultiplier() {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.getEnvironmentalHazardMultiplier"); return environmentalHazardMultiplier; }
        public void setEnvironmentalHazardMultiplier(double environmentalHazardMultiplier) {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.setEnvironmentalHazardMultiplier"); this.environmentalHazardMultiplier = environmentalHazardMultiplier; }

        public BigDecimal getRatedEnvironmentalPremium() {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.getRatedEnvironmentalPremium"); return ratedEnvironmentalPremium; }
        public void setRatedEnvironmentalPremium(BigDecimal ratedEnvironmentalPremium) {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.setRatedEnvironmentalPremium"); this.ratedEnvironmentalPremium = ratedEnvironmentalPremium; }

        public String getHazardCategory() {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.getHazardCategory"); return hazardCategory; }
        public void setHazardCategory(String hazardCategory) {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.setHazardCategory"); this.hazardCategory = hazardCategory; }

        public BigDecimal getRecommendedContainmentDeductible() {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.getRecommendedContainmentDeductible"); return recommendedContainmentDeductible; }
        public void setRecommendedContainmentDeductible(BigDecimal recommendedContainmentDeductible) {
        LOGGER.log(Level.FINE, "→ PollutionHazardEngine.setRecommendedContainmentDeductible"); this.recommendedContainmentDeductible = recommendedContainmentDeductible; }
    }
}
