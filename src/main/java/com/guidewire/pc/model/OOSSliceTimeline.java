package com.guidewire.pc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class OOSSliceTimeline implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(OOSSliceTimeline.class.getName());

    private static final long serialVersionUID = 1L;

    private String policyNumber;
    private int sliceCount;
    private final List<PolicyPeriod> timelineSlices = new ArrayList<>();
    private final List<OOSConflict> detectedConflicts = new ArrayList<>();

    public OOSSliceTimeline() {
        LOGGER.log(Level.FINE, "→ OOSSliceTimeline.OOSSliceTimeline");}

    public OOSSliceTimeline(String policyNumber) {
        LOGGER.log(Level.FINE, "→ OOSSliceTimeline.OOSSliceTimeline");
        this.policyNumber = policyNumber;
    }

    public void addSlice(PolicyPeriod slice) {
        LOGGER.log(Level.FINE, "→ OOSSliceTimeline.addSlice");
        if (slice != null) {
            this.timelineSlices.add(slice);
            this.sliceCount = this.timelineSlices.size();
        }
    }

    public void addConflict(OOSConflict conflict) {
        LOGGER.log(Level.FINE, "→ OOSSliceTimeline.addConflict");
        if (conflict != null) {
            this.detectedConflicts.add(conflict);
        }
    }

    public String getPolicyNumber() {
        LOGGER.log(Level.FINE, "→ OOSSliceTimeline.getPolicyNumber"); return policyNumber; }
    public void setPolicyNumber(String policyNumber) {
        LOGGER.log(Level.FINE, "→ OOSSliceTimeline.setPolicyNumber"); this.policyNumber = policyNumber; }

    public int getSliceCount() {
        LOGGER.log(Level.FINE, "→ OOSSliceTimeline.getSliceCount"); return sliceCount; }
    public List<PolicyPeriod> getTimelineSlices() {
        LOGGER.log(Level.FINE, "→ OOSSliceTimeline.getTimelineSlices"); return timelineSlices; }
    public List<OOSConflict> getDetectedConflicts() {
        LOGGER.log(Level.FINE, "→ OOSSliceTimeline.getDetectedConflicts"); return detectedConflicts; }
}
