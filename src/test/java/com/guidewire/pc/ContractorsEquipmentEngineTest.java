package com.guidewire.pc;

import com.guidewire.pc.service.ContractorsEquipmentEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Inland Marine Contractors' Equipment Floater Engine Tests")
public class ContractorsEquipmentEngineTest {

    @Test
    @DisplayName("Should correctly rate scheduled equipment, boom perils, and deductible credits")
    void testContractorsEquipmentRating() {
        BigDecimal sched = new BigDecimal("500000.00"); // 5000 * 1.65 = 8250 (ACV) -> RC = 8250 * 1.12 = 9240
        BigDecimal rented = new BigDecimal("100000.00"); // 1000 * 1.90 = 1900
        BigDecimal unsched = new BigDecimal("20000.00");  // 200 * 2.25 = 450
        String val = "REPLACEMENT_COST";
        boolean boom = true;
        BigDecimal ded = new BigDecimal("5000.00");

        var res = ContractorsEquipmentEngine.getInstance().rateContractorsEquipment(
                null, sched, rented, unsched, val, boom, ded
        );

        assertNotNull(res);
        assertEquals(new BigDecimal("9240.00"), res.scheduledBasePremium);
        assertEquals(new BigDecimal("1900.00"), res.rentedEquipmentPremium);
        assertEquals(new BigDecimal("450.00"), res.unscheduledToolsPremium);

        // Boom surcharge = 9240 * 0.20 = 1848.00
        assertEquals(new BigDecimal("1848.00"), res.boomOverloadSurcharge);

        // Base combined = 9240 + 1900 + 450 = 11590
        // $5,000 deductible credit = -12% of 11590 = -1390.80
        assertEquals(new BigDecimal("-1390.80"), res.deductibleCreditOrCharge);

        // Subtotal = 11590 + 1848 - 1390.80 = 12047.20
        assertEquals(new BigDecimal("12047.20"), res.subtotalPremium);
        assertTrue(res.totalEquipmentFloaterPremium.compareTo(res.subtotalPremium) > 0);
    }
}
