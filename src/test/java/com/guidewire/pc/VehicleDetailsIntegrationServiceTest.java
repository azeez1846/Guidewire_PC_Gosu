package com.guidewire.pc;

import com.guidewire.ig.vehicledetails.dto.VehicleDetailsResponse;
import com.guidewire.pc.service.VehicleDetailsIntegrationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VehicleDetailsIntegrationServiceTest {

    @Test
    public void testVehicleDetailsIntegrationServiceGatewayLookup() {
        VehicleDetailsResponse response = VehicleDetailsIntegrationService.getInstance().executeVehicleLookup(
            "1FA6P8CF0R5100001",
            2025,
            "Ford",
            "F-150 SuperCrew",
            "DL-CA-778811",
            "CA",
            "CommercialAuto"
        );

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getVehicleSpecs());
        assertNotNull(response.getMvrRecord());
        assertEquals("DL-CA-778811", response.getMvrRecord().getDriverLicenseNumber());
        assertEquals("CLEAR", response.getMvrRecord().getMvrStatus());
        assertEquals("PREFERRED_AUTO_DISCOUNT", response.getUnderwritingRecommendation());
        assertNotNull(response.getGatewayMetadata());
        assertTrue(response.getGatewayMetadata().contains("Integration Gateway"));
    }
}
