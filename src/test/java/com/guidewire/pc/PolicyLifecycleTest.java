package com.guidewire.pc;

import com.guidewire.pc.model.Account;
import com.guidewire.pc.model.Cost;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.DataStoreService;
import com.guidewire.pc.service.PolicyLifecycleService;
import com.guidewire.pc.service.RatingEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PolicyLifecycleTest {
    private DataStoreService dataStore;
    private PolicyLifecycleService lifecycleService;

    @BeforeEach
    public void setUp() {
        dataStore = DataStoreService.getInstance();
        lifecycleService = PolicyLifecycleService.getInstance();
    }

    @Test
    public void testPolicyChangeEndorsement() {
        String polNum = "POL-TEST-ENDORSE-" + (System.currentTimeMillis() % 89999 + 10000);
        PolicyPeriod orig = new PolicyPeriod();
        orig.setPolicyNumber(polNum);
        orig.setProductCode("PersonalAuto");
        orig.setStatus("Issued");
        orig.setEffectiveDate("2026-01-01");
        orig.setExpirationDate("2027-01-01");
        dataStore.createSubmission(orig);

        PolicyPeriod changeJob = lifecycleService.startPolicyChange(polNum, "2026-07-01", "$1M/$1M", "$500");
        assertNotNull(changeJob);
        assertEquals("PolicyChange", changeJob.getJobType());
        assertEquals("Quoted", changeJob.getStatus());
        assertEquals("$1M/$1M", changeJob.getBodilyInjuryLimit());

        PolicyPeriod bound = lifecycleService.bindPolicyChange(changeJob.getJobNumber());
        assertEquals("Bound", bound.getStatus());
    }

    @Test
    public void testPolicyCancellationAndReinstatement() {
        String polNum = "POL-TEST-CANCEL-" + (System.currentTimeMillis() % 89999 + 10000);
        PolicyPeriod orig = new PolicyPeriod();
        orig.setPolicyNumber(polNum);
        orig.setProductCode("CommercialProperty");
        orig.setStatus("Issued");
        orig.setEffectiveDate("2026-01-01");
        orig.setExpirationDate("2027-01-01");
        orig.setTotalPremium(new BigDecimal("2000.00"));
        dataStore.createSubmission(orig);

        PolicyPeriod cancelled = lifecycleService.cancelPolicy(polNum, "Non-Payment", "ProRata", "2026-07-01");
        assertEquals("Canceled", cancelled.getStatus());
        assertTrue(cancelled.getTotalPremium().compareTo(BigDecimal.ZERO) < 0, "Refund premium should be negative credit");

        PolicyPeriod reinstated = lifecycleService.reinstatePolicy(polNum, "Payment Received");
        assertEquals("Issued", reinstated.getStatus());
    }

    @Test
    public void testPolicyRenewal() {
        String polNum = "POL-TEST-RENEW-" + (System.currentTimeMillis() % 89999 + 10000);
        PolicyPeriod orig = new PolicyPeriod();
        orig.setPolicyNumber(polNum);
        orig.setProductCode("GeneralLiability");
        orig.setStatus("Issued");
        orig.setEffectiveDate("2026-01-01");
        orig.setExpirationDate("2027-01-01");
        dataStore.createSubmission(orig);

        PolicyPeriod renewal = lifecycleService.renewPolicy(polNum);
        assertNotNull(renewal);
        assertEquals("Renewal", renewal.getJobType());
        assertEquals("2027-01-01", renewal.getEffectiveDate());
    }

    @Test
    public void testMultiPolicyBundlingDiscount() {
        Account multiAcc = new Account();
        multiAcc.setAccountNumber("A-MULTI-" + (System.currentTimeMillis() % 89999 + 10000));
        dataStore.createAccount(multiAcc);

        PolicyPeriod p1 = new PolicyPeriod();
        p1.setAccount(multiAcc);
        p1.setProductCode("PersonalAuto");
        dataStore.createSubmission(p1);

        PolicyPeriod p2 = new PolicyPeriod();
        p2.setAccount(multiAcc);
        p2.setProductCode("CommercialProperty");
        dataStore.createSubmission(p2);

        List<Cost> costs = RatingEngine.getInstance().rate(p2);
        boolean hasMultiPolicyDiscount = costs.stream().anyMatch(c -> "MultiPolicyDiscount".equalsIgnoreCase(c.getChargePattern()));
        assertTrue(hasMultiPolicyDiscount, "Multi-policy discount cost item should be applied");
    }

    @Test
    public void testCopySubmission() {
        String origJobNum = "S000" + (System.currentTimeMillis() % 89999 + 10000);
        PolicyPeriod orig = new PolicyPeriod();
        orig.setJobNumber(origJobNum);
        orig.setPolicyNumber("POL-COPY-SRC-1");
        orig.setProductCode("CommercialAuto");
        orig.setStatus("Quoted");
        orig.setBodilyInjuryLimit("$500k/$500k");
        orig.setPropertyDamageLimit("$250k");
        dataStore.createSubmission(orig);

        PolicyPeriod copy = lifecycleService.copySubmission(origJobNum);
        assertNotNull(copy);
        assertNotEquals(origJobNum, copy.getJobNumber(), "Copy must receive a fresh job number");
        assertNull(copy.getPolicyNumber(), "Copied draft submission should have no issued policy number");
        assertEquals("Draft", copy.getStatus(), "Copied submission status must be Draft");
        assertEquals("Submission", copy.getJobType(), "Copied submission job type must be Submission");
        assertEquals("CommercialAuto", copy.getProductCode(), "Product line must be cloned");
        assertEquals("$500k/$500k", copy.getBodilyInjuryLimit(), "Limits must be cloned");
        assertEquals("$250k", copy.getPropertyDamageLimit(), "Property damage limit must be cloned");
        assertNotEquals(orig.getPolicyPeriodFixedId(), copy.getPolicyPeriodFixedId(), "Copy must receive a fresh fixed ID");
    }
}
