package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.OOSConflictResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Out-of-Sequence (OOS) Conflict Resolution & Proration Tests")
public class OOSConflictResolutionExtendedTest {

    @Test
    @DisplayName("Should detect conflicts between OOS retroactive branch and latest bound period")
    void testConflictDetection() {
        PolicyPeriod oosBranch = new PolicyPeriod();
        oosBranch.setBodilyInjuryLimit("$1M/$1M");
        oosBranch.setCollisionDeductible("$500");

        PolicyPeriod latestPeriod = new PolicyPeriod();
        latestPeriod.setBodilyInjuryLimit("$500k/$500k");
        latestPeriod.setCollisionDeductible("$1000");

        var conflicts = OOSConflictResolver.detectConflicts(oosBranch, latestPeriod);
        assertNotNull(conflicts);
        assertEquals(2, conflicts.size(), "Both BI limit and Collision deductible should register OOS conflicts");
    }

    @Test
    @DisplayName("Should resolve OOS branch with auto merge forward strategy")
    void testResolveOOSBranchAutoMerge() {
        PolicyPeriod oosBranch = new PolicyPeriod();
        oosBranch.setPolicyNumber("POL-OOS-900");
        oosBranch.setEffectiveDate("2026-06-01");
        oosBranch.setExpirationDate("2027-01-01");
        oosBranch.setTotalPremium(new BigDecimal("5000.00"));

        PolicyPeriod latestPeriod = new PolicyPeriod();
        latestPeriod.setPolicyNumber("POL-OOS-900");
        latestPeriod.setTotalPremium(new BigDecimal("4000.00"));

        var mergeResult = OOSConflictResolver.resolveOOSBranch(oosBranch, latestPeriod, "AUTO_MERGE_FORWARD");

        assertNotNull(mergeResult);
        assertTrue(mergeResult.mergedSuccessfully);
        assertNotNull(mergeResult.timelineSummary);
    }

    @Test
    @DisplayName("Should accurately calculate detailed pro-rata and short-rate cancellation refunds")
    void testProrationRefundDetailed() {
        BigDecimal annualPrem = new BigDecimal("3650.00"); // $10/day
        String effDate = "2026-01-01";
        String canDate = "2026-07-01"; // 181 days in force, 184 unearned
        String expDate = "2027-01-01"; // 365 total days

        // Standard Pro-Rata
        Map<String, Object> proRata = OOSConflictResolver.calculateProrationRefundDetailed(annualPrem, effDate, canDate, expDate, false);
        assertEquals("SUCCESS", proRata.get("status"));
        assertEquals(0, ((BigDecimal) proRata.get("shortRatePenalty")).compareTo(BigDecimal.ZERO));
        assertTrue(((BigDecimal) proRata.get("netRefundPayable")).compareTo(new BigDecimal("1800.00")) > 0);

        // Short-Rate (10% penalty)
        Map<String, Object> shortRate = OOSConflictResolver.calculateProrationRefundDetailed(annualPrem, effDate, canDate, expDate, true);
        assertEquals("SUCCESS", shortRate.get("status"));
        assertTrue((Boolean) shortRate.get("isShortRate"));
        assertTrue(((BigDecimal) shortRate.get("shortRatePenalty")).compareTo(BigDecimal.ZERO) > 0);
        assertTrue(((BigDecimal) shortRate.get("netRefundPayable")).compareTo((BigDecimal) proRata.get("netRefundPayable")) < 0);
    }
}
