package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.ReinsuranceSlipGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Guidewire Accelerator #9: Reinsurance Slip Generator Tests")
public class ReinsuranceSlipGeneratorTest {

    @Test
    @DisplayName("Should generate Quota Share placement slip with syndicate cessions")
    void testQuotaShareSlipGeneration() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-COMM-PROP-9001");
        period.setProductCode("CommercialProperty");

        BigDecimal grossLimit = new BigDecimal("20000000.00");
        BigDecimal grossPrem = new BigDecimal("80000.00");
        double cededPct = 40.0; // 40% ceded, 60% retained

        var slip = ReinsuranceSlipGenerator.getInstance().generatePlacementSlip(period, "QUOTA_SHARE", grossLimit, grossPrem, cededPct);

        assertNotNull(slip);
        assertNotNull(slip.slipReferenceNumber);
        assertEquals("QUOTA_SHARE", slip.treatyType);
        assertEquals(new BigDecimal("8000000.00"), slip.totalCededLimit);     // 20M * 0.40
        assertEquals(new BigDecimal("12000000.00"), slip.carrierRetentionLimit); // 20M * 0.60
        assertEquals(new BigDecimal("32000.00"), slip.totalCededPremium);      // 80k * 0.40
        assertEquals(new BigDecimal("8000.00"), slip.totalCedingCommission);   // 32k * 0.25 (25% comm)
        // Net carrier = 80k - 32k + 8k = 56,000
        assertEquals(new BigDecimal("56000.00"), slip.netCarrierPremium);

        assertEquals(3, slip.syndicateParticipants.size(), "Should partition across 3 syndicate reinsurers");
        assertEquals("Swiss Reinsurance America Corp", slip.syndicateParticipants.get(0).reinsurerName);
        assertEquals(45.0, slip.syndicateParticipants.get(0).sharePercentage);
    }

    @Test
    @DisplayName("Should convert Reinsurance Slip to response map")
    void testToMapConversion() {
        var slip = ReinsuranceSlipGenerator.getInstance().generatePlacementSlip(null, "EXCESS_OF_LOSS", new BigDecimal("10000000.00"), new BigDecimal("50000.00"), 50.0);
        var map = ReinsuranceSlipGenerator.getInstance().toMap(slip);

        assertEquals("SUCCESS", map.get("status"));
        assertEquals("EXCESS_OF_LOSS", map.get("treatyType"));
        assertNotNull(map.get("syndicateParticipants"));
    }
}
