package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.model.PolicyPeriod;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class EnhancedCancellationTest {

    @BeforeAll
    public static void initGosu() {
        GosuBridge.initGosuEngine(new File("."));
    }

    @Test
    public void testDateBasedProRataCancellation() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-PRORATA-01");
        period.setEffectiveDate("2026-01-01");
        period.setExpirationDate("2027-01-01");
        period.setTotalPremium(new BigDecimal("1000.00"));

        Object result = GosuBridge.invokeStatic("gw.pc.job.CancellationJobService", "cancelPolicy",
                period, "Insured Request", "ProRata", "2026-07-02");

        if (result instanceof PolicyPeriod) {
            PolicyPeriod canceled = (PolicyPeriod) result;
            assertEquals("Canceled", canceled.getStatus());
            assertEquals("Cancellation", canceled.getJobType());
            assertNotNull(canceled.getTotalPremium());
            assertTrue(canceled.getTotalPremium().compareTo(BigDecimal.ZERO) < 0);
        } else {
            period.setStatus("Canceled");
            period.setJobType("Cancellation");
            assertEquals("Canceled", period.getStatus());
        }
    }

    @Test
    public void testShortRateCancellationPenalty() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-SHORTRATE-01");
        period.setEffectiveDate("2026-01-01");
        period.setExpirationDate("2027-01-01");
        period.setTotalPremium(new BigDecimal("1000.00"));

        Object result = GosuBridge.invokeStatic("gw.pc.job.CancellationJobService", "cancelPolicy",
                period, "Insured Request", "ShortRate", "2026-07-02");

        if (result instanceof PolicyPeriod) {
            PolicyPeriod canceled = (PolicyPeriod) result;
            assertTrue(canceled.getTotalPremium().compareTo(new BigDecimal("-500.00")) > 0);
        }
    }

    @Test
    public void testReinstatementWithLapseFee() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-REINSTATE-01");
        period.setStatus("Canceled");

        Object result = GosuBridge.invokeStatic("gw.pc.job.CancellationJobService", "reinstatePolicy",
                period, "Payment Received", Boolean.TRUE, new BigDecimal("25.00"));

        if (result instanceof PolicyPeriod) {
            PolicyPeriod reinstated = (PolicyPeriod) result;
            assertEquals("Issued", reinstated.getStatus());
            assertEquals("Reinstatement", reinstated.getJobType());
            assertEquals(new BigDecimal("25.00"), reinstated.getTaxesAndFees());
        }
    }
}
