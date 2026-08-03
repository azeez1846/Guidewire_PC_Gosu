package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.UWEscalationWorkflowEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Multi-Tier UW Authority Escalation & Sign-Off Workflow Engine Tests")
public class UWEscalationWorkflowEngineTest {

    @Test
    @DisplayName("Should trigger Level 2 Manager and Level 3 VP dual sign-off for high TIV and high risk score")
    public void testUWEscalationWorkflow() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-ESC-9001");

        UWEscalationWorkflowEngine.EscalationResult res = UWEscalationWorkflowEngine.getInstance()
                .processUWEscalation(period, new BigDecimal("15000000.00"), 75); // $15M TIV & 75 Risk Score

        assertNotNull(res);
        assertTrue(res.isDualSignOffRequired());
        assertEquals("PENDING_SENIOR_APPROVAL", res.getApprovalStatus());
        assertEquals(3, res.getRequiredApproverLevels().size());
        assertTrue(res.getRequiredApproverLevels().contains("Level3_VP_Underwriting"));
    }
}
