package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.SlidingScaleDividendEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Loss Sensitive Sliding Scale Policyholder Dividend Engine Tests")
public class SlidingScaleDividendEngineTest {

    @Test
    @DisplayName("Should return 15% policyholder dividend for low loss ratio (< 30%)")
    public void testSlidingScaleDividendReturn() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-DIV-5001");
        period.setTotalPremium(new BigDecimal("100000.00")); // $100k premium

        SlidingScaleDividendEngine.DividendResult res = SlidingScaleDividendEngine.getInstance()
                .calculatePolicyholderDividend(period, new BigDecimal("20000.00")); // $20k losses = 20% Loss Ratio

        assertNotNull(res);
        assertEquals(0.20, res.getLossRatio());
        assertEquals(0.15, res.getDividendPercentage());
        assertEquals(new BigDecimal("15000.00"), res.getDividendAmount());
        assertEquals(new BigDecimal("85000.00"), res.getNetPolicyCost());
    }
}
