package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class WCRetrospectiveRatingEngine {
    private static final Logger LOGGER = Logger.getLogger(WCRetrospectiveRatingEngine.class.getName());
    private static final WCRetrospectiveRatingEngine INSTANCE = new WCRetrospectiveRatingEngine();

    public static WCRetrospectiveRatingEngine getInstance() {
        return INSTANCE;
    }

    public static class RetroRatingResult {
        public String policyNumber;
        public BigDecimal manualStandardPremium;
        public BigDecimal basicPremium;
        public BigDecimal basicPremiumFactor; // e.g. 0.22 (22%)
        public BigDecimal incurredLosses;
        public BigDecimal lossConversionFactor; // LCF e.g. 1.15 (15% claims handling)
        public BigDecimal convertedLosses;
        public BigDecimal stateTaxMultiplier; // e.g. 1.05 (5% state assessments)
        public BigDecimal uncappedRetroPremium;
        public BigDecimal minimumPremiumFactor; // e.g. 0.60
        public BigDecimal maximumPremiumFactor; // e.g. 1.40
        public BigDecimal minimumPremiumCap;
        public BigDecimal maximumPremiumCap;
        public BigDecimal finalRetrospectivePremium;
        public BigDecimal retrospectiveAdjustmentAmount; // Final - Standard
        public String adjustmentStatus; // RETURN_PREMIUM, ADDITIONAL_PREMIUM_DUE, EXACT_BALANCE
        public String cappingApplied; // NONE, MINIMUM_CAP, MAXIMUM_CAP
    }

    public RetroRatingResult calculateRetroRating(PolicyPeriod period, BigDecimal standardPremium, BigDecimal incurredLosses,
                                                  BigDecimal basicFactor, BigDecimal lcf, BigDecimal taxMultiplier,
                                                  BigDecimal minFactor, BigDecimal maxFactor) {
        LOGGER.log(Level.FINE, "→ WCRetrospectiveRatingEngine.calculateRetroRating");
        RetroRatingResult res = new RetroRatingResult();
        res.policyNumber = period != null && period.getPolicyNumber() != null ? period.getPolicyNumber() : "POL-WC-RETRO-9001";

        res.manualStandardPremium = standardPremium != null ? standardPremium : new BigDecimal("100000.00");
        res.incurredLosses = incurredLosses != null ? incurredLosses : new BigDecimal("35000.00");

        res.basicPremiumFactor = basicFactor != null ? basicFactor : new BigDecimal("0.220");
        res.lossConversionFactor = lcf != null ? lcf : new BigDecimal("1.150");
        res.stateTaxMultiplier = taxMultiplier != null ? taxMultiplier : new BigDecimal("1.050");
        res.minimumPremiumFactor = minFactor != null ? minFactor : new BigDecimal("0.600");
        res.maximumPremiumFactor = maxFactor != null ? maxFactor : new BigDecimal("1.400");

        // Basic Premium = Standard Premium * Basic Premium Factor
        res.basicPremium = res.manualStandardPremium.multiply(res.basicPremiumFactor).setScale(2, RoundingMode.HALF_UP);

        // Converted Losses = Incurred Losses * Loss Conversion Factor (LCF)
        res.convertedLosses = res.incurredLosses.multiply(res.lossConversionFactor).setScale(2, RoundingMode.HALF_UP);

        // Uncapped Retro Premium = (Basic Premium + Converted Losses) * Tax Multiplier
        BigDecimal sumBasicAndLosses = res.basicPremium.add(res.convertedLosses);
        res.uncappedRetroPremium = sumBasicAndLosses.multiply(res.stateTaxMultiplier).setScale(2, RoundingMode.HALF_UP);

        // Min / Max Caps
        res.minimumPremiumCap = res.manualStandardPremium.multiply(res.minimumPremiumFactor).setScale(2, RoundingMode.HALF_UP);
        res.maximumPremiumCap = res.manualStandardPremium.multiply(res.maximumPremiumFactor).setScale(2, RoundingMode.HALF_UP);

        if (res.uncappedRetroPremium.compareTo(res.minimumPremiumCap) < 0) {
            res.finalRetrospectivePremium = res.minimumPremiumCap;
            res.cappingApplied = "MINIMUM_CAP";
        } else if (res.uncappedRetroPremium.compareTo(res.maximumPremiumCap) > 0) {
            res.finalRetrospectivePremium = res.maximumPremiumCap;
            res.cappingApplied = "MAXIMUM_CAP";
        } else {
            res.finalRetrospectivePremium = res.uncappedRetroPremium;
            res.cappingApplied = "NONE";
        }

        // Net adjustment
        res.retrospectiveAdjustmentAmount = res.finalRetrospectivePremium.subtract(res.manualStandardPremium);

        if (res.retrospectiveAdjustmentAmount.compareTo(BigDecimal.ZERO) < 0) {
            res.adjustmentStatus = "RETURN_PREMIUM";
        } else if (res.retrospectiveAdjustmentAmount.compareTo(BigDecimal.ZERO) > 0) {
            res.adjustmentStatus = "ADDITIONAL_PREMIUM_DUE";
        } else {
            res.adjustmentStatus = "EXACT_BALANCE";
        }

        return res;
    }

    public Map<String, Object> toMap(RetroRatingResult r) {
        Map<String, Object> map = new HashMap<>();
        map.put("policyNumber", r.policyNumber);
        map.put("manualStandardPremium", r.manualStandardPremium);
        map.put("basicPremium", r.basicPremium);
        map.put("basicPremiumFactor", r.basicPremiumFactor);
        map.put("incurredLosses", r.incurredLosses);
        map.put("lossConversionFactor", r.lossConversionFactor);
        map.put("convertedLosses", r.convertedLosses);
        map.put("stateTaxMultiplier", r.stateTaxMultiplier);
        map.put("uncappedRetroPremium", r.uncappedRetroPremium);
        map.put("minimumPremiumCap", r.minimumPremiumCap);
        map.put("maximumPremiumCap", r.maximumPremiumCap);
        map.put("finalRetrospectivePremium", r.finalRetrospectivePremium);
        map.put("retrospectiveAdjustmentAmount", r.retrospectiveAdjustmentAmount);
        map.put("adjustmentStatus", r.adjustmentStatus);
        map.put("cappingApplied", r.cappingApplied);
        map.put("status", "SUCCESS");
        return map;
    }
}
