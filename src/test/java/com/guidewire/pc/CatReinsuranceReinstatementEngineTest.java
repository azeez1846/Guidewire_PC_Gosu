package com.guidewire.pc;

import com.guidewire.pc.service.CatReinsuranceReinstatementEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Catastrophe Reinsurance Reinstatement Premium Engine Tests")
public class CatReinsuranceReinstatementEngineTest {

    @Test
    @DisplayName("Should correctly calculate pro-rata as to amount and time reinstatement premium")
    void testCatReinstatementCalculation() {
        String treatyRef = "TREATY-CAT-2026";
        String layerDesc = "$50M xs $25M Catastrophe Layer";
        BigDecimal layerLimit = new BigDecimal("50000000.00");
        BigDecimal annualPrem = new BigDecimal("4000000.00");
        BigDecimal catLoss = new BigDecimal("25000000.00"); // 50% of layer
        String eff = "2026-01-01";
        String lossDate = "2026-07-02"; // ~50% elapsed
        String exp = "2027-01-01";
        double rate = 100.0;

        var res = CatReinsuranceReinstatementEngine.getInstance().calculateCatReinstatement(
                treatyRef, layerDesc, layerLimit, annualPrem, catLoss, eff, lossDate, exp, rate
        );

        assertNotNull(res);
        assertEquals(layerLimit, res.treatyLayerLimit);
        assertEquals(catLoss, res.catastrophicLossAmount);
        assertEquals(new BigDecimal("0.500000"), res.amountFractionConsumed);
        assertTrue(res.reinstatementPremiumDue.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(res.reinstatementPremiumDue.compareTo(new BigDecimal("2000000.00")) < 0);
        assertEquals(catLoss, res.restoredTreatyCapacity);
    }
}
