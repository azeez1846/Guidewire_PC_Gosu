package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class OOSConflictResolver {
    private static final Logger LOGGER = Logger.getLogger(OOSConflictResolver.class.getName());

    public static class OOSMergeResult {
        public String policyNumber;
        public boolean hasConflicts;
        public List<String> detectedConflicts = new ArrayList<>();
        public String resolutionStrategy; // AUTO_MERGE_FORWARD, OVERWRITE, REQUIRES_MANUAL_UW
        public BigDecimal originalAnnualPremium;
        public BigDecimal retroactiveSlicePremiumDelta;
        public BigDecimal finalAdjustedAnnualPremium;
        public boolean mergedSuccessfully;
        public String timelineSummary;
    }

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

    public static OOSMergeResult resolveOOSBranch(PolicyPeriod oosBranch, PolicyPeriod latestBoundPeriod, String resolutionStrategy) {
        LOGGER.log(Level.FINE, "→ OOSConflictResolver.resolveOOSBranch");
        OOSMergeResult result = new OOSMergeResult();
        result.policyNumber = latestBoundPeriod != null ? latestBoundPeriod.getPolicyNumber() : "POL-OOS-001";
        result.detectedConflicts = detectConflicts(oosBranch, latestBoundPeriod);
        result.hasConflicts = !result.detectedConflicts.isEmpty();
        result.resolutionStrategy = resolutionStrategy != null ? resolutionStrategy : "AUTO_MERGE_FORWARD";

        BigDecimal origPrem = latestBoundPeriod != null && latestBoundPeriod.getTotalPremium() != null ? latestBoundPeriod.getTotalPremium() : new BigDecimal("4500.00");
        result.originalAnnualPremium = origPrem;

        if ("OVERWRITE".equalsIgnoreCase(result.resolutionStrategy) || !result.hasConflicts) {
            result.mergedSuccessfully = true;
            BigDecimal newPrem = oosBranch != null && oosBranch.getTotalPremium() != null ? oosBranch.getTotalPremium() : origPrem.add(new BigDecimal("500.00"));
            result.retroactiveSlicePremiumDelta = calculateOOSPremiumDelta(oosBranch, latestBoundPeriod, newPrem);
            result.finalAdjustedAnnualPremium = origPrem.add(result.retroactiveSlicePremiumDelta);
            result.timelineSummary = "OOS Branch merged forward cleanly into active policy slice timeline.";
        } else {
            result.mergedSuccessfully = false;
            result.retroactiveSlicePremiumDelta = BigDecimal.ZERO;
            result.finalAdjustedAnnualPremium = origPrem;
            result.timelineSummary = "OOS Branch contains " + result.detectedConflicts.size() + " attribute conflicts requiring underwriter adjudication.";
        }

        return result;
    }

    public static Map<String, Object> calculateProrationRefundDetailed(BigDecimal annualPremium, String effectiveDateStr, String cancellationDateStr, String expirationDateStr, boolean isShortRate) {
        LOGGER.log(Level.FINE, "→ OOSConflictResolver.calculateProrationRefundDetailed");
        Map<String, Object> res = new HashMap<>();
        try {
            LocalDate eff = LocalDate.parse(effectiveDateStr);
            LocalDate can = LocalDate.parse(cancellationDateStr);
            LocalDate exp = LocalDate.parse(expirationDateStr);

            long totalDays = ChronoUnit.DAYS.between(eff, exp);
            long daysInForce = ChronoUnit.DAYS.between(eff, can);
            long unearnedDays = Math.max(0, totalDays - daysInForce);

            if (totalDays <= 0) totalDays = 365;

            BigDecimal earnedFraction = new BigDecimal(daysInForce).divide(new BigDecimal(totalDays), 6, RoundingMode.HALF_UP);
            BigDecimal unearnedFraction = new BigDecimal(unearnedDays).divide(new BigDecimal(totalDays), 6, RoundingMode.HALF_UP);

            BigDecimal earnedPremium = annualPremium.multiply(earnedFraction).setScale(2, RoundingMode.HALF_UP);
            BigDecimal grossRefund = annualPremium.multiply(unearnedFraction).setScale(2, RoundingMode.HALF_UP);

            BigDecimal penalty = BigDecimal.ZERO;
            BigDecimal netRefund = grossRefund;

            if (isShortRate) {
                // Short rate cancellation applies 10% penalty on unearned refund (90% rule)
                penalty = grossRefund.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
                netRefund = grossRefund.subtract(penalty);
            }

            res.put("annualPremium", annualPremium);
            res.put("totalPolicyDays", totalDays);
            res.put("daysInForce", daysInForce);
            res.put("unearnedDays", unearnedDays);
            res.put("earnedPremium", earnedPremium);
            res.put("grossRefund", grossRefund);
            res.put("isShortRate", isShortRate);
            res.put("shortRatePenalty", penalty);
            res.put("netRefundPayable", netRefund);
            res.put("status", "SUCCESS");
        } catch (Exception e) {
            res.put("error", "Failed to calculate proration: " + e.getMessage());
            res.put("status", "ERROR");
        }
        return res;
    }
}
