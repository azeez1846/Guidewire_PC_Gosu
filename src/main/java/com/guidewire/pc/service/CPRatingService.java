package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class CPRatingService {
    private static final Logger LOGGER = Logger.getLogger(CPRatingService.class.getName());

    public static BigDecimal rateCommercialProperty(PolicyPeriod period, BigDecimal buildingLimit, BigDecimal bppLimit, String protectionClass) {
        LOGGER.log(Level.FINE, "→ CPRatingService.rateCommercialProperty");
        return rateCommercialPropertyExtended(period, buildingLimit, bppLimit, protectionClass, false, false, false);
    }

    public static BigDecimal rateCommercialPropertyExtended(PolicyPeriod period, BigDecimal buildingLimit, BigDecimal bppLimit, String protectionClass,
                                                             boolean hasEarthquake, boolean hasFlood, boolean hasSprinkler) {
        LOGGER.log(Level.FINE, "→ CPRatingService.rateCommercialPropertyExtended");
        if (period == null) return BigDecimal.ZERO;

        BigDecimal bldg = buildingLimit != null ? buildingLimit : new BigDecimal("1000000.00");
        BigDecimal bpp = bppLimit != null ? bppLimit : new BigDecimal("250000.00");
        BigDecimal totalLimit = bldg.add(bpp);

        double baseRate = 0.35;
        if ("9".equals(protectionClass) || "10".equals(protectionClass)) {
            baseRate = 0.85;
        } else if ("5".equals(protectionClass) || "6".equals(protectionClass) || "7".equals(protectionClass) || "8".equals(protectionClass)) {
            baseRate = 0.55;
        }

        BigDecimal unitsOf100 = totalLimit.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);
        BigDecimal basePrem = unitsOf100.multiply(new BigDecimal(baseRate)).setScale(2, RoundingMode.HALF_UP);

        // Catastrophe & protection credits/surcharges
        if (hasEarthquake) {
            basePrem = basePrem.add(totalLimit.multiply(new BigDecimal("0.0008")).setScale(2, RoundingMode.HALF_UP));
        }
        if (hasFlood) {
            basePrem = basePrem.add(totalLimit.multiply(new BigDecimal("0.0012")).setScale(2, RoundingMode.HALF_UP));
        }
        if (hasSprinkler) {
            basePrem = basePrem.multiply(new BigDecimal("0.92")).setScale(2, RoundingMode.HALF_UP); // 8% sprinkler discount
        }

        BigDecimal tax = basePrem.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);

        return basePrem.add(tax);
    }

    public static BigDecimal rateTenantsImprovements(BigDecimal limit, String valuationBasis) {
        LOGGER.log(Level.FINE, "→ CPRatingService.rateTenantsImprovements");
        if (limit == null || limit.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        BigDecimal baseRate = new BigDecimal("0.42");
        BigDecimal unitsOf100 = limit.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);
        BigDecimal premium = unitsOf100.multiply(baseRate).setScale(2, RoundingMode.HALF_UP);

        if ("ReplacementCost".equalsIgnoreCase(valuationBasis)) {
            premium = premium.multiply(new BigDecimal("1.15")).setScale(2, RoundingMode.HALF_UP);
        }
        return premium;
    }

    public static BigDecimal rateBusinessIncome(BigDecimal biLimit, String indemnityFraction, boolean includePayroll) {
        LOGGER.log(Level.FINE, "→ CPRatingService.rateBusinessIncome");
        if (biLimit == null || biLimit.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        BigDecimal fractionMultiplier = new BigDecimal("1.00");
        if ("1/3".equals(indemnityFraction)) {
            fractionMultiplier = new BigDecimal("0.90");
        } else if ("1/4".equals(indemnityFraction)) {
            fractionMultiplier = new BigDecimal("0.80");
        } else if ("1/6".equals(indemnityFraction)) {
            fractionMultiplier = new BigDecimal("0.65");
        }

        BigDecimal baseRate = new BigDecimal("0.50");
        BigDecimal unitsOf100 = biLimit.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);
        BigDecimal premium = unitsOf100.multiply(baseRate).multiply(fractionMultiplier).setScale(2, RoundingMode.HALF_UP);

        if (includePayroll) {
            premium = premium.multiply(new BigDecimal("1.10")).setScale(2, RoundingMode.HALF_UP);
        }
        return premium;
    }

    public static BigDecimal rateBoilerAndMachinery(BigDecimal equipmentLimit, boolean hasProductionMachinery) {
        LOGGER.log(Level.FINE, "→ CPRatingService.rateBoilerAndMachinery");
        if (equipmentLimit == null || equipmentLimit.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        BigDecimal rate = new BigDecimal("0.18");
        BigDecimal unitsOf100 = equipmentLimit.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);
        BigDecimal premium = unitsOf100.multiply(rate).setScale(2, RoundingMode.HALF_UP);

        if (hasProductionMachinery) {
            premium = premium.multiply(new BigDecimal("1.25")).setScale(2, RoundingMode.HALF_UP);
        }
        return premium;
    }

    public static BigDecimal rateBlanketCoverage(BigDecimal blanketLimit, BigDecimal weightedRatePer100, int coinsurancePct) {
        LOGGER.log(Level.FINE, "→ CPRatingService.rateBlanketCoverage");
        if (blanketLimit == null || blanketLimit.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        BigDecimal rate = weightedRatePer100 != null ? weightedRatePer100 : new BigDecimal("0.45");
        BigDecimal unitsOf100 = blanketLimit.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);
        BigDecimal premium = unitsOf100.multiply(rate).setScale(2, RoundingMode.HALF_UP);

        if (coinsurancePct >= 100) {
            premium = premium.multiply(new BigDecimal("0.90")).setScale(2, RoundingMode.HALF_UP);
        } else if (coinsurancePct >= 90) {
            premium = premium.multiply(new BigDecimal("0.95")).setScale(2, RoundingMode.HALF_UP);
        }
        return premium;
    }

    public static Map<String, Object> calculateCoinsurancePenalty(BigDecimal coverageLimit, int coinsuranceReqPct, BigDecimal propertyFullValue, BigDecimal lossAmount, BigDecimal deductible) {
        LOGGER.log(Level.FINE, "→ CPRatingService.calculateCoinsurancePenalty");
        Map<String, Object> res = new HashMap<>();
        BigDecimal reqPctDecimal = new BigDecimal(coinsuranceReqPct).divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);
        BigDecimal requiredInsurance = propertyFullValue.multiply(reqPctDecimal).setScale(2, RoundingMode.HALF_UP);

        BigDecimal factor = BigDecimal.ONE;
        boolean hasPenalty = false;
        if (coverageLimit.compareTo(requiredInsurance) < 0) {
            factor = coverageLimit.divide(requiredInsurance, 4, RoundingMode.HALF_UP);
            hasPenalty = true;
        }

        BigDecimal grossPayout = lossAmount.multiply(factor).setScale(2, RoundingMode.HALF_UP);
        BigDecimal ded = deductible != null ? deductible : BigDecimal.ZERO;
        BigDecimal netPayout = grossPayout.subtract(ded);
        if (netPayout.compareTo(BigDecimal.ZERO) < 0) {
            netPayout = BigDecimal.ZERO;
        }
        netPayout = netPayout.min(coverageLimit);

        BigDecimal unpenalizedPayout = lossAmount.subtract(ded).max(BigDecimal.ZERO).min(coverageLimit);
        BigDecimal penaltyAmount = unpenalizedPayout.subtract(netPayout).max(BigDecimal.ZERO);

        res.put("reportedLimit", coverageLimit);
        res.put("requiredInsurance", requiredInsurance);
        res.put("coinsuranceFactor", factor);
        res.put("hasPenalty", hasPenalty);
        res.put("grossClaimLoss", lossAmount);
        res.put("deductible", ded);
        res.put("netPayout", netPayout);
        res.put("penaltyAmount", penaltyAmount);
        return res;
    }

    public static Map<String, Object> rateFullCommercialPropertyPackage(Map<String, Object> input) {
        LOGGER.log(Level.FINE, "→ CPRatingService.rateFullCommercialPropertyPackage");
        BigDecimal bldgLimit = input.get("buildingLimit") != null ? new BigDecimal(input.get("buildingLimit").toString()) : new BigDecimal("1000000.00");
        BigDecimal bppLimit = input.get("bppLimit") != null ? new BigDecimal(input.get("bppLimit").toString()) : new BigDecimal("250000.00");
        String protClass = input.get("protectionClass") != null ? input.get("protectionClass").toString() : "3";
        boolean eq = Boolean.parseBoolean(String.valueOf(input.getOrDefault("earthquake", false)));
        boolean flood = Boolean.parseBoolean(String.valueOf(input.getOrDefault("flood", false)));
        boolean sprinkler = Boolean.parseBoolean(String.valueOf(input.getOrDefault("sprinkler", true)));

        BigDecimal basePropertyPrem = rateCommercialPropertyExtended(new PolicyPeriod(), bldgLimit, bppLimit, protClass, eq, flood, sprinkler);

        BigDecimal tiLimit = input.get("tenantsImprovementLimit") != null ? new BigDecimal(input.get("tenantsImprovementLimit").toString()) : BigDecimal.ZERO;
        String tiValuation = input.get("tenantsValuationBasis") != null ? input.get("tenantsValuationBasis").toString() : "ReplacementCost";
        BigDecimal tiPrem = rateTenantsImprovements(tiLimit, tiValuation);

        BigDecimal biLimit = input.get("businessIncomeLimit") != null ? new BigDecimal(input.get("businessIncomeLimit").toString()) : BigDecimal.ZERO;
        String biFraction = input.get("indemnityFraction") != null ? input.get("indemnityFraction").toString() : "1/3";
        boolean biPayroll = Boolean.parseBoolean(String.valueOf(input.getOrDefault("includePayroll", true)));
        BigDecimal biPrem = rateBusinessIncome(biLimit, biFraction, biPayroll);

        BigDecimal eqLimit = input.get("equipmentBreakdownLimit") != null ? new BigDecimal(input.get("equipmentBreakdownLimit").toString()) : BigDecimal.ZERO;
        boolean eqProd = Boolean.parseBoolean(String.valueOf(input.getOrDefault("hasProductionMachinery", false)));
        BigDecimal eqPrem = rateBoilerAndMachinery(eqLimit, eqProd);

        BigDecimal totalPackagePremium = basePropertyPrem.add(tiPrem).add(biPrem).add(eqPrem);

        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("basePropertyPremium", basePropertyPrem);
        breakdown.put("tenantsImprovementsPremium", tiPrem);
        breakdown.put("businessIncomePremium", biPrem);
        breakdown.put("equipmentBreakdownPremium", eqPrem);
        breakdown.put("totalPackagePremium", totalPackagePremium);
        breakdown.put("status", "SUCCESS");
        return breakdown;
    }

    public static List<String> validateCommercialPropertyLine(PolicyPeriod period, BigDecimal buildingLimit, int coinsurance) {
        LOGGER.log(Level.FINE, "→ CPRatingService.validateCommercialPropertyLine");
        List<String> errors = new ArrayList<>();
        if (period == null) return errors;

        if (buildingLimit == null || buildingLimit.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("CP_VALIDATION_ERROR: Building coverage limit must be greater than 0");
        }

        if (coinsurance < 80 || coinsurance > 100) {
            errors.add("CP_VALIDATION_ERROR: Coinsurance percentage must be between 80% and 100%");
        }

        return errors;
    }
}
