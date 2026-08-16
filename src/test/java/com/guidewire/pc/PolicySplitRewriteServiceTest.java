package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.PolicySplitRewriteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Policy Split & Spin-Off Rewrite Transaction Tests")
public class PolicySplitRewriteServiceTest {

    @Test
    @DisplayName("Should execute policy split and correctly allocate child and retained parent premiums")
    void testPolicySplitExecution() {
        PolicyPeriod parent = new PolicyPeriod();
        parent.setPolicyNumber("POL-PARENT-9001");
        parent.setTotalPremium(new BigDecimal("40000.00"));

        List<String> spunAssets = List.of(
                "Location #2 (9400 Industrial Pkwy, Reno, NV)",
                "Commercial Delivery Vans (5 Units)"
        );

        var res = PolicySplitRewriteService.getInstance().executePolicySplit(
                parent, "Apex Mountain Logistics LLC", spunAssets, 0.40
        );

        assertNotNull(res);
        assertEquals("POL-PARENT-9001", res.parentPolicyNumber);
        assertTrue(res.newChildPolicyNumber.startsWith("POL-SPIN-"));
        assertEquals("Apex Mountain Logistics LLC", res.newNamedInsured);
        assertEquals(new BigDecimal("40000.00"), res.originalParentAnnualPremium);
        assertEquals(new BigDecimal("16000.00"), res.newChildAnnualPremium); // 40%
        assertEquals(new BigDecimal("24000.00"), res.retainedParentAnnualPremium); // 60%
        assertEquals(2, res.spunOffAssets.size());
        assertTrue(res.claimsHistoryLinkagePreserved);
        assertEquals("SPLIT_COMPLETED", res.workflowStatus);
    }
}
