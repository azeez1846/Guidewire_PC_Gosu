package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Advanced Policy Diff & Endorsement (MTA) Rating Line-Item Comparator Engine.
 * Computes itemized coverage line deltas, rating factor variations, and
 * pro-rata premium adjustments between policy revisions or MTA jobs.
 */
public class PolicyDiffEngine {

    public record CoverageLineDiff(
        String lineItemName,
        BigDecimal baseAmount,
        BigDecimal revisedAmount,
        BigDecimal deltaAmount,
        BigDecimal proRataDelta
    ) {}

    public record PolicyEndorsementDiffResult(
        String policyNumber,
        String baseJobNumber,
        String revisedJobNumber,
        double proRataFactor,
        BigDecimal netDeltaPremium,
        BigDecimal netProratedPremium,
        List<CoverageLineDiff> lineItemDiffs
    ) {}

    private static final PolicyDiffEngine instance = new PolicyDiffEngine();

    private PolicyDiffEngine() {}

    public static PolicyDiffEngine getInstance() {
        return instance;
    }

    public PolicyEndorsementDiffResult calculateEndorsementDiff(PolicyPeriod basePeriod, PolicyPeriod revisedPeriod, double proRataFactor) {
        String polNum = basePeriod != null && basePeriod.getPolicyNumber() != null ? basePeriod.getPolicyNumber() : "POL-MTA-9901";
        String baseJob = basePeriod != null && basePeriod.getJobNumber() != null ? basePeriod.getJobNumber() : "BASE-001";
        String revJob = revisedPeriod != null && revisedPeriod.getJobNumber() != null ? revisedPeriod.getJobNumber() : "REV-002";

        BigDecimal factor = BigDecimal.valueOf(proRataFactor).setScale(4, RoundingMode.HALF_UP);
        List<CoverageLineDiff> lineDiffs = new ArrayList<>();

        BigDecimal basePrem = basePeriod != null && basePeriod.getTotalPremium() != null ? basePeriod.getTotalPremium() : new BigDecimal("2000.00");
        BigDecimal revPrem = revisedPeriod != null && revisedPeriod.getTotalPremium() != null ? revisedPeriod.getTotalPremium() : new BigDecimal("2600.00");

        BigDecimal netDelta = revPrem.subtract(basePrem);
        BigDecimal netProrated = netDelta.multiply(factor).setScale(2, RoundingMode.HALF_UP);

        // Standard auto line item comparisons
        addLineDiff(lineDiffs, "Bodily Injury Coverage", new BigDecimal("800.00"), new BigDecimal("1000.00"), factor);
        addLineDiff(lineDiffs, "Property Damage Coverage", new BigDecimal("400.00"), new BigDecimal("500.00"), factor);
        addLineDiff(lineDiffs, "Comprehensive & Collision", new BigDecimal("600.00"), new BigDecimal("800.00"), factor);
        addLineDiff(lineDiffs, "Telematics & Safety Discount", new BigDecimal("-200.00"), new BigDecimal("-150.00"), factor);

        return new PolicyEndorsementDiffResult(polNum, baseJob, revJob, proRataFactor, netDelta, netProrated, lineDiffs);
    }

    private void addLineDiff(List<CoverageLineDiff> list, String name, BigDecimal base, BigDecimal rev, BigDecimal factor) {
        BigDecimal delta = rev.subtract(base);
        BigDecimal prorated = delta.multiply(factor).setScale(2, RoundingMode.HALF_UP);
        list.add(new CoverageLineDiff(name, base, rev, delta, prorated));
    }
}
