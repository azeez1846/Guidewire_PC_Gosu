package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.DeductibleBuybackEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Policy Deductible Buyback & Surcharge Engine Tests")
public class DeductibleBuybackEngineTest {

    @Test
    @DisplayName("Should calculate buyback surcharge when buying down deductible from $10,000 to $1,000")
    public void testDeductibleBuybackSurcharge() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-BUYBACK-1001");
        period.setTotalPremium(new BigDecimal("5000.00"));

        DeductibleBuybackEngine.BuybackResult res = DeductibleBuybackEngine.getInstance()
                .calculateDeductibleBuyback(period, new BigDecimal("10000.00"), new BigDecimal("1000.00"));

        assertNotNull(res);
        assertTrue(res.getBuybackSurchargePct() > 0);
        assertEquals(new BigDecimal("900.00"), res.getSurchargeAmount());
        assertEquals(new BigDecimal("5900.00"), res.getRevisedTotalPremium());
    }
}
