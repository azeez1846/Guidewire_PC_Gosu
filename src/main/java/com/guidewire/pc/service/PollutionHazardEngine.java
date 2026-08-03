package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PollutionHazardEngine {
    private static final Logger LOGGER = Logger.getLogger(PollutionHazardEngine.class.getName());
    private static final PollutionHazardEngine instance = new PollutionHazardEngine();

    private PollutionHazardEngine() {}

    public static PollutionHazardEngine getInstance() {
        return instance;
    }

    public PollutionResult assessPollutionHazard(PolicyPeriod period, int ustCount, int chemicalHazardScore, double proximityToWaterwayMiles, int facilityAgeYears) {
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

        public String getPolicyNumber() { return policyNumber; }
        public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

        public BigDecimal getBasePremium() { return basePremium; }
        public void setBasePremium(BigDecimal basePremium) { this.basePremium = basePremium; }

        public int getUstCount() { return ustCount; }
        public void setUstCount(int ustCount) { this.ustCount = ustCount; }

        public int getChemicalHazardScore() { return chemicalHazardScore; }
        public void setChemicalHazardScore(int chemicalHazardScore) { this.chemicalHazardScore = chemicalHazardScore; }

        public double getProximityToWaterwayMiles() { return proximityToWaterwayMiles; }
        public void setProximityToWaterwayMiles(double proximityToWaterwayMiles) { this.proximityToWaterwayMiles = proximityToWaterwayMiles; }

        public int getFacilityAgeYears() { return facilityAgeYears; }
        public void setFacilityAgeYears(int facilityAgeYears) { this.facilityAgeYears = facilityAgeYears; }

        public double getEnvironmentalHazardMultiplier() { return environmentalHazardMultiplier; }
        public void setEnvironmentalHazardMultiplier(double environmentalHazardMultiplier) { this.environmentalHazardMultiplier = environmentalHazardMultiplier; }

        public BigDecimal getRatedEnvironmentalPremium() { return ratedEnvironmentalPremium; }
        public void setRatedEnvironmentalPremium(BigDecimal ratedEnvironmentalPremium) { this.ratedEnvironmentalPremium = ratedEnvironmentalPremium; }

        public String getHazardCategory() { return hazardCategory; }
        public void setHazardCategory(String hazardCategory) { this.hazardCategory = hazardCategory; }

        public BigDecimal getRecommendedContainmentDeductible() { return recommendedContainmentDeductible; }
        public void setRecommendedContainmentDeductible(BigDecimal recommendedContainmentDeductible) { this.recommendedContainmentDeductible = recommendedContainmentDeductible; }
    }
}
