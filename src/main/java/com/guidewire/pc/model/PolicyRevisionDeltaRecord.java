package com.guidewire.pc.model;

import java.math.BigDecimal;
import java.util.SequencedCollection;

public record PolicyRevisionDeltaRecord(
        String policyNumber,
        String revisionNumber,
        String jobType,
        BigDecimal originalPremium,
        BigDecimal revisedPremium,
        BigDecimal deltaAmount,
        SequencedCollection<FieldChange> fieldChanges
) {
    public record FieldChange(
            String fieldName,
            String oldValue,
            String newValue,
            String category
    ) {}

    public boolean isPremiumIncrease() {
        return deltaAmount != null && deltaAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isPremiumDecrease() {
        return deltaAmount != null && deltaAmount.compareTo(BigDecimal.ZERO) < 0;
    }

    public static String formatChangeSummary(Object changeObj) {
        if (changeObj instanceof FieldChange(String name, String oldVal, String newVal, String cat)) {
            return String.format("[%s] %s changed from '%s' to '%s'", cat, name, oldVal, newVal);
        } else if (changeObj instanceof PolicyRevisionDeltaRecord(String pol, String rev, String job, BigDecimal origP, BigDecimal revP, BigDecimal delta, SequencedCollection<FieldChange> changes)) {
            return String.format("Policy %s (Revision %s - %s): Premium $%s -> $%s (Delta $%s) across %d changes", pol, rev, job, origP, revP, delta, changes.size());
        }
        return "Unknown Policy Revision Format";
    }
}
