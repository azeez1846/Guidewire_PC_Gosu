package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.PolicyDiffService;
import com.guidewire.pc.service.UnderwritingDashboardService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyticsAndDiffTest {

    @Test
    public void testPolicyDiffService() {
        PolicyDiffService diffService = PolicyDiffService.getInstance();

        PolicyPeriod base = new PolicyPeriod();
        base.setPolicyNumber("POL-DIFF-100");
        base.setJobNumber("S000100");
        base.setBodilyInjuryLimit("$500k/$500k");
        base.setTotalPremium(new BigDecimal("1500.00"));

        PolicyPeriod compare = new PolicyPeriod();
        compare.setPolicyNumber("POL-DIFF-100");
        compare.setJobNumber("C000101");
        compare.setBodilyInjuryLimit("$1M/$1M");
        compare.setTotalPremium(new BigDecimal("2100.00"));

        PolicyDiffService.PolicyDiffReport report = diffService.compareRevisions(base, compare);
        assertNotNull(report);
        assertEquals("POL-DIFF-100", report.policyNumber());
        assertEquals("S000100", report.baseJobNumber());
        assertEquals("C000101", report.compareJobNumber());

        assertTrue(report.changes().stream().anyMatch(c -> c.fieldName().equals("Bodily Injury Limit") && c.oldValue().equals("$500k/$500k") && c.newValue().equals("$1M/$1M")));
        assertTrue(report.changes().stream().anyMatch(c -> c.fieldName().equals("Total Premium") && c.isFinancial()));
    }

    @Test
    public void testUnderwritingDashboardKpis() {
        UnderwritingDashboardService dashboardService = UnderwritingDashboardService.getInstance();
        UnderwritingDashboardService.UnderwritingKpis kpis = dashboardService.computeKpis();

        assertNotNull(kpis);
        assertTrue(kpis.totalAccounts() >= 0);
        assertTrue(kpis.totalSubmissions() >= 0);
        assertNotNull(kpis.directWrittenPremium());
        assertTrue(kpis.overallLossRatioPercentage() > 0.0);
        assertNotNull(kpis.premiumByLine());
    }
}
