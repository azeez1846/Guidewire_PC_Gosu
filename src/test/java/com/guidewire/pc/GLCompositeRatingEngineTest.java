package com.guidewire.pc;

import com.guidewire.pc.service.GLCompositeRatingEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("General Liability Composite Rating Engine Tests")
public class GLCompositeRatingEngineTest {

    @Test
    @DisplayName("Should correctly rate multi-variable composite exposures")
    void testCompositeExposureRating() {
        BigDecimal sales = new BigDecimal("2000000.00"); // 2000 * 4.50 = 9000
        BigDecimal sqFt = new BigDecimal("40000.00");    // 40 * 85.00 = 3400
        BigDecimal payroll = new BigDecimal("500000.00"); // 5000 * 1.80 = 9000
        BigDecimal ocp = new BigDecimal("1000000.00");   // 1000000 * 0.0012 = 1200
        String liqTier = "TIER_1_RESTAURANT";
        BigDecimal liqSales = new BigDecimal("100000.00"); // 100 * 8.50 * 1.0 = 850
        BigDecimal pco = new BigDecimal("1000000.00");   // 1000 * 1.20 = 1200

        var res = GLCompositeRatingEngine.getInstance().rateCompositeGL(
                null, sales, sqFt, payroll, ocp, liqTier, liqSales, pco
        );

        assertNotNull(res);
        assertEquals(new BigDecimal("9000.00"), res.salesPremium);
        assertEquals(new BigDecimal("3400.00"), res.areaPremium);
        assertEquals(new BigDecimal("9000.00"), res.payrollPremium);
        assertEquals(new BigDecimal("1200.00"), res.ocpPremium);
        assertEquals(new BigDecimal("850.00"), res.liquorLiabilityPremium);
        assertEquals(new BigDecimal("1200.00"), res.productsCompletedOpsPremium);

        // Subtotal = 9000 + 3400 + 9000 + 1200 + 850 + 1200 = 24650
        assertEquals(new BigDecimal("24650.00"), res.subtotalPrem);
        // Taxes = 24650 * 0.06 = 1479.00
        assertEquals(new BigDecimal("1479.00"), res.stateTaxesAndFees);
        // Total = 26129.00
        assertEquals(new BigDecimal("26129.00"), res.totalCompositePremium);
    }

    @Test
    @DisplayName("Should apply higher multipliers for Nightclub liquor liability tiers")
    void testLiquorLiabilityTierMultiplier() {
        BigDecimal sales = new BigDecimal("1000000.00");
        BigDecimal sqFt = new BigDecimal("10000.00");
        BigDecimal payroll = new BigDecimal("100000.00");
        BigDecimal ocp = BigDecimal.ZERO;
        BigDecimal liqSales = new BigDecimal("500000.00");
        BigDecimal pco = BigDecimal.ZERO;

        var restRes = GLCompositeRatingEngine.getInstance().rateCompositeGL(null, sales, sqFt, payroll, ocp, "TIER_1_RESTAURANT", liqSales, pco);
        var tavernRes = GLCompositeRatingEngine.getInstance().rateCompositeGL(null, sales, sqFt, payroll, ocp, "TIER_2_TAVERN", liqSales, pco);
        var clubRes = GLCompositeRatingEngine.getInstance().rateCompositeGL(null, sales, sqFt, payroll, ocp, "TIER_3_NIGHTCLUB", liqSales, pco);

        assertTrue(tavernRes.liquorLiabilityPremium.compareTo(restRes.liquorLiabilityPremium) > 0);
        assertTrue(clubRes.liquorLiabilityPremium.compareTo(tavernRes.liquorLiabilityPremium) > 0);
    }
}
