package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Out-of-Sequence (OOS) Endorsement Timeline Slice Visualizer Service.
 */
public class OOSTimelineVisualizerService {
    private static final Logger LOGGER = Logger.getLogger(OOSTimelineVisualizerService.class.getName());
    private static final OOSTimelineVisualizerService instance = new OOSTimelineVisualizerService();

    private OOSTimelineVisualizerService() {
        LOGGER.log(Level.FINE, "→ OOSTimelineVisualizerService.OOSTimelineVisualizerService");}

    public static OOSTimelineVisualizerService getInstance() {
        LOGGER.log(Level.FINE, "→ OOSTimelineVisualizerService.getInstance");
        return instance;
    }

    public Map<String, Object> generateTimelineSlices(String jobNumber) {
        LOGGER.log(Level.FINE, "→ OOSTimelineVisualizerService.generateTimelineSlices");
        PolicyPeriod period = DataStoreService.getInstance().findSubmission(jobNumber);
        if (period == null) {
            period = DataStoreService.getInstance().findSubmission("S0001001");
        }

        LOGGER.info("[OOS Visualizer Engine] Generating effective timeline slices for job: " + (period != null ? period.getJobNumber() : "S0001001"));

        List<Map<String, Object>> slices = new ArrayList<>();

        // Slice 1: Inception
        Map<String, Object> slice1 = new HashMap<>();
        slice1.put("sliceIndex", 1);
        slice1.put("effectiveDate", "2026-01-01");
        slice1.put("expirationDate", "2026-04-01");
        slice1.put("description", "Policy Bound at Inception (Base Term)");
        slice1.put("writtenPremium", new BigDecimal("12500.00"));
        slice1.put("proRataFactor", 0.25);
        slice1.put("status", "COMMITTED");
        slices.add(slice1);

        // Slice 2: Backdated Endorsement OOS
        Map<String, Object> slice2 = new HashMap<>();
        slice2.put("sliceIndex", 2);
        slice2.put("effectiveDate", "2026-04-01");
        slice2.put("expirationDate", "2026-09-01");
        slice2.put("description", "Out-of-Sequence (OOS) Endorsement: Vehicle Schedule Added (Backdated)");
        slice2.put("writtenPremium", new BigDecimal("4200.00"));
        slice2.put("proRataFactor", 0.42);
        slice2.put("status", "MERGED_OOS_SLICE");
        slices.add(slice2);

        // Slice 3: Renewal Term
        Map<String, Object> slice3 = new HashMap<>();
        slice3.put("sliceIndex", 3);
        slice3.put("effectiveDate", "2026-09-01");
        slice3.put("expirationDate", "2027-01-01");
        slice3.put("description", "Current Effective Term Slice");
        slice3.put("writtenPremium", new BigDecimal("3300.00"));
        slice3.put("proRataFactor", 0.33);
        slice3.put("status", "ACTIVE_TERM_SLICE");
        slices.add(slice3);

        Map<String, Object> response = new HashMap<>();
        response.put("jobNumber", period != null ? period.getJobNumber() : "S0001001");
        response.put("policyNumber", period != null ? period.getPolicyNumber() : "POL-CA-991001");
        response.put("oosConflictDetected", true);
        response.put("conflictResolutionStrategy", "AUTOMATIC_TIMELINE_MERGE");
        response.put("timelineSlices", slices);
        response.put("totalPolicyTermPremium", new BigDecimal("20000.00"));

        return response;
    }
}
