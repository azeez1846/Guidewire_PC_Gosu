package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PolicyDiffService {
    private static final Logger LOGGER = Logger.getLogger(PolicyDiffService.class.getName());
    private static final PolicyDiffService instance = new PolicyDiffService();

    public record FieldChange(String fieldName, String oldValue, String newValue, boolean isFinancial) {}

    public record PolicyDiffReport(String policyNumber, String baseJobNumber, String compareJobNumber, List<FieldChange> changes) {}

    private PolicyDiffService() {}

    public static PolicyDiffService getInstance() {
        return instance;
    }

    public PolicyDiffReport compareRevisions(PolicyPeriod basePeriod, PolicyPeriod comparePeriod) {
        if (basePeriod == null || comparePeriod == null) {
            throw new IllegalArgumentException("Both policy periods must be non-null for comparison");
        }

        List<FieldChange> diffs = new ArrayList<>();

        compareField(diffs, "Job Type", basePeriod.getJobType(), comparePeriod.getJobType(), false);
        compareField(diffs, "Status", basePeriod.getStatus(), comparePeriod.getStatus(), false);
        compareField(diffs, "Bodily Injury Limit", basePeriod.getBodilyInjuryLimit(), comparePeriod.getBodilyInjuryLimit(), false);
        compareField(diffs, "Property Damage Limit", basePeriod.getPropertyDamageLimit(), comparePeriod.getPropertyDamageLimit(), false);
        compareField(diffs, "Collision Deductible", basePeriod.getCollisionDeductible(), comparePeriod.getCollisionDeductible(), false);
        compareField(diffs, "Comprehensive Deductible", basePeriod.getComprehensiveDeductible(), comparePeriod.getComprehensiveDeductible(), false);
        compareField(diffs, "Base Premium", Objects.toString(basePeriod.getBasePremium(), "$0.00"), Objects.toString(comparePeriod.getBasePremium(), "$0.00"), true);
        compareField(diffs, "Total Premium", Objects.toString(basePeriod.getTotalPremium(), "$0.00"), Objects.toString(comparePeriod.getTotalPremium(), "$0.00"), true);

        LOGGER.log(Level.INFO, "Generated Policy Diff Report for policy: {0} Total changes: {1}",
                new Object[]{basePeriod.getPolicyNumber(), diffs.size()});
        return new PolicyDiffReport(basePeriod.getPolicyNumber(), basePeriod.getJobNumber(), comparePeriod.getJobNumber(), diffs);
    }

    private void compareField(List<FieldChange> list, String fieldName, String oldVal, String newVal, boolean isFinancial) {
        if (!Objects.equals(oldVal, newVal)) {
            list.add(new FieldChange(fieldName, oldVal != null ? oldVal : "N/A", newVal != null ? newVal : "N/A", isFinancial));
        }
    }
}
