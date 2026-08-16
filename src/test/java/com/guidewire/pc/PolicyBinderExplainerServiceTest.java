package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.PolicyBinderExplainerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AI Policy Binder Document Explainer & Summary Tests")
public class PolicyBinderExplainerServiceTest {

    @Test
    @DisplayName("Should synthesize complete broker executive summary from policy period")
    void testExecutiveSummaryGeneration() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-COMM-2026-8801");
        period.setProductCode("CommercialProperty");
        period.setTotalPremium(new BigDecimal("22000.00"));

        var summary = PolicyBinderExplainerService.getInstance().generateExecutiveSummary(period);

        assertNotNull(summary);
        assertEquals("POL-COMM-2026-8801", summary.policyNumber);
        assertEquals(new BigDecimal("22000.00"), summary.totalAnnualPremium);
        assertEquals(new BigDecimal("4400.00"), summary.downPaymentRequired); // 20% down
        assertNotNull(summary.monthlyInstallment);
        assertFalse(summary.primaryCoveragesIncluded.isEmpty());
        assertFalse(summary.keyEndorsementsAttached.isEmpty());
        assertFalse(summary.criticalWarrantiesAndExclusions.isEmpty());
        assertNotNull(summary.executiveUnderwritingBriefing);
        assertNotNull(summary.brokerActionItem);
    }
}
