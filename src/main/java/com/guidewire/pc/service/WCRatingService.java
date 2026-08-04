package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class WCRatingService {
    private static final Logger LOGGER = Logger.getLogger(WCRatingService.class.getName());


    public static BigDecimal rateWorkersComp(PolicyPeriod period, BigDecimal estimatedPayroll, BigDecimal expMod, BigDecimal classRate) {
        LOGGER.log(Level.FINE, "→ WCRatingService.rateWorkersComp");
        return rateWorkersCompWithSpecialClassCodes(period, estimatedPayroll, expMod, classRate, null, null, null, false);
    }

    public static BigDecimal rateWorkersCompWithSpecialClassCodes(PolicyPeriod period, BigDecimal estimatedPayroll, BigDecimal expMod, BigDecimal classRate,
                                                                 String specialClassCode, BigDecimal specialClassPayroll, BigDecimal specialClassRate,
                                                                 boolean hasSafetyProgram) {
        LOGGER.log(Level.FINE, "→ WCRatingService.rateWorkersCompWithSpecialClassCodes");
        if (period == null) return BigDecimal.ZERO;

        BigDecimal payroll = estimatedPayroll != null ? estimatedPayroll : new BigDecimal("100000.00");
        BigDecimal rate = classRate != null ? classRate : new BigDecimal("2.50");
        BigDecimal emod = expMod != null ? expMod : new BigDecimal("0.950");

        BigDecimal unitsOf100 = payroll.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);
        BigDecimal manualPrem = unitsOf100.multiply(rate).setScale(2, RoundingMode.HALF_UP);

        // Special Class Code Add-on Rating (e.g. Code 8810 Clerical, Code 5606 Executive)
        if (specialClassCode != null && specialClassPayroll != null && specialClassPayroll.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal scRate = specialClassRate != null ? specialClassRate : new BigDecimal("0.45");
            BigDecimal scUnits = specialClassPayroll.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);
            BigDecimal scPrem = scUnits.multiply(scRate).setScale(2, RoundingMode.HALF_UP);
            manualPrem = manualPrem.add(scPrem);
        }

        BigDecimal standardPrem = manualPrem.multiply(emod).setScale(2, RoundingMode.HALF_UP);

        // OSHA Safety Program Discount (5% reduction)
        if (hasSafetyProgram) {
            standardPrem = standardPrem.multiply(new BigDecimal("0.95")).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal taxAndExpense = standardPrem.multiply(new BigDecimal("0.08")).setScale(2, RoundingMode.HALF_UP);

        return standardPrem.add(taxAndExpense);
    }

    public static List<String> validateWorkersCompLine(PolicyPeriod period, BigDecimal estimatedPayroll, BigDecimal expMod) {
        LOGGER.log(Level.FINE, "→ WCRatingService.validateWorkersCompLine");
        return validateWorkersCompLineDetails(period, estimatedPayroll, expMod, null, null);
    }

    public static List<String> validateWorkersCompLineDetails(PolicyPeriod period, BigDecimal estimatedPayroll, BigDecimal expMod,
                                                               String specialClassCode, BigDecimal specialClassPayroll) {
        LOGGER.log(Level.FINE, "→ WCRatingService.validateWorkersCompLineDetails");
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

        if (specialClassCode != null && (specialClassPayroll == null || specialClassPayroll.compareTo(BigDecimal.ZERO) < 0)) {
            errors.add("WC_VALIDATION_ERROR: Special Class Code " + specialClassCode + " requires non-negative allocated payroll");
        }

        return errors;
    }
}
