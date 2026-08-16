package com.guidewire.pc;

import com.guidewire.pc.service.SOSEntityVerificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Secretary of State & D&B Commercial Entity Verifier Tests")
public class SOSEntityVerificationServiceTest {

    @Test
    @DisplayName("Should successfully verify legitimate active commercial entity")
    void testActiveEntityVerification() {
        var report = SOSEntityVerificationService.getInstance().verifyBusinessEntity(
                "Apex Global Industrial Corp", "94-8192014", "DE"
        );

        assertNotNull(report);
        assertEquals("ACTIVE_GOOD_STANDING", report.filingStatus);
        assertEquals("C-CORP", report.entityType);
        assertTrue(report.yearsInBusiness > 10);
        assertTrue(report.dnbPaydexScore >= 80);
        assertFalse(report.isShellCompanyRisk);
        assertTrue(report.isEligibleToBind);
        assertFalse(report.registeredOfficers.isEmpty());
    }

    @Test
    @DisplayName("Should detect suspended corporate standing and block binding")
    void testSuspendedShellEntityDetection() {
        var report = SOSEntityVerificationService.getInstance().verifyBusinessEntity(
                "Suspended Shell Holdings LLC", "99-0019283", "DE"
        );

        assertNotNull(report);
        assertEquals("SUSPENDED", report.filingStatus);
        assertTrue(report.isShellCompanyRisk);
        assertFalse(report.isEligibleToBind);
        assertTrue(report.dnbPaydexScore < 50);
        assertTrue(report.underwriterNotes.contains("CRITICAL"));
    }
}
