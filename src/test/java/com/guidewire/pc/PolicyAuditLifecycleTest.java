package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.model.AuditInformation;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.Transaction;
import com.guidewire.pc.service.AuditJobService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 1: Policy Final Audit Job Engine Tests")
public class PolicyAuditLifecycleTest {

    private PolicyPeriod policyPeriod;

    @BeforeEach
    public void setUp() {
        GosuBridge.initGosuEngine(new File("."));
        policyPeriod = new PolicyPeriod();
        policyPeriod.setPolicyNumber("POL-AUDIT-1001");
        policyPeriod.setProductCode("CommercialAuto");
        policyPeriod.setBasePremium(new BigDecimal("5000.00"));
        policyPeriod.setTotalPremium(new BigDecimal("5400.00"));
        policyPeriod.setStatus("Expired");
    }

    @Test
    @DisplayName("Test 1: Start Audit Initialization")
    public void testStartAuditInitialization() {
        AuditInformation auditInfo = AuditJobService.startAudit(
            policyPeriod, "FinalAudit", "Physical", new BigDecimal("50000.00")
        );

        assertNotNull(auditInfo, "AuditInformation should not be null");
        assertEquals("FinalAudit", auditInfo.getAuditType());
        assertEquals("Physical", auditInfo.getAuditMethod());
        assertEquals("Draft", auditInfo.getAuditStatus());
        assertEquals(new BigDecimal("50000.00"), auditInfo.getEstimatedExposure());
        assertNotNull(auditInfo.getAuditDueDate());
    }

    @Test
    @DisplayName("Test 2: Enter Audited Exposure & Status Update")
    public void testEnterAuditedExposure() {
        AuditInformation auditInfo = AuditJobService.startAudit(
            policyPeriod, "FinalAudit", "Voluntary", new BigDecimal("100000.00")
        );

        AuditInformation updated = AuditJobService.enterAuditedExposure(
            auditInfo, new BigDecimal("150000.00")
        );

        assertNotNull(updated);
        assertEquals("InProcess", updated.getAuditStatus());
        assertEquals(new BigDecimal("150000.00"), updated.getAuditedExposure());
    }

    @Test
    @DisplayName("Test 3: Additional Premium Audit Adjustment Calculation (+50% Exposure)")
    public void testAuditAdjustmentAdditionalPremium() {
        AuditInformation auditInfo = new AuditInformation("FinalAudit", "Physical", new BigDecimal("100000.00"));
        auditInfo.setAuditedExposure(new BigDecimal("150000.00")); // +50%

        BigDecimal adjustment = AuditJobService.calculateAuditAdjustment(auditInfo, policyPeriod);

        // Original BasePremium = 5000.00. Exposure ratio = 1.50 -> Audited Premium = 7500.00 -> Adjustment = +2500.00
        assertNotNull(adjustment);
        assertEquals(new BigDecimal("2500.00"), adjustment);
    }

    @Test
    @DisplayName("Test 4: Premium Refund Audit Adjustment (-20% Exposure)")
    public void testAuditAdjustmentRefund() {
        AuditInformation auditInfo = new AuditInformation("FinalAudit", "Voluntary", new BigDecimal("100000.00"));
        auditInfo.setAuditedExposure(new BigDecimal("80000.00")); // -20%

        BigDecimal adjustment = AuditJobService.calculateAuditAdjustment(auditInfo, policyPeriod);

        // Original BasePremium = 5000.00. Exposure ratio = 0.80 -> Audited Premium = 4000.00 -> Adjustment = -1000.00
        assertNotNull(adjustment);
        assertEquals(new BigDecimal("-1000.00"), adjustment);
    }

    @Test
    @DisplayName("Test 5: Edge Case - Zero Estimated Exposure Handled Gracefully")
    public void testAuditAdjustmentZeroEstimated() {
        AuditInformation auditInfo = new AuditInformation("FinalAudit", "Phone", BigDecimal.ZERO);
        auditInfo.setAuditedExposure(new BigDecimal("50000.00"));

        BigDecimal adjustment = AuditJobService.calculateAuditAdjustment(auditInfo, policyPeriod);

        assertNotNull(adjustment);
        assertEquals(BigDecimal.ZERO, adjustment);
    }

    @Test
    @DisplayName("Test 6: Close Audit Workflow & Transaction Generation")
    public void testCloseAuditWorkflow() {
        AuditInformation auditInfo = new AuditInformation("FinalAudit", "Physical", new BigDecimal("100000.00"));
        auditInfo.setAuditedExposure(new BigDecimal("120000.00")); // +20% adjustment = +1000.00

        AuditInformation closedAudit = AuditJobService.closeAudit(auditInfo, policyPeriod);

        assertNotNull(closedAudit);
        assertEquals("Closed", closedAudit.getAuditStatus());
        assertEquals(new BigDecimal("1000.00"), closedAudit.getAuditPremiumAdjustment());
        assertNotNull(closedAudit.getAuditCompleteDate());

        Transaction tx = AuditJobService.createAuditTransaction(closedAudit, policyPeriod);

        assertNotNull(tx);
        assertEquals(policyPeriod.getPolicyNumber(), tx.getJobNumber());
        assertEquals(new BigDecimal("1000.00"), tx.getAmount());
        assertEquals("AuditPremiumAdjustment", tx.getTransactionType());
    }
}
