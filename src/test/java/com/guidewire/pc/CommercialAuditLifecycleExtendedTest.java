package com.guidewire.pc;

import com.guidewire.pc.service.CommercialAuditEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Commercial Multi-Classification Audit Lifecycle Tests")
public class CommercialAuditLifecycleExtendedTest {

    @Test
    @DisplayName("Should execute multi-class code payroll audit with state assessment surcharge")
    void testMultiClassAuditExecution() {
        List<CommercialAuditEngine.ClassCodeAuditExposure> exposures = new ArrayList<>();
        // Code 8810: Clerical (Estimated $200k -> Actual $250k @ $0.50 per $100)
        exposures.add(new CommercialAuditEngine.ClassCodeAuditExposure("8810", "Clerical Office", new BigDecimal("0.50"), new BigDecimal("200000.00"), new BigDecimal("250000.00")));
        // Code 5183: Plumbing Techs (Estimated $400k -> Actual $500k @ $5.00 per $100)
        exposures.add(new CommercialAuditEngine.ClassCodeAuditExposure("5183", "Plumbing Field", new BigDecimal("5.00"), new BigDecimal("400000.00"), new BigDecimal("500000.00")));

        double stateAssessment = 4.0; // 4% state assessment

        var result = CommercialAuditEngine.getInstance().executeMultiClassAudit("POL-WC-AUDIT-55", exposures, stateAssessment);

        assertNotNull(result);
        assertEquals("COMPLETED", result.status);
        assertEquals("ADDITIONAL_PREMIUM_DUE", result.adjustmentType);

        // Estimated = (2000*0.50) + (4000*5.00) = 1,000 + 20,000 = 21,000
        assertEquals(new BigDecimal("21000.00"), result.totalEstimatedPremium);
        // Audited = (2500*0.50) + (5000*5.00) = 1,250 + 25,000 = 26,250
        assertEquals(new BigDecimal("26250.00"), result.totalAuditedPremium);
        // State Assessment = 26,250 * 0.04 = 1,050
        assertEquals(new BigDecimal("1050.00"), result.stateAssessmentSurcharge);
        // Final Earned = 26,250 + 1,050 = 27,300
        assertEquals(new BigDecimal("27300.00"), result.finalEarnedPremium);
        // Orig with Assess = 21,000 * 1.04 = 21,840. Net Delta = 27,300 - 21,840 = 5,460
        assertEquals(new BigDecimal("5460.00"), result.netAdjustmentAmount);
    }

    @Test
    @DisplayName("Should process audit dispute and recompute revised exposures")
    void testAuditDisputeProcessing() {
        List<CommercialAuditEngine.ClassCodeAuditExposure> exposures = new ArrayList<>();
        exposures.add(new CommercialAuditEngine.ClassCodeAuditExposure("5183", "Plumbing Field", new BigDecimal("5.00"), new BigDecimal("400000.00"), new BigDecimal("500000.00")));

        var original = CommercialAuditEngine.getInstance().executeMultiClassAudit("POL-WC-AUDIT-55", exposures, 0.0);
        assertEquals(new BigDecimal("25000.00"), original.totalAuditedPremium);

        // Policyholder disputes exposure down to $450k
        var revised = CommercialAuditEngine.getInstance().processAuditDispute(original, "Exempt executive payroll included by error", new BigDecimal("450000.00"));

        assertEquals("REVISED", revised.status);
        assertNotNull(revised.disputeReason);
        // Revised Audited = 4500 * 5.00 = 22,500
        assertEquals(new BigDecimal("22500.00"), revised.totalAuditedPremium);
    }
}
