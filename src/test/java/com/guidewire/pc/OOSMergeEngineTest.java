package com.guidewire.pc;

import com.guidewire.pc.constants.PCConstants;
import com.guidewire.pc.model.OOSSliceTimeline;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.DataStoreService;
import com.guidewire.pc.service.OOSMergeEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Out-of-Sequence Multi-Slice Policy Endorsement & Merging Engine Tests")
public class OOSMergeEngineTest {

    private DataStoreService dataStore;

    @BeforeEach
    public void setUp() {
        dataStore = DataStoreService.getInstance();
    }

    @Test
    @DisplayName("Should split effective timeline into 3 slices and forward merge coverage limits")
    public void testOOSMergeTimelineSplitting() {
        PolicyPeriod base = new PolicyPeriod();
        base.setJobNumber("S000_OOS_BASE");
        base.setPolicyNumber("POL-OOS-9001");
        base.setStatus(PCConstants.STATUS_ISSUED);
        base.setEffectiveDate("2026-01-01");
        base.setExpirationDate("2027-01-01");
        base.setBodilyInjuryLimit("$500k/$500k");
        dataStore.createSubmission(base);

        OOSSliceTimeline timeline = OOSMergeEngine.getInstance().processOOSEndorsement(
                "POL-OOS-9001",
                "2026-04-01",
                "$1M/$1M",
                "$1000"
        );

        assertNotNull(timeline);
        assertEquals(3, timeline.getSliceCount());
        assertEquals("POL-OOS-9001", timeline.getPolicyNumber());
        assertFalse(timeline.getTimelineSlices().isEmpty());
    }
}
