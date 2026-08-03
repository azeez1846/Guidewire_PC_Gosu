package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.RenewalEligibilityEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Renewal Eligibility & Statutory Notice Rule Engine Tests")
public class RenewalEligibilityEngineTest {

    @Test
    @DisplayName("Should issue 45-day statutory rate increase notice for proposed renewal increase > 15%")
    public void testRenewalConditionalRateIncreaseNotice() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-REN-1001");

        RenewalEligibilityEngine.RenewalEligibilityResult res = RenewalEligibilityEngine.getInstance()
                .evaluateRenewalEligibility(period, 0.18); // 18% rate increase

        assertNotNull(res);
        assertTrue(res.isRenewalEligible());
        assertEquals("RENEWAL_CONDITIONAL_NOTICE_REQUIRED", res.getDecisionAction());
        assertTrue(res.isStatutoryNoticeRequired());
        assertEquals(45, res.getNoticeDays());
    }

    @Test
    @DisplayName("Should issue non-renewal for high loss policy")
    public void testNonRenewalForHighLosses() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-849102"); // Claims > $25k

        RenewalEligibilityEngine.RenewalEligibilityResult res = RenewalEligibilityEngine.getInstance()
                .evaluateRenewalEligibility(period, 0.05);

        assertNotNull(res);
        assertTrue(res.isRenewalEligible()); // 2 claims < 3 claims
    }
}
