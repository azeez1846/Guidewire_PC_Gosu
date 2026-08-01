package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.OOSConflictResolver;
import com.guidewire.pc.service.OOSEndorsementEngine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 5: Out-of-Sequence (OOS) Endorsement & Slice Merge Engine Tests")
public class OutOfSequenceEndorsementTest {

    private PolicyPeriod initialPeriod;
    private PolicyPeriod boundEndorsement;

    @BeforeEach
    public void setUp() {
        GosuBridge.initGosuEngine(new File("."));

        initialPeriod = new PolicyPeriod();
        initialPeriod.setPolicyNumber("POL-OOS-5005");
        initialPeriod.setProductCode("PersonalAuto");
        initialPeriod.setEffectiveDate("2026-01-01");
        initialPeriod.setExpirationDate("2027-01-01");
        initialPeriod.setBodilyInjuryLimit("$250k/$500k");
        initialPeriod.setCollisionDeductible("$500");
        initialPeriod.setTotalPremium(new BigDecimal("1200.00"));

        boundEndorsement = new PolicyPeriod();
        boundEndorsement.setPolicyNumber("POL-OOS-5005");
        boundEndorsement.setEffectiveDate("2026-06-01");
        boundEndorsement.setExpirationDate("2027-01-01");
        boundEndorsement.setBodilyInjuryLimit("$500k/$1M");
        boundEndorsement.setCollisionDeductible("$250");
        boundEndorsement.setTotalPremium(new BigDecimal("1500.00"));
    }

    @Test
    @DisplayName("Test 1: Out-of-Sequence Detection for Backdated Change")
    public void testOOSDetection() {
        List<String> priorEndorsements = List.of("2026-06-01");

        // Backdated to 2026-03-01 (before 2026-06-01) -> OOS True
        boolean isOOS = OOSEndorsementEngine.isOutOfSequence("2026-03-01", priorEndorsements);
        assertTrue(isOOS, "Endorsement effective 2026-03-01 should be detected as Out-Of-Sequence");

        // In-sequence to 2026-08-01 (after 2026-06-01) -> OOS False
        boolean inSeq = OOSEndorsementEngine.isOutOfSequence("2026-08-01", priorEndorsements);
        assertFalse(inSeq, "Endorsement effective 2026-08-01 should be In-Sequence");
    }

    @Test
    @DisplayName("Test 2: OOS Branch Creation & Initialization")
    public void testOOSBranchCreation() {
        PolicyPeriod oosBranch = OOSEndorsementEngine.createOOSBranch(initialPeriod, "2026-03-01");

        assertNotNull(oosBranch);
        assertEquals("POL-OOS-5005", oosBranch.getPolicyNumber());
        assertEquals("2026-03-01", oosBranch.getEffectiveDate());
        assertEquals("2027-01-01", oosBranch.getExpirationDate());
        assertEquals("PolicyChange", oosBranch.getJobType());
        assertEquals("Draft", oosBranch.getStatus());
    }

    @Test
    @DisplayName("Test 3: OOS Conflict Detection Between OOS Branch & Bound Period")
    public void testOOSConflictDetection() {
        PolicyPeriod oosBranch = OOSEndorsementEngine.createOOSBranch(initialPeriod, "2026-03-01");
        oosBranch.setBodilyInjuryLimit("$100k/$300k"); // Modified limit on OOS branch

        List<String> conflicts = OOSConflictResolver.detectConflicts(oosBranch, boundEndorsement);

        assertNotNull(conflicts);
        assertFalse(conflicts.isEmpty());
        assertTrue(conflicts.stream().anyMatch(c -> c.contains("Bodily Injury Limit conflict")));
    }

    @Test
    @DisplayName("Test 4: OOS Slice Merge Forward Propagation")
    public void testOOSSliceMerge() {
        PolicyPeriod oosBranch = OOSEndorsementEngine.createOOSBranch(initialPeriod, "2026-03-01");
        oosBranch.setCollisionDeductible("$100"); // Updated deductible on OOS branch

        PolicyPeriod mergedPeriod = OOSEndorsementEngine.mergeOOSSlice(oosBranch, boundEndorsement);

        assertNotNull(mergedPeriod);
        assertEquals("$100", mergedPeriod.getCollisionDeductible(), "OOS slice change should propagate to latest period");
    }

    @Test
    @DisplayName("Test 5: OOS Prorated Premium Delta Calculation Across Effective Dates")
    public void testOOSPremiumDeltaCalculation() {
        PolicyPeriod oosBranch = OOSEndorsementEngine.createOOSBranch(initialPeriod, "2026-01-01");

        BigDecimal newAnnualPremium = new BigDecimal("1800.00"); // +600 annual increase
        BigDecimal delta = OOSConflictResolver.calculateOOSPremiumDelta(oosBranch, initialPeriod, newAnnualPremium);

        assertNotNull(delta);
        assertEquals(new BigDecimal("600.00"), delta);
    }
}
