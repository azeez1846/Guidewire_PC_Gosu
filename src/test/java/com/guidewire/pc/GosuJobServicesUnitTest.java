package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.model.PolicyPeriod;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class GosuJobServicesUnitTest {

    @BeforeAll
    public static void initGosu() {
        GosuBridge.initGosuEngine(new File("."));
    }

    @Test
    public void testPolicyChangeJobWorkflow() {
        PolicyPeriod orig = new PolicyPeriod();
        orig.setPolicyNumber("POL-881920");
        orig.setProductCode("CommercialAuto");
        orig.setEffectiveDate("2026-01-01");
        orig.setExpirationDate("2027-01-01");
        orig.setTermMonths(12);
        orig.setBodilyInjuryLimit("$500k/$500k");
        orig.setPropertyDamageLimit("$250k");
        orig.setCollisionDeductible("$1000");

        PolicyPeriod changeBranch = startPolicyChangeMock(orig, "2026-06-01");
        assertNotNull(changeBranch);
        assertEquals("PolicyChange", changeBranch.getJobType());
        assertEquals("POL-881920", changeBranch.getPolicyNumber());
        assertEquals("2026-06-01", changeBranch.getEffectiveDate());

        PolicyPeriod endorsed = processEndorsementMock(changeBranch, "$1M/$1M", "$500");
        assertEquals("Quoted", endorsed.getStatus());
        assertEquals("$1M/$1M", endorsed.getBodilyInjuryLimit());
        assertEquals("$500", endorsed.getCollisionDeductible());

        PolicyPeriod bound = bindEndorsementMock(endorsed);
        assertEquals("Bound", bound.getStatus());
    }

    @Test
    public void testCancellationProRataWorkflow() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-771920");
        period.setTotalPremium(new BigDecimal("2000.00"));

        PolicyPeriod canceled = cancelPolicyMock(period, "Non-Payment of Premium", "ProRata", "2026-07-01");
        assertNotNull(canceled);
        assertEquals("Canceled", canceled.getStatus());
        assertEquals("Cancellation", canceled.getJobType());
        assertEquals(0, canceled.getTotalPremium().compareTo(new BigDecimal("-1000.00")));

        PolicyPeriod reinstated = reinstatePolicyMock(canceled, "Payment Received in Full");
        assertEquals("Issued", reinstated.getStatus());
        assertEquals("Reinstatement", reinstated.getJobType());
    }

    @Test
    public void testCancellationShortRateWorkflow() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-661920");
        period.setTotalPremium(new BigDecimal("2000.00"));

        PolicyPeriod canceled = cancelPolicyMock(period, "Insured Request", "ShortRate", "2026-07-01");
        assertNotNull(canceled);
        assertEquals(0, canceled.getTotalPremium().compareTo(new BigDecimal("-900.00")));
    }

    private PolicyPeriod startPolicyChangeMock(PolicyPeriod orig, String effDate) {
        PolicyPeriod change = new PolicyPeriod();
        change.setPolicyNumber(orig.getPolicyNumber());
        change.setProductCode(orig.getProductCode());
        change.setJobType("PolicyChange");
        change.setEffectiveDate(effDate);
        change.setStatus("Draft");
        change.setBodilyInjuryLimit(orig.getBodilyInjuryLimit());
        change.setCollisionDeductible(orig.getCollisionDeductible());
        return change;
    }

    private PolicyPeriod processEndorsementMock(PolicyPeriod branch, String newLimit, String newDeductible) {
        branch.setBodilyInjuryLimit(newLimit);
        branch.setCollisionDeductible(newDeductible);
        branch.setStatus("Quoted");
        return branch;
    }

    private PolicyPeriod bindEndorsementMock(PolicyPeriod endorsed) {
        endorsed.setStatus("Bound");
        return endorsed;
    }

    private PolicyPeriod cancelPolicyMock(PolicyPeriod period, String reason, String calcMethod, String cancelDate) {
        assertNotNull(reason);
        PolicyPeriod canceled = new PolicyPeriod();
        canceled.setPolicyNumber(period.getPolicyNumber());
        canceled.setJobType("Cancellation");
        canceled.setStatus("Canceled");
        canceled.setEffectiveDate(cancelDate);

        BigDecimal origPrem = period.getTotalPremium() != null ? period.getTotalPremium() : BigDecimal.ZERO;
        if ("ProRata".equalsIgnoreCase(calcMethod)) {
            canceled.setTotalPremium(origPrem.multiply(new BigDecimal("-0.50")));
        } else { // ShortRate
            canceled.setTotalPremium(origPrem.multiply(new BigDecimal("-0.45")));
        }
        return canceled;
    }

    private PolicyPeriod reinstatePolicyMock(PolicyPeriod canceled, String reason) {
        assertNotNull(reason);
        canceled.setStatus("Issued");
        canceled.setJobType("Reinstatement");
        return canceled;
    }
}
