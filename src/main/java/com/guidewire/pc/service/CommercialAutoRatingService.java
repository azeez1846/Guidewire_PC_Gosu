package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class CommercialAutoRatingService {

    public static BigDecimal rateCommercialAuto(PolicyPeriod period, int vehicleCount, boolean isFleet, String radius) {
        if (period == null || vehicleCount <= 0) return BigDecimal.ZERO;

        double basePerVehicle = 1200.00;
        if ("LongDistance".equalsIgnoreCase(radius)) {
            basePerVehicle = 2200.00;
        } else if ("Intermediate".equalsIgnoreCase(radius)) {
            basePerVehicle = 1600.00;
        }

        BigDecimal totalBase = new BigDecimal(basePerVehicle * vehicleCount);

        if (isFleet || vehicleCount >= 5) {
            totalBase = totalBase.multiply(new BigDecimal("0.90")).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal tax = totalBase.multiply(new BigDecimal("0.06")).setScale(2, RoundingMode.HALF_UP);
        return totalBase.add(tax);
    }

    public static List<String> validateCommercialAutoLine(PolicyPeriod period, int vehicleCount, String radius) {
        List<String> errors = new ArrayList<>();
        if (period == null) return errors;

        if (vehicleCount <= 0) {
            errors.add("AUTO_VALIDATION_ERROR: Commercial Auto policy must have at least 1 registered vehicle");
        }

        if (radius == null || radius.trim().isEmpty()) {
            errors.add("AUTO_VALIDATION_ERROR: Radius of operation is required (Local, Intermediate, LongDistance)");
        }

        return errors;
    }
}
