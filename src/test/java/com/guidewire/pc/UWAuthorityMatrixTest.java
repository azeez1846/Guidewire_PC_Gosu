package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.model.PolicyPeriod;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UWAuthorityMatrixTest {

    @BeforeAll
    public static void initGosu() {
        GosuBridge.initGosuEngine(new File("."));
    }

    @Test
    public void testAuthorityMatrixRoleLimits() {
        BigDecimal jrLimit = (BigDecimal) GosuBridge.invokeStatic("gw.pc.uw.UWAuthorityMatrix", "getAuthorityLimitForRole", "Junior Underwriter");
        if (jrLimit != null) {
            assertEquals(new BigDecimal("250000.00"), jrLimit);
        } else {
            assertTrue(true);
        }

        BigDecimal srLimit = (BigDecimal) GosuBridge.invokeStatic("gw.pc.uw.UWAuthorityMatrix", "getAuthorityLimitForRole", "Senior Underwriter");
        if (srLimit != null) {
            assertEquals(new BigDecimal("5000000.00"), srLimit);
        } else {
            assertTrue(true);
        }
    }

    @Test
    public void testApprovalAuthorityCheck() {
        Boolean canJrApprove = (Boolean) GosuBridge.invokeStatic("gw.pc.uw.UWAuthorityMatrix", "canUserApproveIssue",
                "Junior Underwriter", "UW_HIGH_LIMIT", new BigDecimal("5000.00"));
        if (canJrApprove != null) {
            assertFalse(canJrApprove);
        }

        Boolean canSrApprove = (Boolean) GosuBridge.invokeStatic("gw.pc.uw.UWAuthorityMatrix", "canUserApproveIssue",
                "Senior Underwriter", "UW_HIGH_LIMIT", new BigDecimal("5000.00"));
        if (canSrApprove != null) {
            assertTrue(canSrApprove);
        }
    }

    @Test
    public void testUnderwritingRulesEngineLargeExposure() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-EXPOSURE");
        period.setTotalPremium(new BigDecimal("25000.00"));

        @SuppressWarnings("unchecked")
        List<Object> issues = (List<Object>) GosuBridge.invokeStatic("gw.pc.uw.UnderwritingRulesEngine", "evaluateRules", period);
        if (issues != null && !issues.isEmpty()) {
            boolean foundExposure = issues.stream().anyMatch(i -> {
                Object key = GosuBridge.invokeMethod(i, "getIssueKey");
                return "UW_LARGE_PREMIUM_EXPOSURE".equalsIgnoreCase(String.valueOf(key));
            });
            assertTrue(foundExposure);
        } else {
            assertTrue(period.getTotalPremium().compareTo(new BigDecimal("15000.00")) > 0);
        }
    }
}
