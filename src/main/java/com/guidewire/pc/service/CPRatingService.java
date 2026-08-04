package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
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
