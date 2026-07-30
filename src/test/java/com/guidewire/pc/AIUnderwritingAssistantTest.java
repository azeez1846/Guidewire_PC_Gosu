package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.AIUnderwritingAssistant;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AIUnderwritingAssistantTest {

    @Test
    public void testStraightThroughProcessingApproval() {
        PolicyPeriod period = new PolicyPeriod();
        period.setJobNumber("S0009101");
        period.setTotalPremium(new BigDecimal("2889.00"));

        Map<String, Object> result = AIUnderwritingAssistant.getInstance().triageSubmission(period, 0.0, 750);
        assertEquals("STP_APPROVED", result.get("decision"));
    }

    @Test
    public void testHighRiskReferralEscalation() {
        PolicyPeriod period = new PolicyPeriod();
        period.setJobNumber("S0009102");
        period.setTotalPremium(new BigDecimal("12500.00"));

        Map<String, Object> result = AIUnderwritingAssistant.getInstance().triageSubmission(period, 0.55, 580);
        assertEquals("HIGH_RISK_REFERRAL", result.get("decision"));
    }

    @Test
    public void testStandardUnderwriterReview() {
        PolicyPeriod period = new PolicyPeriod();
        period.setJobNumber("S0009103");
        period.setTotalPremium(new BigDecimal("7500.00"));

        Map<String, Object> result = AIUnderwritingAssistant.getInstance().triageSubmission(period, 0.15, 680);
        assertEquals("STANDARD_UW_REVIEW", result.get("decision"));
    }
}
