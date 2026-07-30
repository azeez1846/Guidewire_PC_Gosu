package com.guidewire.pc;

import com.guidewire.pc.service.VinLookupService;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class VinDecoderTest {

    @Test
    public void testValidFordVinDecode() {
        VinLookupService service = VinLookupService.getInstance();
        Map<String, Object> result = service.decodeVin("1FA6P8CF0R1234567");

        assertTrue((Boolean) result.get("valid"));
        assertEquals("1FA6P8CF0R1234567", result.get("vin"));
        assertEquals("Ford", result.get("make"));
        assertEquals("F-150 Commercial SuperDuty", result.get("model"));
        assertEquals(2024, result.get("modelYear"));
        assertEquals(48500.00, result.get("msrp"));
    }

    @Test
    public void testValidTeslaVinDecode() {
        VinLookupService service = VinLookupService.getInstance();
        Map<String, Object> result = service.decodeVin("5YJ3E1EA5S9876543");

        assertTrue((Boolean) result.get("valid"));
        assertEquals("Tesla", result.get("make"));
        assertEquals(2025, result.get("modelYear"));
        assertEquals(98, result.get("safetyScore"));
    }

    @Test
    public void testInvalidVinLength() {
        VinLookupService service = VinLookupService.getInstance();
        Map<String, Object> shortVin = service.decodeVin("1FA6P8CF0R");
        assertFalse((Boolean) shortVin.get("valid"));

        Map<String, Object> nullVin = service.decodeVin(null);
        assertFalse((Boolean) nullVin.get("valid"));
    }
}
