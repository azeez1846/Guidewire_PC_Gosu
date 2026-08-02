package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class CURatingService {

    public static BigDecimal rateCommercialUmbrella(PolicyPeriod period, BigDecimal limitAmount, BigDecimal sirAmount, int underlyingPolicyCount) {
        if (period == null || limitAmount == null) return BigDecimal.ZERO;

        BigDecimal millions = limitAmount.divide(new BigDecimal("1000000.00"), 2, RoundingMode.HALF_UP);
        BigDecimal basePrem = new BigDecimal("1500.00");
        if (millions.compareTo(BigDecimal.ONE) > 0) {
            BigDecimal extraMillions = millions.subtract(BigDecimal.ONE);
            basePrem = basePrem.add(extraMillions.multiply(new BigDecimal("800.00")));
        }

        if (underlyingPolicyCount > 2) {
            BigDecimal surchargeFactor = new BigDecimal(1.00 + ((underlyingPolicyCount - 2) * 0.10));
            basePrem = basePrem.multiply(surchargeFactor).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal tax = basePrem.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
        return basePrem.add(tax);
    }

    public static List<String> validateCommercialUmbrellaLine(PolicyPeriod period, BigDecimal limitAmount, int underlyingPolicyCount) {
        List<String> errors = new ArrayList<>();
        if (period == null) return errors;

        if (limitAmount == null || limitAmount.compareTo(new BigDecimal("1000000.00")) < 0) {
            errors.add("UMBRELLA_VALIDATION_ERROR: Commercial Umbrella occurrence limit must be at least $1,000,000");
        }

        if (underlyingPolicyCount <= 0) {
            errors.add("UMBRELLA_VALIDATION_ERROR: Commercial Umbrella must have at least 1 schedule underlying policy (GL, Auto, or EL)");
        }

        return errors;
    }
}
