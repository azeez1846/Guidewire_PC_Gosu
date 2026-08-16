package com.guidewire.pc;

import com.guidewire.pc.service.LossControlInspectionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Loss Control Survey & Safety Recommendation Engine Tests")
public class LossControlInspectionServiceTest {

    @Test
    @DisplayName("Should generate survey report and trigger DNOC on overdue critical recommendations")
    void testCriticalSafetyFlawTriggersDNOC() {
        var survey = LossControlInspectionService.getInstance().generateSurveyReport(
                "POL-PROP-8801", "100 Industrial Parkway", true, true
        );

        assertNotNull(survey);
        assertEquals("POL-PROP-8801", survey.policyNumber);
        assertTrue(survey.triggersDirectNoticeOfCancellation);
        assertTrue(survey.openMandatoryCount > 0);
        assertTrue(survey.overallRiskScore < 50);
        assertTrue(survey.underwritingActionRequired.contains("DNOC"));
    }

    @Test
    @DisplayName("Should clear inspection survey with high risk score when no critical hazards exist")
    void testCleanInspectionSurvey() {
        var survey = LossControlInspectionService.getInstance().generateSurveyReport(
                "POL-PROP-8802", "200 Technology Drive", false, false
        );

        assertNotNull(survey);
        assertFalse(survey.triggersDirectNoticeOfCancellation);
        assertEquals(0, survey.openMandatoryCount);
        assertTrue(survey.overallRiskScore >= 90);
        assertTrue(survey.underwritingActionRequired.contains("PREFERRED RISK"));
    }
}
