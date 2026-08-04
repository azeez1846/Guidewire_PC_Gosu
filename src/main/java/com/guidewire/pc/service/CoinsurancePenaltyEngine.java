package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CoinsurancePenaltyEngine {
    private static final Logger LOGGER = Logger.getLogger(CoinsurancePenaltyEngine.class.getName());
    private static final CoinsurancePenaltyEngine instance = new CoinsurancePenaltyEngine();

    private CoinsurancePenaltyEngine() {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.CoinsurancePenaltyEngine");}

    public static CoinsurancePenaltyEngine getInstance() {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.getInstance");
        return instance;
    }

    public CoinsuranceResult calculateClaimPayoutWithCoinsurance(PolicyPeriod period, BigDecimal buildingReplacementValue, BigDecimal actualBuildingLimit, double coinsurancePercentage, BigDecimal claimLossAmount, BigDecimal deductible) {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.calculateClaimPayoutWithCoinsurance");
        CoinsuranceResult result = new CoinsuranceResult();
        if (buildingReplacementValue == null) buildingReplacementValue = new BigDecimal("2000000.00");
        if (actualBuildingLimit == null) actualBuildingLimit = new BigDecimal("1200000.00");
        if (coinsurancePercentage <= 0) coinsurancePercentage = 0.80; // Default 80% Coinsurance
        if (claimLossAmount == null) claimLossAmount = new BigDecimal("500000.00");
        if (deductible == null) deductible = new BigDecimal("5000.00");

        result.setPolicyNumber(period != null ? period.getPolicyNumber() : "POL-PROP-8001");
        result.setBuildingReplacementValue(buildingReplacementValue);
        result.setActualBuildingLimit(actualBuildingLimit);
        result.setCoinsurancePercentage(coinsurancePercentage);
        result.setClaimLossAmount(claimLossAmount);
        result.setDeductible(deductible);

        BigDecimal requiredLimit = buildingReplacementValue.multiply(BigDecimal.valueOf(coinsurancePercentage)).setScale(2, RoundingMode.HALF_UP);
        result.setRequiredCoinsuranceLimit(requiredLimit);

        BigDecimal grossPayout;
        BigDecimal penaltyAmount = BigDecimal.ZERO;

        if (actualBuildingLimit.compareTo(requiredLimit) >= 0) {
            // Insured met coinsurance requirement -> Full loss coverage minus deductible
            result.setCoinsuranceRequirementMet(true);
            grossPayout = claimLossAmount;
        } else {
            // Under-insured -> Coinsurance Penalty Formula: Loss * (ActualLimit / RequiredLimit)
            result.setCoinsuranceRequirementMet(false);
            double penaltyFactor = actualBuildingLimit.divide(requiredLimit, 4, RoundingMode.HALF_UP).doubleValue();
            grossPayout = claimLossAmount.multiply(BigDecimal.valueOf(penaltyFactor)).setScale(2, RoundingMode.HALF_UP);
            penaltyAmount = claimLossAmount.subtract(grossPayout);
        }

        BigDecimal netClaimPayout = grossPayout.subtract(deductible);
        if (netClaimPayout.compareTo(BigDecimal.ZERO) < 0) netClaimPayout = BigDecimal.ZERO;

        result.setGrossPayoutBeforeDeductible(grossPayout);
        result.setCoinsurancePenaltyAmount(penaltyAmount);
        result.setNetClaimPayout(netClaimPayout);

        LOGGER.log(Level.INFO, "Coinsurance Penalty evaluated for policy {0}: RequirementMet={1}, GrossPayout=${2}, Penalty=${3}, NetPayout=${4}",
                new Object[]{result.getPolicyNumber(), result.isCoinsuranceRequirementMet(), grossPayout, penaltyAmount, netClaimPayout});

        return result;
    }

    public static class CoinsuranceResult {
        private String policyNumber;
        private BigDecimal buildingReplacementValue = BigDecimal.ZERO;
        private BigDecimal actualBuildingLimit = BigDecimal.ZERO;
        private double coinsurancePercentage;
        private BigDecimal requiredCoinsuranceLimit = BigDecimal.ZERO;
        private boolean coinsuranceRequirementMet;
        private BigDecimal claimLossAmount = BigDecimal.ZERO;
        private BigDecimal deductible = BigDecimal.ZERO;
        private BigDecimal grossPayoutBeforeDeductible = BigDecimal.ZERO;
        private BigDecimal coinsurancePenaltyAmount = BigDecimal.ZERO;
        private BigDecimal netClaimPayout = BigDecimal.ZERO;

        public String getPolicyNumber() {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.getPolicyNumber"); return policyNumber; }
        public void setPolicyNumber(String policyNumber) {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.setPolicyNumber"); this.policyNumber = policyNumber; }

        public BigDecimal getBuildingReplacementValue() {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.getBuildingReplacementValue"); return buildingReplacementValue; }
        public void setBuildingReplacementValue(BigDecimal buildingReplacementValue) {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.setBuildingReplacementValue"); this.buildingReplacementValue = buildingReplacementValue; }

        public BigDecimal getActualBuildingLimit() {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.getActualBuildingLimit"); return actualBuildingLimit; }
        public void setActualBuildingLimit(BigDecimal actualBuildingLimit) {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.setActualBuildingLimit"); this.actualBuildingLimit = actualBuildingLimit; }

        public double getCoinsurancePercentage() {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.getCoinsurancePercentage"); return coinsurancePercentage; }
        public void setCoinsurancePercentage(double coinsurancePercentage) {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.setCoinsurancePercentage"); this.coinsurancePercentage = coinsurancePercentage; }

        public BigDecimal getRequiredCoinsuranceLimit() {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.getRequiredCoinsuranceLimit"); return requiredCoinsuranceLimit; }
        public void setRequiredCoinsuranceLimit(BigDecimal requiredCoinsuranceLimit) {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.setRequiredCoinsuranceLimit"); this.requiredCoinsuranceLimit = requiredCoinsuranceLimit; }

        public boolean isCoinsuranceRequirementMet() {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.isCoinsuranceRequirementMet"); return coinsuranceRequirementMet; }
        public void setCoinsuranceRequirementMet(boolean coinsuranceRequirementMet) {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.setCoinsuranceRequirementMet"); this.coinsuranceRequirementMet = coinsuranceRequirementMet; }

        public BigDecimal getClaimLossAmount() {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.getClaimLossAmount"); return claimLossAmount; }
        public void setClaimLossAmount(BigDecimal claimLossAmount) {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.setClaimLossAmount"); this.claimLossAmount = claimLossAmount; }

        public BigDecimal getDeductible() {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.getDeductible"); return deductible; }
        public void setDeductible(BigDecimal deductible) {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.setDeductible"); this.deductible = deductible; }

        public BigDecimal getGrossPayoutBeforeDeductible() {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.getGrossPayoutBeforeDeductible"); return grossPayoutBeforeDeductible; }
        public void setGrossPayoutBeforeDeductible(BigDecimal grossPayoutBeforeDeductible) {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.setGrossPayoutBeforeDeductible"); this.grossPayoutBeforeDeductible = grossPayoutBeforeDeductible; }

        public BigDecimal getCoinsurancePenaltyAmount() {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.getCoinsurancePenaltyAmount"); return coinsurancePenaltyAmount; }
        public void setCoinsurancePenaltyAmount(BigDecimal coinsurancePenaltyAmount) {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.setCoinsurancePenaltyAmount"); this.coinsurancePenaltyAmount = coinsurancePenaltyAmount; }

        public BigDecimal getNetClaimPayout() {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.getNetClaimPayout"); return netClaimPayout; }
        public void setNetClaimPayout(BigDecimal netClaimPayout) {
        LOGGER.log(Level.FINE, "→ CoinsurancePenaltyEngine.setNetClaimPayout"); this.netClaimPayout = netClaimPayout; }
    }
}
