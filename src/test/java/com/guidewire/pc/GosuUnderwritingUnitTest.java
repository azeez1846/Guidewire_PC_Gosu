package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.rules.RuleContext;
import com.guidewire.pc.rules.RulesEngine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class GosuUnderwritingUnitTest {

    @BeforeAll
    public static void initGosu() {
        GosuBridge.initGosuEngine(new File("."));
    }

    @Test
    public void testUWIssueLifecycle() {
        UWIssueMock issue = new UWIssueMock("HIGH_HAZARD_CARGO", "High Hazard Chemical Transport", "BlockingBind", "SeniorUW");
        assertEquals("Open", issue.getStatus());
        assertEquals("HIGH_HAZARD_CARGO", issue.getIssueKey());
        assertEquals("BlockingBind", issue.getBlockingPoint());
        assertEquals("SeniorUW", issue.getRequiredAuthority());

        issue.approve("Underwriting Manager Approved", "su");
        assertEquals("Approved", issue.getStatus());

        issue.reopen("Coverage limit increased");
        assertEquals("Open", issue.getStatus());
    }

    @Test
    public void testApprovalAuthorityLimits() {
        ApprovalAuthorityMock seniorUW = new ApprovalAuthorityMock("SeniorUW", new BigDecimal("10000000.00"));
        ApprovalAuthorityMock juniorUW = new ApprovalAuthorityMock("JuniorUW", new BigDecimal("1000000.00"));

        assertTrue(seniorUW.canApprove(new BigDecimal("5000000.00")));
        assertFalse(juniorUW.canApprove(new BigDecimal("5000000.00")));

        assertTrue(juniorUW.canApprove(new BigDecimal("500000.00")));
    }

    @Test
    public void testUnderwritingRulesEngine() {
        RulesEngine rulesEngine = RulesEngine.getInstance();

        PolicyPeriod cleanPeriod = new PolicyPeriod();
        cleanPeriod.setProductCode("PersonalAuto");
        cleanPeriod.setBaseState("CA");

        RuleContext cleanCtx = rulesEngine.evaluatePreQuoteRules(cleanPeriod);
        assertNotNull(cleanCtx);
        assertNotNull(cleanCtx.getErrorMessages());

        PolicyPeriod riskyPeriod = new PolicyPeriod();
        riskyPeriod.setProductCode("CommercialAuto");
        riskyPeriod.setBaseState("FL");
        riskyPeriod.setBodilyInjuryLimit("$1M/$1M");

        RuleContext riskyCtx = rulesEngine.evaluatePreQuoteRules(riskyPeriod);
        assertNotNull(riskyCtx);
        assertNotNull(riskyCtx.getErrorMessages());
    }

    // Mock classes representing Gosu UW model entities
    public static class UWIssueMock {
        private String issueKey;
        private String description;
        private String blockingPoint;
        private String requiredAuthority;
        private String status;

        public UWIssueMock(String key, String desc, String point, String authority) {
            this.issueKey = key;
            this.description = desc;
            this.blockingPoint = point;
            this.requiredAuthority = authority;
            this.status = "Open";
        }

        public void approve(String reason, String user) {
            this.status = "Approved";
        }

        public void reopen(String reason) {
            this.status = "Open";
        }

        public String getIssueKey() { return issueKey; }
        public String getDescription() { return description; }
        public String getBlockingPoint() { return blockingPoint; }
        public String getRequiredAuthority() { return requiredAuthority; }
        public String getStatus() { return status; }
    }

    public static class ApprovalAuthorityMock {
        private String role;
        private BigDecimal maxLimit;

        public ApprovalAuthorityMock(String role, BigDecimal limit) {
            this.role = role;
            this.maxLimit = limit;
        }

        public boolean canApprove(BigDecimal amount) {
            return amount != null && amount.compareTo(maxLimit) <= 0;
        }

        public String getRole() { return role; }
        public BigDecimal getMaxLimit() { return maxLimit; }
    }
}
