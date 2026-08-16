package com.guidewire.pc;

import com.guidewire.pc.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("End-to-End Enterprise Insurance Features Integration Test")
public class EnterpriseFeaturesIntegrationTest {

    @Test
    @DisplayName("Should verify complete integration flow for Commercial Property Quote & Accelerators")
    void testEndToEndCommercialPropertyWorkflow() {
        // Step 1: Pre-Fill Property Specs from Address
        var propProfile = PropertyPreFillService.getInstance().lookupPropertyProfile("100 Ocean Drive, Miami, FL", "33139");
        assertNotNull(propProfile);
        assertEquals("Miami", propProfile.city);
        assertEquals("6", propProfile.isoConstructionClass);

        // Step 2: Retrieve Prior Loss History & Loss Modifier
        var lossReport = PriorLossService.getInstance().retrievePriorLossHistory("TAX-94-1829104-CLEAN", new BigDecimal("45000.00"));
        assertNotNull(lossReport);
        assertEquals(new BigDecimal("0.85"), lossReport.lossModifierFactor);

        // Step 3: Rate Commercial Property Package with Endorsements
        Map<String, Object> cpInput = new HashMap<>();
        cpInput.put("buildingLimit", propProfile.estimatedReplacementCost.toString());
        cpInput.put("bppLimit", "2500000.00");
        cpInput.put("protectionClass", propProfile.protectionClass);
        cpInput.put("earthquake", false);
        cpInput.put("flood", true);
        cpInput.put("sprinkler", propProfile.sprinklered);
        cpInput.put("tenantsImprovementLimit", "500000.00");
        cpInput.put("tenantsValuationBasis", "ReplacementCost");
        cpInput.put("businessIncomeLimit", "1000000.00");
        cpInput.put("indemnityFraction", "1/3");
        cpInput.put("includePayroll", true);
        cpInput.put("equipmentBreakdownLimit", "1500000.00");
        cpInput.put("hasProductionMachinery", false);

        Map<String, Object> ratedPackage = CPRatingService.rateFullCommercialPropertyPackage(cpInput);
        assertEquals("SUCCESS", ratedPackage.get("status"));
        BigDecimal packagePrem = (BigDecimal) ratedPackage.get("totalPackagePremium");
        assertTrue(packagePrem.compareTo(BigDecimal.ZERO) > 0);

        // Apply Loss Modifier (-15% preferred discount)
        BigDecimal finalModifiedPremium = packagePrem.multiply(lossReport.lossModifierFactor).setScale(2, java.math.RoundingMode.HALF_UP);
        assertTrue(finalModifiedPremium.compareTo(packagePrem) < 0);

        // Step 4: Generate Reinsurance Placement Slip for high-value portfolio
        var slip = ReinsuranceSlipGenerator.getInstance().generatePlacementSlip(null, "QUOTA_SHARE", propProfile.estimatedReplacementCost, finalModifiedPremium, 40.0);
        assertNotNull(slip);
        assertEquals(3, slip.syndicateParticipants.size());
        assertTrue(slip.totalCededPremium.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(slip.netCarrierPremium.compareTo(BigDecimal.ZERO) > 0);
    }
}
