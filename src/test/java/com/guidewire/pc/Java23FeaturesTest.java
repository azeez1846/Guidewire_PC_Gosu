package com.guidewire.pc;

import com.guidewire.pc.model.Cost;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.PolicyRevisionDeltaRecord;
import com.guidewire.pc.security.UserContext;
import com.guidewire.pc.service.JVMDiagnosticsService;
import com.guidewire.pc.service.StructuredRatingEngine;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;

import static org.junit.jupiter.api.Assertions.*;

public class Java23FeaturesTest {

    @Test
    public void testJVMDiagnosticsAndZGC() {
        Map<String, Object> diag = JVMDiagnosticsService.getInstance().getJVMDiagnostics();
        assertNotNull(diag);
        assertNotNull(diag.get("javaVersion"));
        assertNotNull(diag.get("jvmName"));
        assertTrue((Boolean) diag.get("virtualThreadsSupported"));
        assertTrue((Boolean) diag.get("generationalZgcActive"));
    }

    @Test
    public void testJava21RecordPatternsAndSequencedCollections() {
        SequencedCollection<PolicyRevisionDeltaRecord.FieldChange> changes = new ArrayList<>();
        changes.addFirst(new PolicyRevisionDeltaRecord.FieldChange("bodilyInjuryLimit", "$250k", "$500k", "Coverage"));
        changes.addLast(new PolicyRevisionDeltaRecord.FieldChange("collisionDeductible", "$1000", "$500", "Deductible"));

        assertEquals("bodilyInjuryLimit", changes.getFirst().fieldName());
        assertEquals("collisionDeductible", changes.getLast().fieldName());

        PolicyRevisionDeltaRecord record = new PolicyRevisionDeltaRecord(
                "POL-9900",
                "R2",
                "PolicyChange",
                new BigDecimal("2000.00"),
                new BigDecimal("2350.00"),
                new BigDecimal("350.00"),
                changes
        );

        assertTrue(record.isPremiumIncrease());
        assertFalse(record.isPremiumDecrease());

        String summary = PolicyRevisionDeltaRecord.formatChangeSummary(changes.getFirst());
        assertTrue(summary.contains("[Coverage] bodilyInjuryLimit changed from '$250k' to '$500k'"));

        String recSummary = PolicyRevisionDeltaRecord.formatChangeSummary(record);
        assertTrue(recSummary.contains("Policy POL-9900"));
    }

    @Test
    public void testScopedValuesContext() {
        assertEquals("system", UserContext.getCurrentUser());
        assertEquals("PR-10000", UserContext.getProducerCode());

        UserContext.runWithUser("underwriter_bob", "PR-8849", () -> {
            assertEquals("underwriter_bob", UserContext.getCurrentUser());
            assertEquals("PR-8849", UserContext.getProducerCode());
        });

        assertEquals("system", UserContext.getCurrentUser());
    }

    @Test
    public void testStructuredTaskScopeRating() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-STRUCT-1");
        period.setBasePremium(new BigDecimal("3200.00"));
        period.setBodilyInjuryLimit("$1M/$1M");
        period.setCollisionDeductible("$500");

        List<Cost> costs = StructuredRatingEngine.getInstance().rateMultiLineStructured(period);
        assertNotNull(costs);
        assertFalse(costs.isEmpty());
        assertTrue(costs.stream().anyMatch(c -> "BasePremium".equals(c.getChargePattern())));
        assertTrue(costs.stream().anyMatch(c -> "BodilyInjuryCoverage".equals(c.getChargePattern())));
        assertTrue(costs.stream().anyMatch(c -> "CollisionCoverage".equals(c.getChargePattern())));
        assertTrue(costs.stream().anyMatch(c -> "StateTax".equals(c.getChargePattern())));
    }
}
