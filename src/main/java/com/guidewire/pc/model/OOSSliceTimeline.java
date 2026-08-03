package com.guidewire.pc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OOSSliceTimeline implements Serializable {
    private static final long serialVersionUID = 1L;

    private String policyNumber;
    private int sliceCount;
    private final List<PolicyPeriod> timelineSlices = new ArrayList<>();
    private final List<OOSConflict> detectedConflicts = new ArrayList<>();

    public OOSSliceTimeline() {}

    public OOSSliceTimeline(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public void addSlice(PolicyPeriod slice) {
        if (slice != null) {
            this.timelineSlices.add(slice);
            this.sliceCount = this.timelineSlices.size();
        }
    }

    public void addConflict(OOSConflict conflict) {
        if (conflict != null) {
            this.detectedConflicts.add(conflict);
        }
    }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

    public int getSliceCount() { return sliceCount; }
    public List<PolicyPeriod> getTimelineSlices() { return timelineSlices; }
    public List<OOSConflict> getDetectedConflicts() { return detectedConflicts; }
}
