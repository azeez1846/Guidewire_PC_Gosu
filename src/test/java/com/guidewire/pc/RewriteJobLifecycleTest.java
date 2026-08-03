package com.guidewire.pc;

import com.guidewire.pc.constants.PCConstants;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.DataStoreService;
import com.guidewire.pc.service.PolicyLifecycleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Guidewire OOTB Rewrite & Rewrite New Account Job Engine Tests")
public class RewriteJobLifecycleTest {

    private DataStoreService dataStore;
    private PolicyLifecycleService lifecycleService;

    @BeforeEach
    public void setUp() {
        dataStore = DataStoreService.getInstance();
        lifecycleService = PolicyLifecycleService.getInstance();
    }

    @Test
    @DisplayName("Should execute Mid-Term Rewrite, cancelling original and creating rewritten branch")
    public void testMidTermRewrite() {
        PolicyPeriod orig = new PolicyPeriod();
        orig.setJobNumber("S000_REWRITE_BASE");
        orig.setPolicyNumber("POL-RW-ORIG-1001");
        orig.setStatus(PCConstants.STATUS_ISSUED);
        orig.setProductCode(PCConstants.PRODUCT_GENERAL_LIABILITY);
        dataStore.createSubmission(orig);

        PolicyPeriod rewritten = lifecycleService.startRewrite("POL-RW-ORIG-1001", "Coverage restructuring request", "2026-06-01");

        assertNotNull(rewritten);
        assertEquals(PCConstants.JOB_TYPE_REWRITE, rewritten.getJobType());
        assertEquals(PCConstants.STATUS_BOUND, rewritten.getStatus());
        assertEquals(PCConstants.STATUS_CANCELED, dataStore.findSubmission("S000_REWRITE_BASE").getStatus());
        assertNotEquals(orig.getPolicyNumber(), rewritten.getPolicyNumber());
    }

    @Test
    @DisplayName("Should execute Rewrite New Account, transferring policy to new target account")
    public void testRewriteNewAccount() {
        PolicyPeriod orig = new PolicyPeriod();
        orig.setJobNumber("S000_REWRITE_RNA_BASE");
        orig.setPolicyNumber("POL-RNA-ORIG-2002");
        orig.setStatus(PCConstants.STATUS_ISSUED);
        orig.setProductCode(PCConstants.PRODUCT_COMMERCIAL_PROPERTY);
        dataStore.createSubmission(orig);

        PolicyPeriod rewritten = lifecycleService.startRewriteNewAccount("POL-RNA-ORIG-2002", "A0009999", "Entity reorganization transfer");

        assertNotNull(rewritten);
        assertEquals(PCConstants.JOB_TYPE_REWRITE_NEW_ACCOUNT, rewritten.getJobType());
        assertEquals(PCConstants.STATUS_BOUND, rewritten.getStatus());
        assertNotNull(rewritten.getAccount());
        assertEquals("A0009999", rewritten.getAccount().getAccountNumber());
        assertEquals(PCConstants.STATUS_CANCELED, dataStore.findSubmission("S000_REWRITE_RNA_BASE").getStatus());
    }
}
