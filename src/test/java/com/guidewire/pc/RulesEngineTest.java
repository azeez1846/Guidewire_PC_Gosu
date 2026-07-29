package com.guidewire.pc;

import com.guidewire.pc.model.Account;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.rules.RuleContext;
import com.guidewire.pc.rules.RulesEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RulesEngineTest {

    private RulesEngine rulesEngine;

    @BeforeEach
    public void setUp() {
        rulesEngine = RulesEngine.getInstance();
    }

    @Test
    public void testPreQuoteRulesUnderwritingHold() {
        PolicyPeriod period = new PolicyPeriod();
        period.setBodilyInjuryLimit("$1M/$1M");
        period.setTermMonths(12);

        RuleContext context = rulesEngine.evaluatePreQuoteRules(period);
        assertTrue(context.isUnderwritingHoldRequired());
        assertFalse(context.getWarningMessages().isEmpty());
        assertFalse(context.hasErrors());
    }

    @Test
    public void testPreBindRulesProducerCodeMissing() {
        Account acc = new Account();
        acc.setAccountStatus("Active");

        PolicyPeriod period = new PolicyPeriod();
        period.setAccount(acc);
        period.setProducerCode(""); // Missing producer code

        RuleContext context = rulesEngine.evaluatePreBindRules(period);
        assertTrue(context.hasErrors());
        assertTrue(context.getErrorMessages().stream().anyMatch(msg -> msg.contains("Producer Code is mandatory")));
    }
}
