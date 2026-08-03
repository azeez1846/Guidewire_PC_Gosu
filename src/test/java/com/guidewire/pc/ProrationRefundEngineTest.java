package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.ProrationRefundEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Short-Rate vs Pro-Rata Premium Refund Engine Tests")
public class ProrationRefundEngineTest {

    @Test
    @DisplayName("Should calculate 100% pro-rata refund for carrier-initiated cancellation")
    public void testProRataCarrierCancellation() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-PRO-1001");
        period.setTotalPremium(new BigDecimal("1200.00"));

        ProrationRefundEngine.RefundCalculationResult res = ProrationRefundEngine.getInstance()
                .calculateCancellationRefund(period, 182, 365, false);

        assertNotNull(res);
        assertEquals("Pro-Rata (Carrier-Initiated)", res.getCancellationType());
        assertTrue(res.getRefundAmount().compareTo(new BigDecimal("590.00")) > 0);
        assertEquals(BigDecimal.ZERO, res.getShortRatePenalty());
    }

    @Test
    @DisplayName("Should apply 90% short-rate refund penalty for insured-initiated cancellation")
    public void testShortRateInsuredCancellation() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-SHORT-2002");
        period.setTotalPremium(new BigDecimal("1000.00"));

        ProrationRefundEngine.RefundCalculationResult res = ProrationRefundEngine.getInstance()
                .calculateCancellationRefund(period, 180, 365, true);

        assertNotNull(res);
        assertEquals("Short-Rate (Insured-Initiated)", res.getCancellationType());
        assertTrue(res.getShortRatePenalty().compareTo(BigDecimal.ZERO) > 0);
    }
}
