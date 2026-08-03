package com.guidewire.pc;

import com.guidewire.pc.model.CessionLedgerEntry;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.ReinsuranceLedgerEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Reinsurance Treaty Layering & Loss Recovery Ledger Engine Tests")
public class ReinsuranceLedgerEngineTest {

    @Test
    @DisplayName("Should generate gross-to-net cession ledger entries for policy premium")
    public void testReinsuranceCessionLedger() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-RI-5001");
        period.setTotalPremium(new BigDecimal("10000.00"));

        List<CessionLedgerEntry> ledger = ReinsuranceLedgerEngine.getInstance().generateCessionLedger(period);
        assertNotNull(ledger);
        assertFalse(ledger.isEmpty());
        assertEquals(3, ledger.size());

        CessionLedgerEntry qsEntry = ledger.get(0);
        assertEquals("POL-RI-5001", qsEntry.getPolicyNumber());
        assertEquals("Swiss Re", qsEntry.getReinsurerName());
        assertEquals(new BigDecimal("3000.00"), qsEntry.getCededPremium());
        assertEquals(new BigDecimal("600.00"), qsEntry.getCedingCommission());
    }

    @Test
    @DisplayName("Should simulate reinsurance claim loss recoveries across treaty layers")
    public void testReinsuranceClaimLossSimulation() {
        BigDecimal lossAmount = new BigDecimal("2500000.00"); // $2.5M loss

        Map<String, Object> result = ReinsuranceLedgerEngine.getInstance().simulateClaimLossRecovery(lossAmount);
        assertNotNull(result);
        assertEquals(lossAmount, result.get("totalClaimLoss"));

        BigDecimal totalRecovery = (BigDecimal) result.get("totalReinsuranceRecovery");
        BigDecimal netRetained = (BigDecimal) result.get("netInsurerRetainedLoss");

        assertNotNull(totalRecovery);
        assertNotNull(netRetained);
        assertTrue(totalRecovery.compareTo(BigDecimal.ZERO) > 0);
        assertEquals(lossAmount, totalRecovery.add(netRetained));
    }
}
