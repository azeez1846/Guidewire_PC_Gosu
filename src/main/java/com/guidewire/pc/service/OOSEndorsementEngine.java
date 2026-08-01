package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.time.LocalDate;
import java.util.List;

public class OOSEndorsementEngine {

    public static boolean isOutOfSequence(String oosEffDate, List<String> priorEndorsementEffDates) {
        if (oosEffDate == null || priorEndorsementEffDates == null || priorEndorsementEffDates.isEmpty()) return false;

        try {
            LocalDate current = LocalDate.parse(oosEffDate);
            for (String d : priorEndorsementEffDates) {
                LocalDate prior = LocalDate.parse(d);
                if (current.isBefore(prior)) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    public static PolicyPeriod createOOSBranch(PolicyPeriod period, String oosEffDate) {
        if (period == null) return null;

        PolicyPeriod oosBranch = new PolicyPeriod();
        oosBranch.setPolicyNumber(period.getPolicyNumber());
        oosBranch.setProductCode(period.getProductCode());
        oosBranch.setAccount(period.getAccount());
        oosBranch.setJobType("PolicyChange");
        oosBranch.setStatus("Draft");
        oosBranch.setEffectiveDate(oosEffDate != null ? oosEffDate : period.getEffectiveDate());
        oosBranch.setExpirationDate(period.getExpirationDate());
        oosBranch.setBaseState(period.getBaseState());
        oosBranch.setBodilyInjuryLimit(period.getBodilyInjuryLimit());
        oosBranch.setPropertyDamageLimit(period.getPropertyDamageLimit());
        oosBranch.setCollisionDeductible(period.getCollisionDeductible());
        oosBranch.setComprehensiveDeductible(period.getComprehensiveDeductible());
        oosBranch.setTotalPremium(period.getTotalPremium());

        return oosBranch;
    }

    public static PolicyPeriod mergeOOSSlice(PolicyPeriod oosBranch, PolicyPeriod latestPeriod) {
        if (oosBranch == null || latestPeriod == null) return latestPeriod;

        latestPeriod.setBodilyInjuryLimit(oosBranch.getBodilyInjuryLimit());
        latestPeriod.setPropertyDamageLimit(oosBranch.getPropertyDamageLimit());
        latestPeriod.setCollisionDeductible(oosBranch.getCollisionDeductible());
        latestPeriod.setComprehensiveDeductible(oosBranch.getComprehensiveDeductible());

        return latestPeriod;
    }
}
