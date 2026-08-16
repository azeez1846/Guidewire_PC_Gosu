package com.guidewire.pc;

import com.guidewire.pc.service.SanctionsComplianceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OFAC / PEP Sanctions & AML Screener Tests")
public class SanctionsComplianceServiceTest {

    @Test
    @DisplayName("Should clear legitimate commercial subjects with zero sanctions match")
    void testClearSubjectScreening() {
        var res = SanctionsComplianceService.getInstance().screenSubject("Apex Commercial Logistics", "USA", "COMMERCIAL_ORGANIZATION");
        assertNotNull(res);
        assertEquals("CLEAR", res.screeningDisposition);
        assertFalse(res.isBindingBlocked);
        assertFalse(res.sarFilingRecommended);
        assertEquals(0.0, res.highestConfidenceScore);
        assertTrue(res.matches.isEmpty());
    }

    @Test
    @DisplayName("Should execute hard binding lock and SAR recommendation for positive OFAC SDN matches")
    void testPositiveOfacMatchBindingLock() {
        var res = SanctionsComplianceService.getInstance().screenSubject("Petrov Blocked Holdings Ltd", "Russia", "COMMERCIAL_ORGANIZATION");
        assertNotNull(res);
        assertEquals("HARD_BLOCK_SANCTIONED", res.screeningDisposition);
        assertTrue(res.isBindingBlocked);
        assertTrue(res.sarFilingRecommended);
        assertTrue(res.highestConfidenceScore >= 90.0);
        assertFalse(res.matches.isEmpty());
        assertTrue(res.complianceOfficerGuidance.contains("CRITICAL"));
    }
}
