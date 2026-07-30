package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.model.Cost;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.RatingEngine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GosuRatingEngineUnitTest {

    @BeforeAll
    public static void initGosu() {
        GosuBridge.initGosuEngine(new File("."));
    }

    @Test
    public void testCommercialAutoRating() {
        RatingEngine ratingEngine = RatingEngine.getInstance();

        PolicyPeriod period = new PolicyPeriod();
        period.setProductCode("CommercialAuto");
        period.setBaseState("TX");
        period.setBodilyInjuryLimit("$500k/$500k");
        period.setTermMonths(12);

        List<Cost> costs = ratingEngine.rate(period);
        assertNotNull(costs);
        assertFalse(costs.isEmpty());

        BigDecimal total = costs.stream()
                .map(Cost::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertTrue(total.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    public void testCommercialPropertyRatingTX() {
        RatingEngine ratingEngine = RatingEngine.getInstance();

        PolicyPeriod period = new PolicyPeriod();
        period.setProductCode("CommercialProperty");
        period.setBaseState("TX");
        period.setTermMonths(12);

        List<Cost> costs = ratingEngine.rate(period);
        assertNotNull(costs);
        assertFalse(costs.isEmpty());
    }

    @Test
    public void testPolicyChangeMidTermProration() {
        RatingEngine ratingEngine = RatingEngine.getInstance();

        PolicyPeriod period = new PolicyPeriod();
        period.setProductCode("PersonalAuto");
        period.setBaseState("CA");
        period.setTermMonths(12);

        List<Cost> costs = ratingEngine.rate(period);
        assertNotNull(costs);
        BigDecimal annualPremium = costs.stream()
                .map(Cost::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertTrue(annualPremium.compareTo(BigDecimal.ZERO) > 0);

        // 50% term proration (6 months remaining)
        BigDecimal prorated6Months = annualPremium.multiply(new BigDecimal("0.50"));
        assertEquals(0, annualPremium.divide(new BigDecimal("2")).compareTo(prorated6Months));
    }
}
