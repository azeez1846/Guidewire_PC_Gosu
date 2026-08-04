package com.guidewire.pc;

import com.guidewire.ig.credit.dto.CreditFraudResponse;
import com.guidewire.ig.telematics.dto.TelematicsResponse;
import com.guidewire.pc.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("5 New Enterprise Features & Integration Gateways Unit Test Suite")
public class FiveNewEnterpriseFeaturesTest {

    @Test
    @DisplayName("Feature 1: Credit Score & OFAC Sanctions IG Gateway Test")
    public void testCreditFraudIntegrationService() {
        CreditFraudResponse response = CreditFraudIntegrationService.getInstance().executeCreditAndFraudLookup(
            "Apex Global Industrial",
            "98-7654321",
            "Corporation",
            "CA"
        );
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getScoreDetails());
        assertTrue(response.getScoreDetails().getOfacSanctionClear());
        assertNotNull(response.getUnderwritingAction());
    }

    @Test
    @DisplayName("Feature 2: ACORD 125/126 Application Ingestion Engine Test")
    public void testAcordIngestionService() {
        Map<String, Object> payload = Map.of(
            "acordFormType", "ACORD_125_COMMERCIAL_AUTO",
            "applicantName", "Apex Industrial Logistics LLC",
            "fein", "98-7654321",
            "lineOfBusiness", "CommercialAuto",
            "requestedLimit", 1000000
        );

        Map<String, Object> result = AcordIngestionService.getInstance().parseAndIngestAcordPayload(payload);
        assertNotNull(result);
        assertEquals("SUCCESSFULLY_INGESTED_AND_QUOTED", result.get("status"));
        assertNotNull(result.get("accountNumber"));
        assertNotNull(result.get("jobNumber"));
    }

    @Test
    @DisplayName("Feature 3: Out-of-Sequence (OOS) Endorsement Timeline Visualizer Test")
    public void testOOSTimelineVisualizerService() {
        Map<String, Object> result = OOSTimelineVisualizerService.getInstance().generateTimelineSlices("S0001001");
        assertNotNull(result);
        assertTrue((Boolean) result.get("oosConflictDetected"));
        assertNotNull(result.get("timelineSlices"));
    }

    @Test
    @DisplayName("Feature 4: ClaimsCenter (CC) Loss Ratio & FNOL Sync Engine Test")
    public void testClaimsCenterSyncService() {
        Map<String, Object> result = ClaimsCenterSyncService.getInstance().calculateAccountLossRatioAndSyncClaims("A0001001");
        assertNotNull(result);
        assertEquals("A0001001", result.get("accountNumber"));
        assertNotNull(result.get("lossRatioPercentage"));
        assertNotNull(result.get("recentClaims"));
    }

    @Test
    @DisplayName("Feature 5: Commercial IoT Telematics IG Gateway Test")
    public void testTelematicsIntegrationService() {
        TelematicsResponse response = TelematicsIntegrationService.getInstance().executeTelematicsIngestion(
            "FLT-CA-90812",
            "A0001001",
            15
        );
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getSafetyScore());
        assertEquals(92, response.getSafetyScore().getOverallSafetyScore());
    }
}
