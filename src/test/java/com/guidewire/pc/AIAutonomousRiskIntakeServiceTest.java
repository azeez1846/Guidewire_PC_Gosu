package com.guidewire.pc;

import com.guidewire.pc.service.AIAutonomousRiskIntakeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AI Autonomous Risk Intake & Triage Unit Tests")
public class AIAutonomousRiskIntakeServiceTest {

    @Test
    @DisplayName("Should Auto-Accept Clean Risk Submissions with High Confidence")
    void testAutoAcceptCleanRisk() {
        AIAutonomousRiskIntakeService service = AIAutonomousRiskIntakeService.getInstance();

        AIAutonomousRiskIntakeService.TriageResult result = service.evaluateSubmission(
                null, "CommercialProperty", new BigDecimal("1500000.00"), 0, false
        );

        assertNotNull(result);
        assertEquals("AUTO_ACCEPT", result.getRecommendation());
        assertTrue(result.getAiConfidenceScore() >= 0.90);
        assertTrue(result.getRiskFactors().isEmpty());
    }

    @Test
    @DisplayName("Should Refer Submission to Underwriter when Moderate Hazards / High Claims Present")
    void testUnderwriterReferralTrigger() {
        AIAutonomousRiskIntakeService service = AIAutonomousRiskIntakeService.getInstance();

        AIAutonomousRiskIntakeService.TriageResult result = service.evaluateSubmission(
                null, "CommercialProperty", new BigDecimal("12000000.00"), 4, true
        );

        assertNotNull(result);
        assertEquals("UNDERWRITER_REFERRAL", result.getRecommendation());
        assertFalse(result.getRiskFactors().isEmpty());
    }

    @Test
    @DisplayName("Should Decline Submissions Exceeding Carrier Risk Thresholds")
    void testDeclineHighRiskSubmission() {
        AIAutonomousRiskIntakeService service = AIAutonomousRiskIntakeService.getInstance();

        AIAutonomousRiskIntakeService.TriageResult result = service.evaluateSubmission(
                null, "CommercialProperty", new BigDecimal("20000000.00"), 6, true
        );

        assertNotNull(result);
        assertEquals("DECLINE", result.getRecommendation());
        assertTrue(result.getRationale().contains("Risk exceeds carrier underwriting appetite"));
    }
}
