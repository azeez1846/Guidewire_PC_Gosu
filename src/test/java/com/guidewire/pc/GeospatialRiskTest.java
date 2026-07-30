package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.rules.RuleContext;
import com.guidewire.pc.rules.RulesEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GeospatialRiskTest {

    @Test
    public void testWildfireHighRiskHoldRule() {
        PolicyPeriod period = new PolicyPeriod();
        period.setBaseState("CA");

        RuleContext ctx = RulesEngine.getInstance().evaluatePreQuoteRules(period);
        assertTrue(ctx.isUnderwritingHoldRequired());
        assertTrue(ctx.getWarningMessages().stream().anyMatch(msg -> msg.contains("Wildfire Zone")));
    }

    @Test
    public void testFloodHighRiskHoldRule() {
        PolicyPeriod period = new PolicyPeriod();
        period.setBaseState("FL");

        RuleContext ctx = RulesEngine.getInstance().evaluatePreQuoteRules(period);
        assertTrue(ctx.isUnderwritingHoldRequired());
        assertTrue(ctx.getWarningMessages().stream().anyMatch(msg -> msg.contains("Special Flood Hazard Area")));
    }

    @Test
    public void testLowRiskStateNoHold() {
        PolicyPeriod period = new PolicyPeriod();
        period.setBaseState("TX");

        RuleContext ctx = RulesEngine.getInstance().evaluatePreQuoteRules(period);
        assertFalse(ctx.isUnderwritingHoldRequired());
    }
}
