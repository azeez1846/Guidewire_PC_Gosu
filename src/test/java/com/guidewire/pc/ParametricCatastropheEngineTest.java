package com.guidewire.pc;

import com.guidewire.pc.service.ParametricCatastropheEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Parametric Climate & Catastrophe Engine Unit Tests")
public class ParametricCatastropheEngineTest {

    @Test
    @DisplayName("Should Trigger Parametric Hurricane Wind Payout when Windspeed >= 120 Knots")
    void testParametricHurricaneWindTrigger() {
        ParametricCatastropheEngine engine = ParametricCatastropheEngine.getInstance();

        ParametricCatastropheEngine.ParametricEvaluationResult result = engine.evaluateWindspeedTrigger(
                null, "33101", 135.0, new BigDecimal("1000000.00")
        );

        assertNotNull(result);
        assertTrue(result.isTriggered());
        assertEquals(0.75, result.getPayoutFactor());
        assertEquals(new BigDecimal("750000.00"), result.getCalculatedPayoutAmount());
        assertEquals("TRIGGERED_CLAIM_PAYOUT_INITIATED", result.getStatus());
        assertTrue(result.getPayoutReference().startsWith("PAYOUT-PARAM-"));
    }

    @Test
    @DisplayName("Should Not Trigger Payout when Windspeed Below 120 Knots Threshold")
    void testParametricWindspeedBelowThreshold() {
        ParametricCatastropheEngine engine = ParametricCatastropheEngine.getInstance();

        ParametricCatastropheEngine.ParametricEvaluationResult result = engine.evaluateWindspeedTrigger(
                null, "33101", 95.0, new BigDecimal("1000000.00")
        );

        assertNotNull(result);
        assertFalse(result.isTriggered());
        assertEquals(0.0, result.getPayoutFactor());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getCalculatedPayoutAmount()));
        assertEquals("THRESHOLD_NOT_MET", result.getStatus());
    }

    @Test
    @DisplayName("Should Trigger Full Parametric Earthquake Payout when Richter Magnitude >= 7.5")
    void testParametricEarthquakeFullTrigger() {
        ParametricCatastropheEngine engine = ParametricCatastropheEngine.getInstance();

        ParametricCatastropheEngine.ParametricEvaluationResult result = engine.evaluateEarthquakeTrigger(
                null, "94102", 7.8, new BigDecimal("2000000.00")
        );

        assertNotNull(result);
        assertTrue(result.isTriggered());
        assertEquals(1.00, result.getPayoutFactor());
        assertEquals(new BigDecimal("2000000.00"), result.getCalculatedPayoutAmount());
        assertEquals("TRIGGERED_CLAIM_PAYOUT_INITIATED", result.getStatus());
    }
}
