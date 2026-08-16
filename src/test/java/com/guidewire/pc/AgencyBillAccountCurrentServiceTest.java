package com.guidewire.pc;

import com.guidewire.pc.service.AgencyBillAccountCurrentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Agency Bill & Account Current Statement Engine Tests")
public class AgencyBillAccountCurrentServiceTest {

    @Test
    @DisplayName("Should generate reconciled monthly agency Account Current statement with commissions")
    void testAccountCurrentStatementReconciliation() {
        var items = List.of(
                new AgencyBillAccountCurrentService.AccountCurrentLineItem("POL-CA-101", "Client A", "NEW_BUSINESS", new BigDecimal("10000.00"), new BigDecimal("15.0")),
                new AgencyBillAccountCurrentService.AccountCurrentLineItem("POL-CP-102", "Client B", "RENEWAL", new BigDecimal("20000.00"), new BigDecimal("10.0")),
                new AgencyBillAccountCurrentService.AccountCurrentLineItem("POL-WC-103", "Client C", "CANCELLATION", new BigDecimal("-2000.00"), new BigDecimal("10.0"))
        );

        var stmt = AgencyBillAccountCurrentService.getInstance().generateAccountCurrent(
                "PR-WEST-901", "Pacific Coast Brokers", "2026-08", items
        );

        assertNotNull(stmt);
        assertEquals("PR-WEST-901", stmt.producerCode);
        assertEquals(3, stmt.lineItems.size());

        // Gross = 10k + 20k - 2k = 28,000
        assertEquals(new BigDecimal("28000.00"), stmt.totalGrossWrittenPremium);
        // Commissions = 1500 + 2000 - 200 = 3300
        assertEquals(new BigDecimal("3300.00"), stmt.totalAgencyCommission);
        // Net remittance = 28000 - 3300 = 24700
        assertEquals(new BigDecimal("24700.00"), stmt.totalNetRemittancePayable);
        assertEquals("BALANCED_RECONCILED", stmt.reconciliationStatus);
    }
}
