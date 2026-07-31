package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.model.Cost;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.rules.RuleContext;
import com.guidewire.pc.rules.RulesEngine;
import com.guidewire.pc.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EdgeCasesAndBoundaryTest {

    private PolicyLifecycleService lifecycleService;

    @BeforeEach
    public void setUp() {
        lifecycleService = PolicyLifecycleService.getInstance();
    }

    @Test
    public void testNullAndEmptyInputsInRatingEngine() {
        List<Cost> nullCosts = RatingEngine.getInstance().rate(null);
        assertNotNull(nullCosts);
        assertTrue(nullCosts.isEmpty());

        PolicyPeriod emptyPeriod = new PolicyPeriod();
        List<Cost> costs = RatingEngine.getInstance().rate(emptyPeriod);
        assertNotNull(costs);
        assertFalse(costs.isEmpty());
        assertNotNull(emptyPeriod.getBasePremium());
    }

    @Test
    public void testPolicyLifecycleExceptionGuards() {
        Throwable ex1 = assertThrows(IllegalArgumentException.class, () -> {
            lifecycleService.startPolicyChange("NON-EXISTENT-POL-999", "2026-06-01", "$500k", "$500");
        });
        assertNotNull(ex1.getMessage());

        Throwable ex2 = assertThrows(IllegalArgumentException.class, () -> {
            lifecycleService.cancelPolicy("NON-EXISTENT-POL-999", "NonPayment", "ProRata", "2026-06-01");
        });
        assertNotNull(ex2.getMessage());

        Throwable ex3 = assertThrows(IllegalArgumentException.class, () -> {
            lifecycleService.renewPolicy("NON-EXISTENT-POL-999");
        });
        assertNotNull(ex3.getMessage());
    }

    @Test
    public void testSearchServiceBoundaryQueries() {
        SearchService searchService = SearchService.getInstance();

        SearchService.SearchResult nullRes = searchService.executeSearch(null);
        assertNotNull(nullRes);
        assertEquals(SearchService.SearchResultType.NO_MATCH, nullRes.getResultType());

        SearchService.SearchResult emptyRes = searchService.executeSearch("   ");
        assertNotNull(emptyRes);
        assertEquals(SearchService.SearchResultType.NO_MATCH, emptyRes.getResultType());

        SearchService.SearchResult invalidRes = searchService.executeSearch("INVALID-JOB-XXXX-999999");
        assertNotNull(invalidRes);
        assertEquals(SearchService.SearchResultType.NO_MATCH, invalidRes.getResultType());
    }

    @Test
    public void testClaimCenterFnolEdgeCases() {
        ClaimCenterService cc = ClaimCenterService.getInstance();

        List<ClaimCenterService.Claim> nullClaims = cc.getClaimsForPolicy(null);
        assertNotNull(nullClaims);
        assertTrue(nullClaims.isEmpty());

        ClaimCenterService.Claim fnol = cc.reportFnol("POL-EDGE-1", "Minor scratches", new BigDecimal("150.00"));
        assertNotNull(fnol);
        assertEquals("POL-EDGE-1", fnol.getPolicyNumber());
        assertEquals(new BigDecimal("150.00"), fnol.getLossAmount());
    }

    @Test
    public void testGosuScriptBridgeEvalBoundary() {
        Object accCount = GosuBridge.eval("gw.pc.service.AccountService.Instance.getAllAccounts().size()");
        assertNotNull(accCount);

        Object gosuConstantsStatus = GosuBridge.eval("gw.pc.config.PCConstants.STATUS_ISSUED");
        assertNotNull(gosuConstantsStatus);
        assertTrue(gosuConstantsStatus.toString().contains("SUCCESS"));
    }

    @Test
    public void testRulesEngineValidationEdgeCases() {
        RulesEngine rules = RulesEngine.getInstance();

        PolicyPeriod period = new PolicyPeriod();
        period.setTermMonths(0); // Invalid term

        RuleContext ctx = rules.evaluatePreQuoteRules(period);
        assertTrue(ctx.hasErrors());
        assertTrue(ctx.getErrorMessages().stream().anyMatch(msg -> msg.contains("Invalid policy term months")));
    }
}
