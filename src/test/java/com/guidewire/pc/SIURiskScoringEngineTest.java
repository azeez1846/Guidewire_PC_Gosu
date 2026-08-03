package com.guidewire.pc;

import com.guidewire.pc.model.Account;
import com.guidewire.pc.model.FraudRiskScore;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.DataStoreService;
import com.guidewire.pc.service.SIURiskScoringEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Enterprise Fraud Risk Scoring & SIU Referral Engine Tests")
public class SIURiskScoringEngineTest {

    private DataStoreService dataStore;

    @BeforeEach
    public void setUp() {
        dataStore = DataStoreService.getInstance();
    }

    @Test
    @DisplayName("Should calculate low risk score for clean policy")
    public void testCleanPolicyFraudScore() {
        String accNum = "A000_CLN_" + (System.currentTimeMillis() % 89999 + 10000);
        Account acc = new Account();
        acc.setAccountNumber(accNum);
        acc.setFein("12-3456789");
        acc.setState("CA");
        dataStore.createAccount(acc);

        PolicyPeriod period = new PolicyPeriod();
        period.setJobNumber("S000_SIU_CLEAN_" + (System.currentTimeMillis() % 89999 + 10000));
        period.setPolicyNumber("POL-SIU-1001");
        period.setAccount(acc);
        period.setBaseState("CA");

        FraudRiskScore score = SIURiskScoringEngine.getInstance().evaluatePolicyFraudRisk(period);
        assertNotNull(score);
        assertEquals("LOW", score.getRiskTier());
        assertFalse(score.isSiuHoldRequired());
        assertTrue(score.getTotalRiskScore() < 25);
    }

    @Test
    @DisplayName("Should calculate critical risk score and trigger SIU Hold for multi-risk signal policy")
    public void testHighRiskFraudScoreAndSIUHold() {
        String accNum = "A000_HR_" + (System.currentTimeMillis() % 89999 + 10000);
        Account acc = new Account();
        acc.setAccountNumber(accNum);
        acc.setFein(""); // Missing FEIN (+20 pts)
        acc.setState("NY"); // Mismatch vs Base State CA (+15 pts)
        dataStore.createAccount(acc);

        PolicyPeriod period = new PolicyPeriod();
        period.setJobNumber("S000_SIU_HIGH");
        period.setPolicyNumber("POL-849102"); // Has prior claims > $25k (+30 pts)
        period.setAccount(acc);
        period.setBaseState("CA");
        period.setEditEffectiveDate(new Date(System.currentTimeMillis() - 45L * 24 * 3600 * 1000)); // 45 days backdated (+20 pts)

        FraudRiskScore score = SIURiskScoringEngine.getInstance().evaluatePolicyFraudRisk(period);
        assertNotNull(score);
        assertTrue(score.getTotalRiskScore() >= 50);
        assertTrue(score.isSiuHoldRequired());
        assertEquals("HIGH", score.getRiskTier());
    }
}
