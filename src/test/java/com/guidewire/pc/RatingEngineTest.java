package com.guidewire.pc;

import com.guidewire.pc.model.Cost;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.Transaction;
import com.guidewire.pc.service.RatingEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RatingEngineTest {

    private RatingEngine ratingEngine;

    @BeforeEach
    public void setUp() {
        ratingEngine = RatingEngine.getInstance();
    }

    @Test
    public void testCommercialAutoRating() {
        PolicyPeriod period = new PolicyPeriod();
        period.setProductCode("CommercialAuto");
        period.setTermMonths(12);
        period.setBodilyInjuryLimit("$500k/$500k");
        period.setPropertyDamageLimit("$250k");

        List<Cost> costs = ratingEngine.rate(period);
        assertNotNull(costs);
        assertFalse(costs.isEmpty());

        // Verify Cost Breakdown
        assertTrue(costs.stream().anyMatch(c -> "BasePremium".equals(c.getChargePattern())));
        assertTrue(costs.stream().anyMatch(c -> "BodilyInjuryCoverage".equals(c.getChargePattern())));
        assertTrue(costs.stream().anyMatch(c -> "StateTax".equals(c.getChargePattern())));

        assertTrue(period.getTotalPremium().compareTo(BigDecimal.ZERO) > 0);

        List<Transaction> transactions = ratingEngine.createTransactions(period, costs);
        assertEquals(costs.size(), transactions.size());
    }
}
