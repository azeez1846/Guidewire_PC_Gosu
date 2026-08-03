package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.CatastropheAccumulationEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Geospatial Catastrophe (CAT) Accumulation Engine Tests")
public class CatastropheAccumulationEngineTest {

    @Test
    @DisplayName("Should evaluate PML and trigger UW approval if CAT aggregation limit is exceeded")
    public void testCatastropheAccumulationCapExceeded() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-CAT-9001");

        CatastropheAccumulationEngine.CatAccumulationResult res = CatastropheAccumulationEngine.getInstance()
                .evaluateRiskAccumulation(period, "90210", "Wildfire_High", new BigDecimal("3500000.00"));

        assertNotNull(res);
        assertEquals(new BigDecimal("2450000.00"), res.getPmlAmount());
        assertTrue(res.isAggregationCapExceeded());
        assertTrue(res.getUnderwritingAction().contains("UW_APPROVAL_REQUIRED"));
    }
}
