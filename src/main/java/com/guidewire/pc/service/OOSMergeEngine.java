package com.guidewire.pc.service;

import com.guidewire.pc.model.OOSConflict;
import com.guidewire.pc.model.OOSSliceTimeline;
import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OOSMergeEngine {
    private static final Logger LOGGER = Logger.getLogger(OOSMergeEngine.class.getName());
    private static final OOSMergeEngine instance = new OOSMergeEngine();

    private final DataStoreService dataStore = DataStoreService.getInstance();

    private OOSMergeEngine() {}

    public static OOSMergeEngine getInstance() {
        return instance;
    }

    /**
     * Executes Out-Of-Sequence (OOS) Endorsement Timeline Splitting & Forward Merging
     */
    public OOSSliceTimeline processOOSEndorsement(String policyNumber, String backdatedEffectiveDateStr, String newBiLimit, String newCollisionDed) {
        PolicyPeriod basePeriod = dataStore.findPolicyByPolicyNumber(policyNumber);
        if (basePeriod == null) {
            throw new IllegalArgumentException("Policy not found for OOS merge: " + policyNumber);
        }

        OOSSliceTimeline timeline = new OOSSliceTimeline(policyNumber);

        // Slice 1: Initial Term Slice [T0 -> Backdated Date]
        PolicyPeriod slice1 = basePeriod.copySubmissionBranch("OOS01_" + (System.currentTimeMillis() % 89999 + 10000));
        slice1.setEffectiveDate(basePeriod.getEffectiveDate());
        slice1.setExpirationDate(backdatedEffectiveDateStr);
        slice1.setTotalPremium(basePeriod.getTotalPremium() != null ? basePeriod.getTotalPremium().multiply(new BigDecimal("0.40")).setScale(2, RoundingMode.HALF_UP) : new BigDecimal("1000.00"));
        timeline.addSlice(slice1);

        // Slice 2: Backdated Out-Of-Sequence Slice [Backdated Date -> Mid-Term Date]
        PolicyPeriod slice2 = basePeriod.copySubmissionBranch("OOS02_" + (System.currentTimeMillis() % 89999 + 10000));
        slice2.setEffectiveDate(backdatedEffectiveDateStr);
        slice2.setExpirationDate("2026-09-01");
        slice2.setBodilyInjuryLimit(newBiLimit != null ? newBiLimit : basePeriod.getBodilyInjuryLimit());
        slice2.setCollisionDeductible(newCollisionDed != null ? newCollisionDed : basePeriod.getCollisionDeductible());
        RatingEngine.getInstance().rate(slice2);
        timeline.addSlice(slice2);

        // Slice 3: Later Bound Endorsement Slice [Mid-Term Date -> Expiration]
        PolicyPeriod slice3 = basePeriod.copySubmissionBranch("OOS03_" + (System.currentTimeMillis() % 89999 + 10000));
        slice3.setEffectiveDate("2026-09-01");
        slice3.setExpirationDate(basePeriod.getExpirationDate());

        // Forward Merge Check & Conflict Resolution
        if (slice2.getBodilyInjuryLimit() != null && !slice2.getBodilyInjuryLimit().equals(slice3.getBodilyInjuryLimit())) {
            OOSConflict conflict = new OOSConflict(
                    "BodilyInjuryLimit",
                    backdatedEffectiveDateStr,
                    slice2.getBodilyInjuryLimit(),
                    "2026-09-01",
                    slice3.getBodilyInjuryLimit()
            );
            // Auto forward merge value to slice 3
            slice3.setBodilyInjuryLimit(slice2.getBodilyInjuryLimit());
            conflict.setResolutionStatus("FORWARD_MERGED");
            timeline.addConflict(conflict);
        }

        RatingEngine.getInstance().rate(slice3);
        timeline.addSlice(slice3);

        dataStore.createSubmission(slice2);
        dataStore.createSubmission(slice3);

        LOGGER.log(Level.INFO, "OOS Endorsement split & merge completed for policy {0}: {1} slices created, {2} conflicts forward-merged.",
                new Object[]{policyNumber, timeline.getSliceCount(), timeline.getDetectedConflicts().size()});

        return timeline;
    }
}
