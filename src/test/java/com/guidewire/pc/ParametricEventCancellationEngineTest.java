package com.guidewire.pc;

import com.guidewire.pc.service.ParametricEventCancellationEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Parametric Weather & Event Cancellation Engine Tests")
public class ParametricEventCancellationEngineTest {

    @Test
    @DisplayName("Should quote parametric event cancellation and trigger instant automatic indemnity")
    void testParametricEndorsementAndAutomaticClaimSettlement() {
        BigDecimal eventLimit = new BigDecimal("500000.00");
        var quote = ParametricEventCancellationEngine.getInstance().quoteParametricEndorsement(
                null, "Austin Music Festival", "2026-10-15", "Zilker Park", eventLimit, "RAINFALL_ACCUMULATION", 1.25
        );

        assertNotNull(quote);
        assertEquals(new BigDecimal("22500.00"), quote.calculatedParametricPremium); // 500k * 4.5%
        assertEquals("INCHES", quote.thresholdUnit);

        // Case 1: Telemetry breaches threshold (1.75 inches >= 1.25)
        var breachedSettlement = ParametricEventCancellationEngine.getInstance().evaluateTelemetryTrigger(quote, 1.75);
        assertTrue(breachedSettlement.isTriggerBreached);
        assertEquals(eventLimit, breachedSettlement.automaticClaimSettlementAmount);
        assertEquals("INSTANT_INDEMNITY_DISPATCHED", breachedSettlement.claimsStatus);

        // Case 2: Telemetry below threshold (0.50 inches < 1.25)
        var drySettlement = ParametricEventCancellationEngine.getInstance().evaluateTelemetryTrigger(quote, 0.50);
        assertFalse(drySettlement.isTriggerBreached);
        assertEquals(BigDecimal.ZERO, drySettlement.automaticClaimSettlementAmount);
        assertEquals("NO_CLAIM_TRIGGERED", drySettlement.claimsStatus);
    }
}
