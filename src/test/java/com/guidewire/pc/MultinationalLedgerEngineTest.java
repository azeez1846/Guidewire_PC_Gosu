package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.MultinationalLedgerEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Multinational Policy & Local Fronting Policy Ledger Engine Tests")
public class MultinationalLedgerEngineTest {

    @Test
    @DisplayName("Should generate local fronting policies with local currency and taxes across 4 jurisdictions")
    public void testMultinationalLedgerGeneration() {
        PolicyPeriod master = new PolicyPeriod();
        master.setPolicyNumber("POL-GLOBAL-9001");
        master.setTotalPremium(new BigDecimal("100000.00")); // $100k USD Master

        List<MultinationalLedgerEngine.LocalFrontingPolicyResult> frontingList = MultinationalLedgerEngine.getInstance()
                .generateMultinationalLedger(master);

        assertNotNull(frontingList);
        assertEquals(4, frontingList.size());

        for (var f : frontingList) {
            assertEquals("POL-GLOBAL-9001", f.getMasterPolicyNumber());
            assertTrue(f.getLocalPremium().compareTo(BigDecimal.ZERO) > 0);
            assertTrue(f.getLocalTaxAmount().compareTo(BigDecimal.ZERO) > 0);
            assertEquals(f.getLocalPremium().add(f.getLocalTaxAmount()), f.getTotalLocalCost());
        }
    }
}
