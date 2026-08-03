package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CoinsurancePenaltyEngine {
    private static final Logger LOGGER = Logger.getLogger(CoinsurancePenaltyEngine.class.getName());
    private static final CoinsurancePenaltyEngine instance = new CoinsurancePenaltyEngine();

    private CoinsurancePenaltyEngine() {}

    public static CoinsurancePenaltyEngine getInstance() {
        return instance;
    }

    public CoinsuranceResult calculateClaimPayoutWithCoinsurance(PolicyPeriod period, BigDecimal buildingReplacementValue, BigDecimal actualBuildingLimit, double coinsurancePercentage, BigDecimal claimLossAmount, BigDecimal deductible) {
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

        public String getPolicyNumber() { return policyNumber; }
        public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

        public BigDecimal getBuildingReplacementValue() { return buildingReplacementValue; }
        public void setBuildingReplacementValue(BigDecimal buildingReplacementValue) { this.buildingReplacementValue = buildingReplacementValue; }

        public BigDecimal getActualBuildingLimit() { return actualBuildingLimit; }
        public void setActualBuildingLimit(BigDecimal actualBuildingLimit) { this.actualBuildingLimit = actualBuildingLimit; }

        public double getCoinsurancePercentage() { return coinsurancePercentage; }
        public void setCoinsurancePercentage(double coinsurancePercentage) { this.coinsurancePercentage = coinsurancePercentage; }

        public BigDecimal getRequiredCoinsuranceLimit() { return requiredCoinsuranceLimit; }
        public void setRequiredCoinsuranceLimit(BigDecimal requiredCoinsuranceLimit) { this.requiredCoinsuranceLimit = requiredCoinsuranceLimit; }

        public boolean isCoinsuranceRequirementMet() { return coinsuranceRequirementMet; }
        public void setCoinsuranceRequirementMet(boolean coinsuranceRequirementMet) { this.coinsuranceRequirementMet = coinsuranceRequirementMet; }

        public BigDecimal getClaimLossAmount() { return claimLossAmount; }
        public void setClaimLossAmount(BigDecimal claimLossAmount) { this.claimLossAmount = claimLossAmount; }

        public BigDecimal getDeductible() { return deductible; }
        public void setDeductible(BigDecimal deductible) { this.deductible = deductible; }

        public BigDecimal getGrossPayoutBeforeDeductible() { return grossPayoutBeforeDeductible; }
        public void setGrossPayoutBeforeDeductible(BigDecimal grossPayoutBeforeDeductible) { this.grossPayoutBeforeDeductible = grossPayoutBeforeDeductible; }

        public BigDecimal getCoinsurancePenaltyAmount() { return coinsurancePenaltyAmount; }
        public void setCoinsurancePenaltyAmount(BigDecimal coinsurancePenaltyAmount) { this.coinsurancePenaltyAmount = coinsurancePenaltyAmount; }

        public BigDecimal getNetClaimPayout() { return netClaimPayout; }
        public void setNetClaimPayout(BigDecimal netClaimPayout) { this.netClaimPayout = netClaimPayout; }
    }
}
