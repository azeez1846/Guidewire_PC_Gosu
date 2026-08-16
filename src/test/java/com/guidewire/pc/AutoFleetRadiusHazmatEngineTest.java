package com.guidewire.pc;

import com.guidewire.pc.service.AutoFleetRadiusHazmatEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Commercial Auto Fleet Radius & Hazmat Surcharge Tests")
public class AutoFleetRadiusHazmatEngineTest {

    @Test
    @DisplayName("Should correctly rate fleet radius multiplier and Hazmat Class 3 surcharge")
    void testFleetRadiusAndHazmatRating() {
        int vehicles = 10;
        BigDecimal basePerUnit = new BigDecimal("2000.00"); // Base fleet = 20,000
        String radius = "INTERMEDIATE"; // 1.25x -> 25,000
        String hazmat = "CLASS_3_FLAMMABLE"; // +40% -> 10,000
        boolean attachPollution = true; // $1,250

        var res = AutoFleetRadiusHazmatEngine.getInstance().rateFleetRadiusAndHazmat(
                null, vehicles, basePerUnit, radius, hazmat, attachPollution
        );

        assertNotNull(res);
        assertEquals(new BigDecimal("20000.00"), res.baseFleetLiabilityPremium);
        assertEquals(new BigDecimal("25000.00"), res.radiusAdjustedPremium);
        assertEquals(new BigDecimal("10000.00"), res.hazmatSurchargeAmount);
        assertEquals(new BigDecimal("1250.00"), res.pollutionEndorsementPremium);
        // Total = 25000 + 10000 + 1250 = 36250.00
        assertEquals(new BigDecimal("36250.00"), res.totalCommercialAutoFleetPremium);
    }

    @Test
    @DisplayName("Should apply Long Distance 1.60x factor and Explosives 85% surcharge")
    void testLongDistanceAndExplosives() {
        var res = AutoFleetRadiusHazmatEngine.getInstance().rateFleetRadiusAndHazmat(
                null, 5, new BigDecimal("2000.00"), "LONG_DISTANCE", "CLASS_1_EXPLOSIVES", false
        );

        assertEquals(new BigDecimal("10000.00"), res.baseFleetLiabilityPremium);
        assertEquals(new BigDecimal("16000.00"), res.radiusAdjustedPremium); // 10k * 1.60
        assertEquals(new BigDecimal("13600.00"), res.hazmatSurchargeAmount); // 16k * 0.85
        assertEquals(new BigDecimal("29600.00"), res.totalCommercialAutoFleetPremium);
    }
}
