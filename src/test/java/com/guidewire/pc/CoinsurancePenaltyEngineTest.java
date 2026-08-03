package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.CoinsurancePenaltyEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Property Coinsurance Clause Penalty Engine Tests")
public class CoinsurancePenaltyEngineTest {

    @Test
    @DisplayName("Should apply coinsurance penalty payout reduction for under-insured building")
    public void testCoinsurancePenaltyApplied() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-COINS-8001");

        // Building Replacement Value: $2,000,000. 80% Coinsurance -> Required Limit: $1,600,000.
        // Actual Building Limit: $1,200,000 (Under-insured, penalty ratio = 1.2M / 1.6M = 0.75).
        // Claim Loss: $500,000. Gross Payout = $500k * 0.75 = $375,000. Penalty = $125,000.
        CoinsurancePenaltyEngine.CoinsuranceResult res = CoinsurancePenaltyEngine.getInstance()
                .calculateClaimPayoutWithCoinsurance(period, new BigDecimal("2000000.00"), new BigDecimal("1200000.00"), 0.80, new BigDecimal("500000.00"), new BigDecimal("5000.00"));

        assertNotNull(res);
        assertFalse(res.isCoinsuranceRequirementMet());
        assertEquals(new BigDecimal("1600000.00"), res.getRequiredCoinsuranceLimit());
        assertEquals(new BigDecimal("375000.00"), res.getGrossPayoutBeforeDeductible());
        assertEquals(new BigDecimal("125000.00"), res.getCoinsurancePenaltyAmount());
        assertEquals(new BigDecimal("370000.00"), res.getNetClaimPayout());
    }
}
