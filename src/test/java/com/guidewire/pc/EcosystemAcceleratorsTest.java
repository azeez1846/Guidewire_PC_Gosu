package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.rules.RuleContext;
import com.guidewire.pc.rules.RulesEngine;
import com.guidewire.pc.service.BillingCenterService;
import com.guidewire.pc.service.ClaimCenterService;
import com.guidewire.pc.service.DecPageImporterService;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EcosystemAcceleratorsTest {

    @Test
    public void testClaimCenterFnolAndLossEscalation() {
        ClaimCenterService claimService = ClaimCenterService.getInstance();
        String policyNum = "POL-CC-TEST-001";

        List<ClaimCenterService.Claim> claimsBefore = claimService.getClaimsForPolicy(policyNum);
        assertTrue(claimsBefore.isEmpty());

        claimService.reportFnol(policyNum, "Accident on Highway 101", new BigDecimal("15000.00"));
        BigDecimal openLoss = claimService.getTotalOpenLossForPolicy(policyNum);
        assertEquals(new BigDecimal("15000.00"), openLoss);

        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber(policyNum);

        RuleContext ruleCtx = RulesEngine.getInstance().evaluatePreQuoteRules(period);
        assertTrue(ruleCtx.isUnderwritingHoldRequired(), "Quote with >$10k open claims should require UW hold");
        assertTrue(ruleCtx.getWarningMessages().stream().anyMatch(msg -> msg.toLowerCase().contains("underwriting") || msg.toLowerCase().contains("loss")));
    }

    @Test
    public void testBillingCenterScheduleAndInvoice() {
        BillingCenterService billingService = BillingCenterService.getInstance();
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-BILL-TEST-001");
        period.setTotalPremium(new BigDecimal("2400.00"));

        BillingCenterService.BillingSchedule schedule = billingService.generateSchedule(period, "FourPay");
        assertNotNull(schedule);
        assertEquals(4, schedule.getInstallments().size());

        String invoiceHtml = billingService.generateInvoiceHtml(period, schedule);
        assertNotNull(invoiceHtml);
        assertTrue(invoiceHtml.contains("POL-BILL-TEST-001"));
        assertTrue(invoiceHtml.contains("FourPay"));
    }

    @Test
    public void testDecPageImporter() {
        DecPageImporterService importer = DecPageImporterService.getInstance();
        String rawDecText = """
                POLICY DECLARATIONS PAGE
                Policy Type: Personal Auto Policy
                Named Insured: Metro Transit Logistics LLC
                Bodily Injury: $1M/$1M
                Property Damage: $500k
                Collision Deductible: $500
                Effective Date: 2026-08-01
                """;

        PolicyPeriod imported = importer.importDecPageText(rawDecText);
        assertNotNull(imported);
        assertEquals("PersonalAuto", imported.getProductCode());
        assertEquals("Metro Transit Logistics LLC", imported.getAccount().getAccountHolderName());
        assertEquals("$1M/$1M", imported.getBodilyInjuryLimit());
        assertEquals("$500k", imported.getPropertyDamageLimit());
        assertEquals("$500", imported.getCollisionDeductible());
    }
}
