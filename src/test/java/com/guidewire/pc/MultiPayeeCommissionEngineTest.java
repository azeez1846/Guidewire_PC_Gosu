package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.MultiPayeeCommissionEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Multi-Payee Billing Split & Tiered Commission Engine Tests")
public class MultiPayeeCommissionEngineTest {

    @Test
    @DisplayName("Should calculate 60/40 billing split and Tier 3 15% producer commission for high volume agency")
    public void testMultiPayeeTier3Commission() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-PAYEE-5001");
        period.setTotalPremium(new BigDecimal("10000.00"));

        MultiPayeeCommissionEngine.BillingCommissionResult res = MultiPayeeCommissionEngine.getInstance()
                .calculateMultiPayeeCommission(period, "First Named Insured", 0.60, "Loss Payee Bank", new BigDecimal("750000.00"));

        assertNotNull(res);
        assertEquals(2, res.getPayeeSplits().size());
        assertEquals(new BigDecimal("6000.00"), res.getPayeeSplits().get(0).getSplitAmount());
        assertEquals(new BigDecimal("4000.00"), res.getPayeeSplits().get(1).getSplitAmount());

        assertEquals(0.15, res.getCommissionRate());
        assertEquals(new BigDecimal("1500.00"), res.getCommissionAmount());
    }
}
