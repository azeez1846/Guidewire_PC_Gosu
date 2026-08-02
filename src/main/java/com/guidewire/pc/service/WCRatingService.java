package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class WCRatingService {

    public static BigDecimal rateWorkersComp(PolicyPeriod period, BigDecimal estimatedPayroll, BigDecimal expMod, BigDecimal classRate) {
        if (period == null) return BigDecimal.ZERO;

        BigDecimal payroll = estimatedPayroll != null ? estimatedPayroll : new BigDecimal("100000.00");
        BigDecimal rate = classRate != null ? classRate : new BigDecimal("2.50");
        BigDecimal emod = expMod != null ? expMod : new BigDecimal("0.950");

        BigDecimal unitsOf100 = payroll.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);
        BigDecimal manualPrem = unitsOf100.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal standardPrem = manualPrem.multiply(emod).setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxAndExpense = standardPrem.multiply(new BigDecimal("0.08")).setScale(2, RoundingMode.HALF_UP);

        return standardPrem.add(taxAndExpense);
    }

    public static List<String> validateWorkersCompLine(PolicyPeriod period, BigDecimal estimatedPayroll, BigDecimal expMod) {
        List<String> errors = new ArrayList<>();
        if (period == null) return errors;

        if (estimatedPayroll == null || estimatedPayroll.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("WC_VALIDATION_ERROR: Estimated annual payroll must be greater than 0");
        }

        if (expMod != null && (expMod.compareTo(new BigDecimal("0.400")) < 0 || expMod.compareTo(new BigDecimal("2.500")) > 0)) {
            errors.add("WC_VALIDATION_ERROR: Experience Modifier " + expMod + " is outside acceptable NCCI range (0.400 - 2.500)");
        }

        if (period.getBaseState() == null || period.getBaseState().trim().isEmpty()) {
            errors.add("WC_VALIDATION_ERROR: Workers Compensation primary jurisdiction base state is required");
        }

        return errors;
    }
}
