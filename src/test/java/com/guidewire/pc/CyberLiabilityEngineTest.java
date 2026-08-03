package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.CyberLiabilityEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Cyber Liability Security Posture & Ransomware Sub-Limit Engine Tests")
public class CyberLiabilityEngineTest {

    @Test
    @DisplayName("Should enforce $250,000 ransomware sub-limit cap and +30% surcharge when MFA is disabled")
    public void testCyberRansomwareSubLimitCapNoMFA() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-CYBER-4001");
        period.setTotalPremium(new BigDecimal("5000.00"));

        CyberLiabilityEngine.CyberResult res = CyberLiabilityEngine.getInstance()
                .evaluateCyberSecurityControls(period, false, true, true, true); // MFA = false

        assertNotNull(res);
        assertFalse(res.isMfaEnabled());
        assertTrue(res.isSubLimitCapped());
        assertEquals(new BigDecimal("250000.00"), res.getRansomwareSubLimit());
        assertEquals(1.30, res.getRateModifier());
        assertEquals(new BigDecimal("6500.00"), res.getAdjustedCyberPremium());
    }
}
