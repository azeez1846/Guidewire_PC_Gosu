package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.RIAgreement;
import com.guidewire.pc.model.RICession;
import com.guidewire.pc.service.ReinsuranceRIRulesEngine;
import com.guidewire.pc.service.ReinsuranceService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 2: Reinsurance Management & Risk Cession Engine Tests")
public class ReinsuranceCessionTest {

    private PolicyPeriod policyPeriod;

    @BeforeEach
    public void setUp() {
        GosuBridge.initGosuEngine(new File("."));
        policyPeriod = new PolicyPeriod();
        policyPeriod.setPolicyNumber("POL-RI-2002");
        policyPeriod.setProductCode("CommercialProperty");
        policyPeriod.setTotalPremium(new BigDecimal("10000.00"));
    }

    @Test
    @DisplayName("Test 1: Quota Share Treaty Cession (25% Ceding)")
    public void testQuotaShareCession() {
        RIAgreement agreement = new RIAgreement("TREATY-QS-01", "Commercial Property QS", "QuotaShare", new BigDecimal("2000000.00"), new BigDecimal("25.00"));

        RICession cession = ReinsuranceService.calculateCession(policyPeriod, agreement, new BigDecimal("1000000.00"));

        assertNotNull(cession);
        assertEquals(new BigDecimal("1000000.00"), cession.getGrossRiskExposure());
        assertEquals(new BigDecimal("250000.00"), cession.getCededExposure()); // 25% of 1M
        assertEquals(new BigDecimal("750000.00"), cession.getRetainedExposure()); // 75% of 1M
        assertEquals(new BigDecimal("2500.00"), cession.getCededPremium()); // 25% of 10k
        assertFalse(cession.isRequiresFacultative());
    }

    @Test
    @DisplayName("Test 2: Excess of Loss Treaty Cession Above Attachment Point")
    public void testExcessOfLossCessionAboveAttachment() {
        RIAgreement agreement = new RIAgreement("TREATY-XOL-01", "Catastrophe XOL", "ExcessOfLoss", new BigDecimal("2000000.00"), BigDecimal.ZERO);
        agreement.setAttachmentPoint(new BigDecimal("500000.00"));

        RICession cession = ReinsuranceService.calculateCession(policyPeriod, agreement, new BigDecimal("1200000.00"));

        assertNotNull(cession);
        assertEquals(new BigDecimal("1200000.00"), cession.getGrossRiskExposure());
        assertEquals(new BigDecimal("700000.00"), cession.getCededExposure()); // 1.2M - 500k = 700k
        assertEquals(new BigDecimal("500000.00"), cession.getRetainedExposure());
        assertTrue(cession.getCededPremium().compareTo(BigDecimal.ZERO) > 0);
        assertFalse(cession.isRequiresFacultative());
    }

    @Test
    @DisplayName("Test 3: Excess of Loss Treaty Below Attachment Point (Zero Ceding)")
    public void testExcessOfLossCessionBelowAttachment() {
        RIAgreement agreement = new RIAgreement("TREATY-XOL-01", "Catastrophe XOL", "ExcessOfLoss", new BigDecimal("2000000.00"), BigDecimal.ZERO);
        agreement.setAttachmentPoint(new BigDecimal("500000.00"));

        RICession cession = ReinsuranceService.calculateCession(policyPeriod, agreement, new BigDecimal("300000.00"));

        assertNotNull(cession);
        assertEquals(new BigDecimal("300000.00"), cession.getGrossRiskExposure());
        assertEquals(BigDecimal.ZERO, cession.getCededExposure());
        assertEquals(new BigDecimal("300000.00"), cession.getRetainedExposure());
        assertEquals(BigDecimal.ZERO, cession.getCededPremium());
        assertFalse(cession.isRequiresFacultative());
    }

    @Test
    @DisplayName("Test 4: Facultative Reinsurance Triggered When Exceeding Retention Limit")
    public void testFacultativeTriggerWhenExceedingRetention() {
        RIAgreement agreement = new RIAgreement("TREATY-QS-02", "Small Lines QS", "QuotaShare", new BigDecimal("500000.00"), new BigDecimal("10.00"));

        RICession cession = ReinsuranceService.calculateCession(policyPeriod, agreement, new BigDecimal("2000000.00"));

        assertNotNull(cession);
        assertTrue(cession.isRequiresFacultative(), "Facultative RI should be required when exposure > gross retention limit");
    }

    @Test
    @DisplayName("Test 5: Reinsurance Rules Engine Evaluation")
    public void testReinsuranceRulesEngine() {
        RIAgreement agreement = new RIAgreement("TREATY-QS-03", "Commercial Property QS", "QuotaShare", new BigDecimal("500000.00"), new BigDecimal("20.00"));

        List<String> issues = ReinsuranceRIRulesEngine.evaluateReinsuranceRules(policyPeriod, agreement, new BigDecimal("1500000.00"));

        assertNotNull(issues);
        assertFalse(issues.isEmpty());
        assertTrue(issues.stream().anyMatch(i -> i.contains("UW_FACULTATIVE_REINSURANCE_REQUIRED")));
        assertTrue(issues.stream().anyMatch(i -> i.contains("UW_REINSURANCE_TREATY_ATTACHED")));
    }
}
