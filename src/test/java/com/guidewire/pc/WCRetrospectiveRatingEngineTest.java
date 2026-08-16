package com.guidewire.pc;

import com.guidewire.pc.service.WCRetrospectiveRatingEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Workers' Compensation NCCI Retrospective Rating Engine Tests")
public class WCRetrospectiveRatingEngineTest {

    @Test
    @DisplayName("Should correctly calculate normal uncapped retrospective premium")
    void testNormalRetroRatingCalculation() {
        BigDecimal stdPrem = new BigDecimal("100000.00");
        BigDecimal losses = new BigDecimal("35000.00");
        BigDecimal basicFac = new BigDecimal("0.220");
        BigDecimal lcf = new BigDecimal("1.150");
        BigDecimal taxMult = new BigDecimal("1.050");
        BigDecimal minFac = new BigDecimal("0.600");
        BigDecimal maxFac = new BigDecimal("1.400");

        var result = WCRetrospectiveRatingEngine.getInstance().calculateRetroRating(
                null, stdPrem, losses, basicFac, lcf, taxMult, minFac, maxFac
        );

        assertNotNull(result);
        assertEquals(new BigDecimal("22000.00"), result.basicPremium); // 100k * 0.22
        assertEquals(new BigDecimal("40250.00"), result.convertedLosses); // 35k * 1.15
        // Uncapped = (22000 + 40250) * 1.05 = 62250 * 1.05 = 65362.50
        assertEquals(new BigDecimal("65362.50"), result.uncappedRetroPremium);
        assertEquals("NONE", result.cappingApplied);
        assertEquals(new BigDecimal("65362.50"), result.finalRetrospectivePremium);
        // Adjustment = 65362.50 - 100000.00 = -34637.50 (Refund due to employer)
        assertEquals("RETURN_PREMIUM", result.adjustmentStatus);
        assertEquals(new BigDecimal("-34637.50"), result.retrospectiveAdjustmentAmount);
    }

    @Test
    @DisplayName("Should enforce Maximum Premium Cap when loss severity is catastrophic")
    void testMaximumPremiumCapEnforcement() {
        BigDecimal stdPrem = new BigDecimal("100000.00");
        BigDecimal severeLosses = new BigDecimal("150000.00");
        BigDecimal basicFac = new BigDecimal("0.220");
        BigDecimal lcf = new BigDecimal("1.150");
        BigDecimal taxMult = new BigDecimal("1.050");
        BigDecimal minFac = new BigDecimal("0.600");
        BigDecimal maxFac = new BigDecimal("1.400"); // Max = $140,000

        var result = WCRetrospectiveRatingEngine.getInstance().calculateRetroRating(
                null, stdPrem, severeLosses, basicFac, lcf, taxMult, minFac, maxFac
        );

        assertEquals("MAXIMUM_CAP", result.cappingApplied);
        assertEquals(new BigDecimal("140000.00"), result.finalRetrospectivePremium);
        assertEquals("ADDITIONAL_PREMIUM_DUE", result.adjustmentStatus);
        assertEquals(new BigDecimal("40000.00"), result.retrospectiveAdjustmentAmount);
    }

    @Test
    @DisplayName("Should enforce Minimum Premium Cap when zero or minimal losses occur")
    void testMinimumPremiumCapEnforcement() {
        BigDecimal stdPrem = new BigDecimal("100000.00");
        BigDecimal zeroLosses = BigDecimal.ZERO;
        BigDecimal basicFac = new BigDecimal("0.220");
        BigDecimal lcf = new BigDecimal("1.150");
        BigDecimal taxMult = new BigDecimal("1.050");
        BigDecimal minFac = new BigDecimal("0.600"); // Min = $60,000
        BigDecimal maxFac = new BigDecimal("1.400");

        var result = WCRetrospectiveRatingEngine.getInstance().calculateRetroRating(
                null, stdPrem, zeroLosses, basicFac, lcf, taxMult, minFac, maxFac
        );

        // Uncapped = 22000 * 1.05 = 23100 < 60000 Min Cap
        assertEquals("MINIMUM_CAP", result.cappingApplied);
        assertEquals(new BigDecimal("60000.00"), result.finalRetrospectivePremium);
    }
}
