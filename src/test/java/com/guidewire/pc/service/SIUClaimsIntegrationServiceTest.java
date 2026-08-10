package com.guidewire.pc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class SIUClaimsIntegrationServiceTest {

    private SIUClaimsIntegrationService claimsService;

    @BeforeEach
    public void setUp() {
        claimsService = SIUClaimsIntegrationService.getInstance();
    }

    @Test
    public void testProcessStandardClaimIntake() {
        SIUClaimsIntegrationService.ClaimSubmissionResult result = claimsService.processClaimIntake(
                "POL-849102",
                "Jane Doe",
                new BigDecimal("3500.00"),
                "Windstorm Damage",
                "Roof shingles damaged during hail storm",
                new Date()
        );

        assertNotNull(result);
        assertNotNull(result.getClaimNumber());
        assertEquals("POL-849102", result.getPolicyNumber());
        assertEquals("FAST_TRACK_APPROVED", result.getUnderwritingAction());
        assertFalse(result.isSiuReferralTriggered());
    }

    @Test
    public void testHighValuedLossTriggersSiuReview() {
        SIUClaimsIntegrationService.ClaimSubmissionResult result = claimsService.processClaimIntake(
                "POL-849102",
                "Robert Smith",
                new BigDecimal("120000.00"),
                "Arson",
                "Commercial warehouse total loss by fire",
                new Date()
        );

        assertNotNull(result);
        assertTrue(result.getFraudRiskScore() >= 60);
        assertTrue(result.isSiuReferralTriggered());
        assertEquals("FLAGGED_FOR_SIU_INVESTIGATION", result.getUnderwritingAction());
        assertTrue(result.getRiskSignals().stream().anyMatch(s -> s.contains("HIGH_VALUED_LOSS")));
    }

    @Test
    public void testDuplicateLossCauseDetection() {
        String polNum = "POL-DUP-TEST-" + System.currentTimeMillis();
        
        claimsService.processClaimIntake(polNum, "Alice", new BigDecimal("1000"), "Vandalism", "Window broken", new Date());
        SIUClaimsIntegrationService.ClaimSubmissionResult secondResult = claimsService.processClaimIntake(
                polNum, "Alice", new BigDecimal("1500"), "Vandalism", "Second window broken", new Date()
        );

        assertNotNull(secondResult);
        assertTrue(secondResult.getRiskSignals().stream().anyMatch(s -> s.contains("DUPLICATE_CAUSE_RECURRENCE")));
    }
}
