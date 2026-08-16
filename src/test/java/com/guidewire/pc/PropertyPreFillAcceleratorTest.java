package com.guidewire.pc;

import com.guidewire.pc.service.PropertyPreFillService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Guidewire Accelerator #7: Property Pre-Fill & Hazard Intelligence Tests")
public class PropertyPreFillAcceleratorTest {

    @Test
    @DisplayName("Should lookup coastal hurricane risk profile for Miami FL address")
    void testMiamiPropertyProfile() {
        var profile = PropertyPreFillService.getInstance().lookupPropertyProfile("100 Ocean Drive, Miami Beach, FL", "33139");

        assertNotNull(profile);
        assertEquals("Miami", profile.city);
        assertEquals("FL", profile.state);
        assertEquals("6", profile.isoConstructionClass); // Fire resistive
        assertEquals("Flat", profile.roofGeometry);
        assertEquals("AE", profile.femaFloodZone);
        assertTrue(profile.windPoolTier.contains("Tier 1"));
        assertEquals("2", profile.protectionClass);
        assertTrue(profile.sprinklered);
        assertTrue(profile.wildfireRiskScore < 20);
    }

    @Test
    @DisplayName("Should lookup wildfire risk profile for Malibu CA address")
    void testMalibuWildfireProfile() {
        var profile = PropertyPreFillService.getInstance().lookupPropertyProfile("24000 Pacific Coast Hwy, Malibu, CA", "90265");

        assertNotNull(profile);
        assertEquals("Malibu", profile.city);
        assertEquals("CA", profile.state);
        assertEquals("3", profile.isoConstructionClass); // Non-combustible
        assertEquals("Hip", profile.roofGeometry);
        assertEquals("X", profile.femaFloodZone);
        assertTrue(profile.wildfireRiskScore >= 80, "Malibu property should have severe wildfire score");
        assertTrue(profile.wildfireRiskLevel.contains("EXTREME"));
    }

    @Test
    @DisplayName("Should convert Property profile to response map")
    void testToMapConversion() {
        var profile = PropertyPreFillService.getInstance().lookupPropertyProfile("500 Michigan Ave, Chicago, IL", "60611");
        var map = PropertyPreFillService.getInstance().toMap(profile);

        assertEquals("SUCCESS", map.get("status"));
        assertEquals("Chicago", map.get("city"));
        assertEquals("IL", map.get("state"));
        assertEquals("4", map.get("isoConstructionClass"));
        assertEquals("1", map.get("protectionClass"));
    }
}
