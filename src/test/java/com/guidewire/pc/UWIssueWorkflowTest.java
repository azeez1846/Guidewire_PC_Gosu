package com.guidewire.pc;

import com.guidewire.pc.constants.PCConstants;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.UWIssue;
import com.guidewire.pc.service.DataStoreService;
import com.guidewire.pc.service.PolicyLifecycleService;
import com.guidewire.pc.service.UWAuthorityEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Guidewire OOTB Underwriting Authority & Issue Approval Workflow Tests")
public class UWIssueWorkflowTest {

    private DataStoreService dataStore;
    private PolicyLifecycleService lifecycleService;

    @BeforeEach
    public void setUp() {
        dataStore = DataStoreService.getInstance();
        lifecycleService = PolicyLifecycleService.getInstance();
    }

    @Test
    @DisplayName("Should raise High Premium UW Issue when premium exceeds $50,000")
    public void testHighPremiumUWIssue() {
        PolicyPeriod period = new PolicyPeriod();
        period.setJobNumber("S000_UW_TEST_1");
        period.setTotalPremium(new BigDecimal("75000.00"));

        List<UWIssue> issues = UWAuthorityEngine.getInstance().evaluatePolicy(period);
        assertNotNull(issues);
        assertTrue(issues.stream().anyMatch(i -> "UW_HIGH_PREMIUM".equalsIgnoreCase(i.getIssueCode())));
        assertTrue(period.hasBlockingBindIssues());
    }

    @Test
    @DisplayName("Should raise High BI Limit UW Issue when limit is $1M/$1M")
    public void testHighBILimitUWIssue() {
        PolicyPeriod period = new PolicyPeriod();
        period.setJobNumber("S000_UW_TEST_2");
        period.setBodilyInjuryLimit("$1M/$1M");

        List<UWIssue> issues = UWAuthorityEngine.getInstance().evaluatePolicy(period);
        assertNotNull(issues);
        assertTrue(issues.stream().anyMatch(i -> "UW_HIGH_BI_LIMIT".equalsIgnoreCase(i.getIssueCode())));
        assertEquals("ExecutiveUnderwriter", issues.get(0).getRequiredAuthorityLevel());
    }

    @Test
    @DisplayName("Should block bind when open blocking bind UW issue exists, then succeed upon approval")
    public void testBlockAndApproveWorkflow() {
        PolicyPeriod period = new PolicyPeriod();
        period.setJobNumber("S000_UW_TEST_3");
        period.setBodilyInjuryLimit("$2M/$2M");
        period.setTotalPremium(new BigDecimal("60000.00"));
        dataStore.createSubmission(period);

        UWAuthorityEngine.getInstance().evaluatePolicy(period);
        assertTrue(period.hasBlockingBindIssues());

        // Attempting to bind should throw IllegalStateException
        Exception ex = assertThrows(IllegalStateException.class, () -> {
            lifecycleService.bindSubmissionBranch("S000_UW_TEST_3");
        });
        assertNotNull(ex.getMessage());

        // Underwriter approves all open issues
        for (UWIssue issue : period.getOpenBlockingBindIssues()) {
            issue.approve("uw_manager", "Approved after risk committee audit");
        }

        assertFalse(period.hasBlockingBindIssues());

        // Binding should now succeed
        PolicyPeriod bound = lifecycleService.bindSubmissionBranch("S000_UW_TEST_3");
        assertEquals(PCConstants.STATUS_BOUND, bound.getStatus());
    }
}
