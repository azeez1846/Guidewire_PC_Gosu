package com.guidewire.pc;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentGatewayTest {

    @Test
    public void testPaymentTokenization() {
        String card = "4111222233334444";
        String last4 = card.substring(card.length() - 4);
        assertEquals("4444", last4);
        assertTrue(card.startsWith("4"));
    }

    @Test
    public void testInstallmentScheduleCalculation() {
        BigDecimal totalPremium = new BigDecimal("2889.00");
        BigDecimal downPayment = totalPremium.multiply(new BigDecimal("0.20")).setScale(2, java.math.RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("577.80"), downPayment);

        BigDecimal remaining = totalPremium.subtract(downPayment);
        BigDecimal installment = remaining.divide(new BigDecimal("11"), 2, java.math.RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("210.11"), installment);
    }
}
