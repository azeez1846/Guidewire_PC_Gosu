package com.guidewire.pc;

import com.guidewire.ig.address.dto.AddressValidationResponse;
import com.guidewire.pc.service.AddressStandardizationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AddressStandardizationServiceTest {

    @Test
    public void testAddressStandardizationServiceGatewayLookup() {
        AddressValidationResponse response = AddressStandardizationService.getInstance().executeAddressStandardization(
            "100 California St",
            "",
            "San Francisco",
            "CA",
            "94111",
            "USA"
        );

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getAddressSpecs());
        assertEquals("USPS_STANDARDIZED", response.getStandardizationStatus());
        assertTrue(response.getIsDeliverable());
        assertNotNull(response.getAddressSpecs().getStandardizedAddressLine1());
        assertNotNull(response.getGatewayMetadata());
        assertTrue(response.getGatewayMetadata().contains("Integration Gateway"));
    }
}
