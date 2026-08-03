package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.CommercialAuditEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Commercial Lines Final Exposure Audit Engine Tests")
public class CommercialAuditEngineTest {

    @Test
    @DisplayName("Should calculate earned premium delta for higher audited payroll exposure")
    public void testCompletedAuditHigherExposure() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-AUDIT-101");
        period.setTotalPremium(new BigDecimal("10000.00"));

        CommercialAuditEngine.AuditResult res = CommercialAuditEngine.getInstance()
                .processFinalAudit(period, new BigDecimal("1200000.00"), new BigDecimal("1000000.00"), false);

        assertNotNull(res);
        assertEquals("COMPLETED", res.getAuditStatus());
        assertEquals(new BigDecimal("12000.00"), res.getAuditedEarnedPremium());
        assertEquals(new BigDecimal("2000.00"), res.getAuditAdjustmentAmount());
    }

    @Test
    @DisplayName("Should apply 200% ANC penalty charge for non-compliant audit")
    public void testNonCompliantAuditPenalty() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-AUDIT-909");
        period.setTotalPremium(new BigDecimal("5000.00"));

        CommercialAuditEngine.AuditResult res = CommercialAuditEngine.getInstance()
                .processFinalAudit(period, null, new BigDecimal("500000.00"), true);

        assertNotNull(res);
        assertEquals("NON_COMPLIANT", res.getAuditStatus());
        assertEquals(new BigDecimal("10000.00"), res.getAncPenaltyCharge());
        assertEquals(new BigDecimal("15000.00"), res.getAuditedEarnedPremium());
    }
}
