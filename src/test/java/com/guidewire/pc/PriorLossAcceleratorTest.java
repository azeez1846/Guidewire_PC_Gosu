package com.guidewire.pc;

import com.guidewire.pc.service.PriorLossService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Guidewire Accelerator #8: Prior Loss & C.L.U.E. History Tests")
public class PriorLossAcceleratorTest {

    @Test
    @DisplayName("Should reward clean 3-year loss history with preferred discount credit")
    void testCleanPriorLossHistory() {
        var report = PriorLossService.getInstance().retrievePriorLossHistory("TAX-94-1829104-CLEAN", new BigDecimal("50000.00"));

        assertNotNull(report);
        assertEquals(0, report.totalClaimsCount);
        assertEquals(BigDecimal.ZERO, report.totalIncurredAmount);
        assertEquals(0.0, report.lossRatioPct);
        assertEquals(new BigDecimal("0.85"), report.lossModifierFactor);
        assertFalse(report.requiresUnderwriterReferral);
    }

    @Test
    @DisplayName("Should penalize adverse multi-claim history with surcharge and UW referral trigger")
    void testAdversePriorLossHistory() {
        var report = PriorLossService.getInstance().retrievePriorLossHistory("TAX-94-9999999-ADVERSE", new BigDecimal("40000.00"));

        assertNotNull(report);
        assertTrue(report.totalClaimsCount >= 3);
        assertTrue(report.totalIncurredAmount.compareTo(new BigDecimal("50000.00")) > 0);
        assertTrue(report.lossRatioPct > 50.0);
        assertEquals(new BigDecimal("1.30"), report.lossModifierFactor); // +30% debit
        assertTrue(report.requiresUnderwriterReferral);
        assertNotNull(report.underwriterReferralReason);
    }

    @Test
    @DisplayName("Should convert Prior Loss report to response map")
    void testToMapConversion() {
        var report = PriorLossService.getInstance().retrievePriorLossHistory("TAX-STANDARD", new BigDecimal("30000.00"));
        var map = PriorLossService.getInstance().toMap(report);

        assertEquals("SUCCESS", map.get("status"));
        assertEquals("TAX-STANDARD", map.get("searchKey"));
        assertNotNull(map.get("provider"));
        assertEquals(1, map.get("totalClaimsCount"));
    }
}
