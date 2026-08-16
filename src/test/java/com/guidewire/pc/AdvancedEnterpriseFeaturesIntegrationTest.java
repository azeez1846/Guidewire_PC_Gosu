package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Advanced Enterprise Insurance & Accelerators Integrated Workflow Tests")
public class AdvancedEnterpriseFeaturesIntegrationTest {

    @Test
    @DisplayName("Should execute complete intake -> screening -> composite rating -> binder -> accounting workflow")
    void testEndToEndCommercialInsurancePipeline() {
        // Step 1: Corporate Intake Verification via Secretary of State & D&B
        var sosReport = SOSEntityVerificationService.getInstance().verifyBusinessEntity(
                "Apex Global Logistics Corp", "94-8192014", "DE"
        );
        assertNotNull(sosReport);
        assertTrue(sosReport.isEligibleToBind);

        // Step 2: OFAC / PEP Sanctions Screening
        var ofacReport = SanctionsComplianceService.getInstance().screenSubject(
                sosReport.searchBusinessName, "USA", "COMMERCIAL_ORGANIZATION"
        );
        assertNotNull(ofacReport);
        assertFalse(ofacReport.isBindingBlocked);

        // Step 3: Commercial Lines Multi-Exposure Composite Rating
        var glQuote = GLCompositeRatingEngine.getInstance().rateCompositeGL(
                null,
                new BigDecimal("5000000.00"), // $5M gross sales
                new BigDecimal("60000.00"),   // 60k sq ft
                new BigDecimal("1200000.00"), // $1.2M payroll
                new BigDecimal("1000000.00"), // $1M OCP
                "NONE",
                BigDecimal.ZERO,
                new BigDecimal("2000000.00")
        );
        assertNotNull(glQuote);
        assertTrue(glQuote.totalCompositePremium.compareTo(BigDecimal.ZERO) > 0);

        // Step 4: Inland Marine Machinery Schedule Rating
        var imQuote = ContractorsEquipmentEngine.getInstance().rateContractorsEquipment(
                null,
                new BigDecimal("1200000.00"),
                new BigDecimal("250000.00"),
                new BigDecimal("50000.00"),
                "REPLACEMENT_COST",
                true,
                new BigDecimal("5000.00")
        );
        assertNotNull(imQuote);
        assertTrue(imQuote.totalEquipmentFloaterPremium.compareTo(BigDecimal.ZERO) > 0);

        // Step 5: AI Executive Binder Explainer Generation
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-APEX-2026-01");
        period.setProductCode("CommercialPackage");
        BigDecimal totalPackagePrem = glQuote.totalCompositePremium.add(imQuote.totalEquipmentFloaterPremium);
        period.setTotalPremium(totalPackagePrem);

        var binderSummary = PolicyBinderExplainerService.getInstance().generateExecutiveSummary(period);
        assertNotNull(binderSummary);
        assertEquals(totalPackagePrem, binderSummary.totalAnnualPremium);
        assertTrue(binderSummary.downPaymentRequired.compareTo(BigDecimal.ZERO) > 0);

        // Step 6: Loss Control Survey Verification
        var lossSurvey = LossControlInspectionService.getInstance().generateSurveyReport(
                period.getPolicyNumber(), "9400 Industrial Blvd", false, false
        );
        assertNotNull(lossSurvey);
        assertFalse(lossSurvey.triggersDirectNoticeOfCancellation);

        // Step 7: Broker Agency Bill Account Current Reconciliation
        var lineItem = new AgencyBillAccountCurrentService.AccountCurrentLineItem(
                period.getPolicyNumber(), sosReport.searchBusinessName, "NEW_BUSINESS", totalPackagePrem, new BigDecimal("15.0")
        );
        var agencyStmt = AgencyBillAccountCurrentService.getInstance().generateAccountCurrent(
                "PR-WEST-901", "Pacific Coast Brokers", "2026-08", java.util.List.of(lineItem)
        );
        assertNotNull(agencyStmt);
        assertEquals(totalPackagePrem, agencyStmt.totalGrossWrittenPremium);
        assertEquals("BALANCED_RECONCILED", agencyStmt.reconciliationStatus);
    }
}
