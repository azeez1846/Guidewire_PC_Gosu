package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class OOSConflictResolver {
    private static final Logger LOGGER = Logger.getLogger(OOSConflictResolver.class.getName());


    public static List<String> detectConflicts(PolicyPeriod oosBranch, PolicyPeriod latestPeriod) {
        LOGGER.log(Level.FINE, "→ OOSConflictResolver.detectConflicts");
        List<String> conflicts = new ArrayList<>();
        if (oosBranch == null || latestPeriod == null) return conflicts;

        if (oosBranch.getBodilyInjuryLimit() != null && !oosBranch.getBodilyInjuryLimit().equalsIgnoreCase(latestPeriod.getBodilyInjuryLimit())) {
            conflicts.add("OOS_CONFLICT: Bodily Injury Limit conflict between OOS Branch (" + oosBranch.getBodilyInjuryLimit() + ") and Latest Bound Period (" + latestPeriod.getBodilyInjuryLimit() + ")");
        }

        if (oosBranch.getCollisionDeductible() != null && !oosBranch.getCollisionDeductible().equalsIgnoreCase(latestPeriod.getCollisionDeductible())) {
            conflicts.add("OOS_CONFLICT: Collision Deductible conflict between OOS Branch (" + oosBranch.getCollisionDeductible() + ") and Latest Bound Period (" + latestPeriod.getCollisionDeductible() + ")");
        }

        return conflicts;
    }

    public static BigDecimal calculateOOSPremiumDelta(PolicyPeriod oosBranch, PolicyPeriod latestPeriod, BigDecimal newAnnualPremium) {
        LOGGER.log(Level.FINE, "→ OOSConflictResolver.calculateOOSPremiumDelta");
        if (oosBranch == null || latestPeriod == null || newAnnualPremium == null) return BigDecimal.ZERO;

        try {
            LocalDate eff = LocalDate.parse(oosBranch.getEffectiveDate());
            LocalDate exp = LocalDate.parse(oosBranch.getExpirationDate());
            long totalTermDays = ChronoUnit.DAYS.between(eff, exp);
            long remainingDays = ChronoUnit.DAYS.between(eff, exp);

            if (totalTermDays <= 0) return BigDecimal.ZERO;

            BigDecimal origPrem = latestPeriod.getTotalPremium() != null ? latestPeriod.getTotalPremium() : BigDecimal.ZERO;
            BigDecimal annualDelta = newAnnualPremium.subtract(origPrem);
            BigDecimal dayRatio = new BigDecimal(remainingDays).divide(new BigDecimal(totalTermDays), 6, RoundingMode.HALF_UP);
            BigDecimal proratedDelta = annualDelta.multiply(dayRatio).setScale(2, RoundingMode.HALF_UP);

            return proratedDelta;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
