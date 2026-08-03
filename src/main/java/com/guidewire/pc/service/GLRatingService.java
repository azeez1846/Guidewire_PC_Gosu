package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class GLRatingService {

    public static BigDecimal rateGeneralLiability(PolicyPeriod period, BigDecimal exposureBasisAmount, BigDecimal baseRatePer1000, boolean isClaimsMade) {
        return rateGeneralLiabilityExtended(period, exposureBasisAmount, baseRatePer1000, isClaimsMade, false, 0);
    }

    public static BigDecimal rateGeneralLiabilityExtended(PolicyPeriod period, BigDecimal exposureBasisAmount, BigDecimal baseRatePer1000,
                                                          boolean isClaimsMade, boolean hasCyberExtension, int additionalInsuredCount) {
        if (period == null || exposureBasisAmount == null) return BigDecimal.ZERO;

        BigDecimal rate = baseRatePer1000 != null ? baseRatePer1000 : new BigDecimal("4.50");
        BigDecimal unitsOf1000 = exposureBasisAmount.divide(new BigDecimal("1000.00"), 4, RoundingMode.HALF_UP);
        BigDecimal basePrem = unitsOf1000.multiply(rate).setScale(2, RoundingMode.HALF_UP);

        if (isClaimsMade) {
            basePrem = basePrem.multiply(new BigDecimal("0.85")).setScale(2, RoundingMode.HALF_UP);
        }

        if (hasCyberExtension) {
            basePrem = basePrem.add(new BigDecimal("350.00")); // Flat $350 cyber extension rider
        }

        if (additionalInsuredCount > 0) {
            BigDecimal aiFee = new BigDecimal(additionalInsuredCount * 50); // $50 per additional insured endorsement
            basePrem = basePrem.add(aiFee);
        }

        BigDecimal tax = basePrem.multiply(new BigDecimal("0.07")).setScale(2, RoundingMode.HALF_UP);
        return basePrem.add(tax);
    }

    public static List<String> validateGeneralLiabilityLine(PolicyPeriod period, BigDecimal exposureAmount, String coverageForm) {
        List<String> errors = new ArrayList<>();
        if (period == null) return errors;

        if (exposureAmount == null || exposureAmount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("GL_VALIDATION_ERROR: Exposure amount (gross sales/payroll) must be greater than 0");
        }

        if (coverageForm == null || (!"Occurrence".equalsIgnoreCase(coverageForm) && !"ClaimsMade".equalsIgnoreCase(coverageForm))) {
            errors.add("GL_VALIDATION_ERROR: Coverage form must be either Occurrence or ClaimsMade");
        }

        return errors;
    }
}
