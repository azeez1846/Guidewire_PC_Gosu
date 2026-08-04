package com.guidewire.pc;

import com.guidewire.pc.service.ClaimCenterIntegrationEngine;
import com.guidewire.pc.service.PolicyLifecycleRenewalEngine;
import com.guidewire.pc.service.PolicyLifecycleRenewalEngine.MTACalculationResult;
import com.guidewire.pc.service.PolicyLifecycleRenewalEngine.RenewalResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class PolicyLifecycleRenewalEngineTest {

    @Test
    @DisplayName("Test Automated Renewal Eligibility & Premium Inflation Calculation")
    public void testRenewalEvaluation() {
        PolicyLifecycleRenewalEngine renewalEngine = new PolicyLifecycleRenewalEngine();
        ClaimCenterIntegrationEngine claimsEngine = new ClaimCenterIntegrationEngine();
        String policyNumber = "POL-REN-2026";
        BigDecimal currentPremium = new BigDecimal("2000.00");

        // 1. Clean policy -> Auto renewal eligible with 5% rate increase
        ClaimCenterIntegrationEngine.PolicyLossSummary cleanSummary = claimsEngine.evaluatePolicyLossSummary(policyNumber, currentPremium);
        RenewalResult resultClean = renewalEngine.evaluateAndCreateRenewal(policyNumber, currentPremium, cleanSummary, new BigDecimal("5.00"));

        assertTrue(resultClean.isEligible());
        assertEquals(new BigDecimal("2100.00"), resultClean.getNewRenewalPremium());
        assertNotNull(resultClean.getRenewalJobNumber());

        // 2. High loss policy -> Underwriting hold blocks auto renewal
        claimsEngine.ingestFNOL(policyNumber, "FIRE", new BigDecimal("1800.00"), "Kitchen fire");
        ClaimCenterIntegrationEngine.PolicyLossSummary highLossSummary = claimsEngine.evaluatePolicyLossSummary(policyNumber, currentPremium);
        RenewalResult resultBlocked = renewalEngine.evaluateAndCreateRenewal(policyNumber, currentPremium, highLossSummary, new BigDecimal("5.00"));

        assertFalse(resultBlocked.isEligible());
        assertTrue(resultBlocked.getStatusMessage().contains("Ineligible for automated renewal due to underwriting hold"));
    }

    @Test
    @DisplayName("Test Mid-Term Endorsement Pro-Rata Recalculation")
    public void testMidTermEndorsementProration() {
        PolicyLifecycleRenewalEngine engine = new PolicyLifecycleRenewalEngine();
        String policyNumber = "POL-MTA-500";
        BigDecimal currentAnnualPrem = new BigDecimal("1200.00");
        BigDecimal updatedAnnualPrem = new BigDecimal("1800.00"); // +$600 annual increase

        LocalDate effDate = LocalDate.of(2026, 1, 1);
        LocalDate expDate = LocalDate.of(2027, 1, 1); // 365 days
        LocalDate mtaDate = LocalDate.of(2026, 7, 2);  // Exactly half year (~183 days remaining)

        MTACalculationResult mta = engine.calculateMidTermEndorsement(policyNumber, currentAnnualPrem, updatedAnnualPrem, effDate, expDate, mtaDate);

        assertEquals(365, mta.getTotalPolicyDays());
        assertEquals(183, mta.getRemainingDays());
        // Expected prorated premium = 600 * (183 / 365) = $300.82
        assertEquals(new BigDecimal("300.82"), mta.getProratedAdditionalPremium());
    }
}
